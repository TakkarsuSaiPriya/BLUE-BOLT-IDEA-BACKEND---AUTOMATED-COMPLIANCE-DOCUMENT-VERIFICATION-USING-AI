package com.compliance.verificationservice.config;

import com.compliance.verificationservice.dto.event.OcrCompletedEvent;
import com.compliance.verificationservice.dto.event.VerificationCompletedEvent;
import com.compliance.verificationservice.dto.event.VerificationFailedEvent;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.List;
import java.util.Map;

@Configuration
public class ActiveMqConfig {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ActiveMqConfig.class
            );

    @Bean
    public ConnectionFactory activeMqConnectionFactory(
            @Value("${spring.activemq.broker-url}")
            String brokerUrl,
            @Value("${spring.activemq.user}")
            String username,
            @Value("${spring.activemq.password}")
            String password,
            MessagingProperties messagingProperties) {

        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(
                        username,
                        password,
                        brokerUrl
                );

        connectionFactory.setTrustedPackages(
                List.of(
                        "java.lang",
                        "java.util",
                        "java.time",
                        "java.math",
                        "com.compliance.verificationservice.dto.event",
                        "com.compliance.verificationservice.dto.internal"
                )
        );

        connectionFactory.setRedeliveryPolicy(
                createRedeliveryPolicy(
                        messagingProperties
                )
        );

        return connectionFactory;
    }

    @Bean
    public RedeliveryPolicy activeMqRedeliveryPolicy(
            MessagingProperties messagingProperties) {

        return createRedeliveryPolicy(
                messagingProperties
        );
    }

    @Bean
    public MessageConverter
    verificationJmsMessageConverter() {

        MappingJackson2MessageConverter converter =
                new MappingJackson2MessageConverter();

        converter.setTargetType(
                MessageType.TEXT
        );

        converter.setTypeIdPropertyName(
                "_eventType"
        );

        converter.setTypeIdMappings(
                Map.of(
                        "OCR_COMPLETED",
                        OcrCompletedEvent.class,
                        "VERIFICATION_COMPLETED",
                        VerificationCompletedEvent.class,
                        "VERIFICATION_FAILED",
                        VerificationFailedEvent.class
                )
        );

        return converter;
    }

    @Bean(
            name =
                    "verificationJmsListenerContainerFactory"
    )
    public DefaultJmsListenerContainerFactory
    verificationJmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer
                    configurer,
            @Qualifier(
                    "verificationJmsMessageConverter"
            )
            MessageConverter messageConverter) {

        DefaultJmsListenerContainerFactory factory =
                new DefaultJmsListenerContainerFactory();

        configurer.configure(
                factory,
                connectionFactory
        );

        factory.setMessageConverter(
                messageConverter
        );

        factory.setPubSubDomain(
                false
        );

        factory.setSessionTransacted(
                true
        );

        factory.setConcurrency(
                "1-3"
        );

        factory.setErrorHandler(
                throwable ->
                        LOGGER.error(
                                "ActiveMQ listener execution failed",
                                throwable
                        )
        );

        return factory;
    }

    @Bean(
            name = "verificationTopicJmsTemplate"
    )
    public JmsTemplate verificationTopicJmsTemplate(
            ConnectionFactory connectionFactory,
            @Qualifier(
                    "verificationJmsMessageConverter"
            )
            MessageConverter messageConverter) {

        JmsTemplate jmsTemplate =
                new JmsTemplate(
                        connectionFactory
                );

        jmsTemplate.setMessageConverter(
                messageConverter
        );

        jmsTemplate.setPubSubDomain(
                true
        );

        jmsTemplate.setDeliveryPersistent(
                true
        );

        jmsTemplate.setDeliveryMode(
                DeliveryMode.PERSISTENT
        );

        jmsTemplate.setExplicitQosEnabled(
                true
        );

        return jmsTemplate;
    }

    private RedeliveryPolicy createRedeliveryPolicy(
            MessagingProperties messagingProperties) {

        MessagingProperties.Redelivery settings =
                messagingProperties.getRedelivery();

        RedeliveryPolicy policy =
                new RedeliveryPolicy();

        policy.setInitialRedeliveryDelay(
                settings.getInitialDelay()
        );

        policy.setRedeliveryDelay(
                settings.getDelay()
        );

        policy.setMaximumRedeliveries(
                settings.getMaximumAttempts()
        );

        policy.setUseExponentialBackOff(
                settings.isUseExponentialBackoff()
        );

        policy.setBackOffMultiplier(
                settings.getBackoffMultiplier()
        );

        return policy;
    }
}