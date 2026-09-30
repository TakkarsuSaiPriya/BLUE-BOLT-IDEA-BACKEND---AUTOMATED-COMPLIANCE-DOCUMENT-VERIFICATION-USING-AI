package com.compliance.documentservice.service;

import com.compliance.documentservice.entity.Document;
import com.compliance.documentservice.enums.DocumentStatus;
import com.compliance.documentservice.producer.DocumentEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class TransactionEventPublisher {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    TransactionEventPublisher.class
            );

    private final DocumentEventProducer documentEventProducer;

    public TransactionEventPublisher(
            DocumentEventProducer documentEventProducer) {

        this.documentEventProducer =
                documentEventProducer;
    }

    public void publishDocumentUploadedAfterCommit(
            Document document) {

        executeAfterCommit(() ->
                documentEventProducer
                        .publishDocumentUploaded(document)
        );
    }

    public void publishStatusChangedAfterCommit(
            Document document,
            DocumentStatus previousStatus,
            String changedBy) {

        executeAfterCommit(() ->
                documentEventProducer
                        .publishDocumentStatusChanged(
                                document,
                                previousStatus,
                                changedBy
                        )
        );
    }

    public void publishDocumentDeletedAfterCommit(
            Document document,
            String deletedBy) {

        executeAfterCommit(() ->
                documentEventProducer
                        .publishDocumentDeleted(
                                document,
                                deletedBy
                        )
        );
    }

    private void executeAfterCommit(
            Runnable eventPublication) {

        if (TransactionSynchronizationManager
                .isActualTransactionActive()
                && TransactionSynchronizationManager
                .isSynchronizationActive()) {

            TransactionSynchronizationManager
                    .registerSynchronization(
                            new TransactionSynchronization() {

                                @Override
                                public void afterCommit() {
                                    publishSafely(
                                            eventPublication
                                    );
                                }
                            }
                    );

            return;
        }

        publishSafely(eventPublication);
    }

    private void publishSafely(
            Runnable eventPublication) {

        try {
            eventPublication.run();

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "Document transaction committed, but event publication failed",
                    exception
            );
        }
    }
}