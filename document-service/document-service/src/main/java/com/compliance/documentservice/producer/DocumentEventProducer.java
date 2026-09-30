package com.compliance.documentservice.producer;

import com.compliance.documentservice.config.MessagingDestinations;
import com.compliance.documentservice.entity.Document;
import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.event.DocumentDeletedEvent;
import com.compliance.documentservice.event.DocumentStatusChangedEvent;
import com.compliance.documentservice.event.DocumentUploadedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DocumentEventProducer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    DocumentEventProducer.class
            );

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    public DocumentEventProducer(
            JmsTemplate jmsTemplate,
            ObjectMapper objectMapper) {

        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishDocumentUploaded(
            Document document) {

        if (document == null) {
            throw new IllegalArgumentException(
                    "Document is required for upload event"
            );
        }

        DocumentUploadedEvent event =
                new DocumentUploadedEvent(
                        UUID.randomUUID().toString(),
                        "DOCUMENT_UPLOADED",
                        document.getId(),
                        document.getOriginalFileName(),
                        document.getContentType(),
                        document.getFileSize(),
                        document.getChecksum(),
                        document.getDocumentType(),
                        document.getStatus(),
                        document.getUploadedBy(),
                        LocalDateTime.now()
                );

        publish(
                MessagingDestinations
                        .DOCUMENT_UPLOADED_TOPIC,
                event,
                event.getEventId()
        );
    }

    public void publishDocumentStatusChanged(
            Document document,
            DocumentStatus previousStatus,
            String changedBy) {

        if (document == null) {
            throw new IllegalArgumentException(
                    "Document is required for status event"
            );
        }

        DocumentStatusChangedEvent event =
                new DocumentStatusChangedEvent(
                        UUID.randomUUID().toString(),
                        "DOCUMENT_STATUS_CHANGED",
                        document.getId(),
                        previousStatus,
                        document.getStatus(),
                        document.getStatusReason(),
                        changedBy,
                        LocalDateTime.now()
                );

        publish(
                MessagingDestinations
                        .DOCUMENT_STATUS_CHANGED_TOPIC,
                event,
                event.getEventId()
        );
    }

    public void publishDocumentDeleted(
            Document document,
            String deletedBy) {

        if (document == null) {
            throw new IllegalArgumentException(
                    "Document is required for deletion event"
            );
        }

        DocumentDeletedEvent event =
                new DocumentDeletedEvent(
                        UUID.randomUUID().toString(),
                        "DOCUMENT_DELETED",
                        document.getId(),
                        document.getOriginalFileName(),
                        document.getUploadedBy(),
                        deletedBy,
                        LocalDateTime.now()
                );

        publish(
                MessagingDestinations
                        .DOCUMENT_DELETED_TOPIC,
                event,
                event.getEventId()
        );
    }

    private void publish(
            String topicName,
            Object event,
            String eventId) {

        try {
            String payload =
                    objectMapper.writeValueAsString(event);

            ActiveMQTopic topic =
                    new ActiveMQTopic(topicName);

            jmsTemplate.convertAndSend(
                    topic,
                    payload
            );

            LOGGER.info(
                    "Published document event {} to topic {}",
                    eventId,
                    topicName
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to serialize document event",
                    exception
            );
        }
    }
}