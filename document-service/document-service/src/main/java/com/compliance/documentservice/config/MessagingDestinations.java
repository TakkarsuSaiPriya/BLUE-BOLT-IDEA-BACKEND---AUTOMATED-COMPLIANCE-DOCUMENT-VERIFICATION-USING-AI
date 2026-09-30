package com.compliance.documentservice.config;

public final class MessagingDestinations {

    public static final String DOCUMENT_UPLOADED_TOPIC =
            "VirtualTopic.DocumentUploaded";

    public static final String DOCUMENT_STATUS_CHANGED_TOPIC =
            "VirtualTopic.DocumentStatusChanged";

    public static final String DOCUMENT_DELETED_TOPIC =
            "VirtualTopic.DocumentDeleted";

    public static final String AUDIT_EVENT_TOPIC =
            "VirtualTopic.AuditEvent";

    public static final String DOCUMENT_UPLOAD_FAILED_TOPIC =
            "VirtualTopic.DocumentUploadFailed";

    private MessagingDestinations() {
    }
}
