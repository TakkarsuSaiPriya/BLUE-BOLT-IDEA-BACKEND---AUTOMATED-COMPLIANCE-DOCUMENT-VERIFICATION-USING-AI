package com.compliance.documentservice.producer;

import com.compliance.documentservice.config.MessagingDestinations;
import com.compliance.documentservice.entity.DocumentAuditLog;
import com.compliance.documentservice.event.AuditEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentAuditEventProducer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    DocumentAuditEventProducer.class
            );

    private static final String SERVICE_NAME =
            "document-service";

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    public DocumentAuditEventProducer(
            JmsTemplate jmsTemplate,
            ObjectMapper objectMapper) {

        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishAuditEvent(
            DocumentAuditLog auditLog) {

        if (auditLog == null) {
            throw new IllegalArgumentException(
                    "Audit log is required"
            );
        }

        AuditEvent auditEvent =
                new AuditEvent(
                        UUID.randomUUID().toString(),
                        SERVICE_NAME,
                        auditLog.getAction(),
                        auditLog.getUsername(),
                        auditLog.getDocumentId(),
                        auditLog.getRequestMethod(),
                        auditLog.getRequestPath(),
                        auditLog.getRequestPayload(),
                        auditLog.getResponsePayload(),
                        auditLog.getExecutionStatus(),
                        auditLog.getIpAddress(),
                        auditLog.getErrorMessage(),
                        auditLog.getCreatedAt()
                );

        try {
            String payload =
                    objectMapper.writeValueAsString(
                            auditEvent
                    );

            ActiveMQTopic topic =
                    new ActiveMQTopic(
                            MessagingDestinations
                                    .AUDIT_EVENT_TOPIC
                    );

            jmsTemplate.convertAndSend(
                    topic,
                    payload
            );

            LOGGER.info(
                    "Published audit event {} for action {}",
                    auditEvent.getEventId(),
                    auditEvent.getAction()
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to serialize audit event",
                    exception
            );
        }
    }
}