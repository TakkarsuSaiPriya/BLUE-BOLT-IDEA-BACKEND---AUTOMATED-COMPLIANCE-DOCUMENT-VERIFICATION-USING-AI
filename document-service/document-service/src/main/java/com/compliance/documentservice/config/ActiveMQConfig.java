package com.compliance.documentservice.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
public class ActiveMQConfig {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user:admin}")
    private String brokerUsername;

    @Value("${spring.activemq.password:admin}")
    private String brokerPassword;

    @Bean
    public ConnectionFactory connectionFactory() {

        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory();

        connectionFactory.setBrokerURL(brokerUrl);
        connectionFactory.setUserName(brokerUsername);
        connectionFactory.setPassword(brokerPassword);
        connectionFactory.setTrustAllPackages(false);

        return connectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate(
            ConnectionFactory connectionFactory) {

        JmsTemplate jmsTemplate =
                new JmsTemplate(connectionFactory);

        jmsTemplate.setPubSubDomain(false);
        jmsTemplate.setDeliveryPersistent(true);
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setTimeToLive(0L);

        return jmsTemplate;
    }

    @Bean(name = "queueListenerFactory")
    public DefaultJmsListenerContainerFactory
    queueListenerFactory(
            ConnectionFactory connectionFactory) {

        DefaultJmsListenerContainerFactory factory =
                new DefaultJmsListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(false);
        factory.setConcurrency("1-3");
        factory.setRecoveryInterval(5000L);

        return factory;
    }
}