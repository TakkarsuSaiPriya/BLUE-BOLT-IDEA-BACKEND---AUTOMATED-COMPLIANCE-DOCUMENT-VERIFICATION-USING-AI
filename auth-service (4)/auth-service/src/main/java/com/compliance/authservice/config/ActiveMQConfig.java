package com.compliance.authservice.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
public class ActiveMQConfig {

    @Bean
    public ConnectionFactory connectionFactory() {

        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory();

        connectionFactory.setBrokerURL(
                "vm://embedded?broker.persistent=false"
        );

        return connectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate(
            ConnectionFactory connectionFactory) {

        JmsTemplate jmsTemplate =
                new JmsTemplate(connectionFactory);

        jmsTemplate.setPubSubDomain(false);

        return jmsTemplate;
    }

    @Bean(name = "queueListenerFactory")
    public DefaultJmsListenerContainerFactory queueListenerFactory(
            ConnectionFactory connectionFactory) {

        DefaultJmsListenerContainerFactory factory =
                new DefaultJmsListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(false);
        factory.setConcurrency("1-3");

        return factory;
    }
}