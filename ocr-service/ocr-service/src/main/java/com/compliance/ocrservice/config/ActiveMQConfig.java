package com.compliance.ocrservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import jakarta.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.util.ErrorHandler;

import java.util.List;

@Configuration
@EnableJms
public class ActiveMQConfig {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ActiveMQConfig.class
            );

    public static final String DOCUMENT_UPLOADED_TOPIC =
            "VirtualTopic.DocumentUploaded";

    public static final String OCR_CONSUMER_QUEUE =
            "Consumer.OcrService."
                    + DOCUMENT_UPLOADED_TOPIC;

    public static final String OCR_COMPLETED_TOPIC =
            "VirtualTopic.OcrCompleted";

    public static final String OCR_FAILED_TOPIC =
            "VirtualTopic.OcrFailed";

    @Bean
    public RedeliveryPolicy redeliveryPolicy(
            @Value("${messaging.redelivery.initial-delay:2000}")
            long initialDelay,
            @Value("${messaging.redelivery.delay:5000}")
            long redeliveryDelay,
            @Value("${messaging.redelivery.maximum-attempts:3}")
            int maximumRedeliveries,
            @Value("${messaging.redelivery.use-exponential-backoff:true}")
            boolean useExponentialBackoff,
            @Value("${messaging.redelivery.backoff-multiplier:2.0}")
            double backoffMultiplier) {

        RedeliveryPolicy redeliveryPolicy =
                new RedeliveryPolicy();

        redeliveryPolicy.setInitialRedeliveryDelay(
                Math.max(initialDelay, 0L)
        );

        redeliveryPolicy.setRedeliveryDelay(
                Math.max(redeliveryDelay, 0L)
        );

        redeliveryPolicy.setMaximumRedeliveries(
                Math.max(maximumRedeliveries, 0)
        );

        redeliveryPolicy.setUseExponentialBackOff(
                useExponentialBackoff
        );

        redeliveryPolicy.setBackOffMultiplier(
                Math.max(
                        backoffMultiplier,
                        1.0
                )
        );

        return redeliveryPolicy;
    }

    @Bean
    public ConnectionFactory activeMqConnectionFactory(
            BrokerService embeddedActiveMqBroker,
            RedeliveryPolicy redeliveryPolicy,
            @Value("${spring.activemq.broker-url}")
            String brokerUrl,
            @Value("${spring.activemq.user:admin}")
            String username,
            @Value("${spring.activemq.password:admin}")
            String password) {

        if (!embeddedActiveMqBroker.isStarted()) {
            throw new IllegalStateException(
                    "Embedded ActiveMQ broker did not start"
            );
        }

        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory();

        connectionFactory.setBrokerURL(
                brokerUrl
        );

        connectionFactory.setUserName(
                username
        );

        connectionFactory.setPassword(
                password
        );

        connectionFactory.setRedeliveryPolicy(
                redeliveryPolicy
        );

        connectionFactory.setTrustAllPackages(
                false
        );

        connectionFactory.setTrustedPackages(
                List.of(
                        "java.lang",
                        "java.util",
                        "java.time",
                        "com.compliance.ocrservice.event"
                )
        );

        LOGGER.info(
                "Configured ActiveMQ connection factory "
                        + "for embedded broker at {}",
                brokerUrl
        );

        return connectionFactory;
    }

    @Bean
    public MappingJackson2MessageConverter
    jacksonJmsMessageConverter(
            ObjectMapper objectMapper) {

        MappingJackson2MessageConverter converter =
                new MappingJackson2MessageConverter();

        converter.setObjectMapper(
                objectMapper
        );

        converter.setTargetType(
                MessageType.TEXT
        );

        converter.setTypeIdPropertyName(
                "_messageType"
        );

        return converter;
    }

    @Bean(name = "ocrJmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory
    ocrJmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer,
            MappingJackson2MessageConverter messageConverter,
            ErrorHandler jmsErrorHandler) {

        DefaultJmsListenerContainerFactory factory =
                new DefaultJmsListenerContainerFactory();

        configurer.configure(
                factory,
                connectionFactory
        );

        factory.setConnectionFactory(
                connectionFactory
        );

        factory.setMessageConverter(
                messageConverter
        );

        factory.setSessionTransacted(
                true
        );

        factory.setConcurrency(
                "1-3"
        );

        factory.setRecoveryInterval(
                5000L
        );

        factory.setErrorHandler(
                jmsErrorHandler
        );

        return factory;
    }

    @Bean
    public ErrorHandler jmsErrorHandler() {

        return throwable ->
                LOGGER.error(
                        "OCR JMS listener failed. "
                                + "ActiveMQ redelivery will apply.",
                        throwable
                );
    }

    @Bean
    public JmsTemplate jmsTemplate(
            ConnectionFactory connectionFactory,
            MappingJackson2MessageConverter messageConverter) {

        JmsTemplate jmsTemplate =
                new JmsTemplate(
                        connectionFactory
                );

        jmsTemplate.setMessageConverter(
                messageConverter
        );

        jmsTemplate.setDeliveryPersistent(
                true
        );

        jmsTemplate.setExplicitQosEnabled(
                true
        );

        jmsTemplate.setSessionTransacted(
                false
        );

        return jmsTemplate;
    }

    @Bean(name = "documentUploadedConsumerQueue")
    public Queue documentUploadedConsumerQueue() {

        return new ActiveMQQueue(
                OCR_CONSUMER_QUEUE
        );
    }

    @Bean(name = "ocrCompletedTopic")
    public Topic ocrCompletedTopic() {

        return new ActiveMQTopic(
                OCR_COMPLETED_TOPIC
        );
    }

    @Bean(name = "ocrFailedTopic")
    public Topic ocrFailedTopic() {

        return new ActiveMQTopic(
                OCR_FAILED_TOPIC
        );
    }
}