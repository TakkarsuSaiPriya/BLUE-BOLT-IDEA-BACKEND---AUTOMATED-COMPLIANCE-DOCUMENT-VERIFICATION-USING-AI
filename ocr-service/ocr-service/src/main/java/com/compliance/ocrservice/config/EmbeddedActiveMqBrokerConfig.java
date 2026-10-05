package com.compliance.ocrservice.config;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedActiveMqBrokerConfig {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EmbeddedActiveMqBrokerConfig.class
            );

    @Bean(
            name = "embeddedActiveMqBroker",
            initMethod = "start",
            destroyMethod = "stop"
    )
    public BrokerService embeddedActiveMqBroker(
            @Value("${embedded-activemq.broker-name:blue-bolt-broker}")
            String brokerName,
            @Value("${embedded-activemq.tcp-connector:tcp://0.0.0.0:61616}")
            String tcpConnector,
            @Value("${embedded-activemq.persistent:false}")
            boolean persistent,
            @Value("${embedded-activemq.use-jmx:false}")
            boolean useJmx,
            @Value("${embedded-activemq.delete-messages-on-startup:true}")
            boolean deleteMessagesOnStartup)
            throws Exception {

        BrokerService brokerService =
                new BrokerService();

        brokerService.setBrokerName(
                brokerName
        );

        brokerService.setPersistent(
                persistent
        );

        brokerService.setUseJmx(
                useJmx
        );

        brokerService.setUseShutdownHook(
                false
        );

        brokerService.setSchedulerSupport(
                false
        );

        brokerService.setDeleteAllMessagesOnStartup(
                deleteMessagesOnStartup
        );

        brokerService.setAdvisorySupport(
                true
        );

        TransportConnector transportConnector =
                brokerService.addConnector(
                        tcpConnector
                );

        transportConnector.setName(
                "openwire"
        );

        LOGGER.info(
                "Configured embedded ActiveMQ broker. "
                        + "brokerName={}, connector={}, persistent={}",
                brokerName,
                tcpConnector,
                persistent
        );

        return brokerService;
    }
}