package com.compliance.documentservice.event;

import com.compliance.documentservice.enums.AuditActionType;

import java.time.LocalDateTime;

public class AuditEvent {

    private String eventId;
    private String serviceName;
    private AuditActionType action;
    private String username;
    private Long documentId;
    private String requestMethod;
    private String requestPath;
    private String requestPayload;
    private String responsePayload;
    private String executionStatus;
    private String ipAddress;
    private String errorMessage;
    private LocalDateTime occurredAt;

    public AuditEvent() {
    }

    public AuditEvent(
            String eventId,
            String serviceName,
            AuditActionType action,
            String username,
            Long documentId,
            String requestMethod,
            String requestPath,
            String requestPayload,
            String responsePayload,
            String executionStatus,
            String ipAddress,
            String errorMessage,
            LocalDateTime occurredAt) {

        this.eventId = eventId;
        this.serviceName = serviceName;
        this.action = action;
        this.username = username;
        this.documentId = documentId;
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
        this.requestPayload = requestPayload;
        this.responsePayload = responsePayload;
        this.executionStatus = executionStatus;
        this.ipAddress = ipAddress;
        this.errorMessage = errorMessage;
        this.occurredAt = occurredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(
            String serviceName) {

        this.serviceName = serviceName;
    }

    public AuditActionType getAction() {
        return action;
    }

    public void setAction(
            AuditActionType action) {

        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username) {

        this.username = username;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(
            Long documentId) {

        this.documentId = documentId;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(
            String requestMethod) {

        this.requestMethod = requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(
            String requestPath) {

        this.requestPath = requestPath;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(
            String requestPayload) {

        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(
            String responsePayload) {

        this.responsePayload = responsePayload;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(
            String executionStatus) {

        this.executionStatus = executionStatus;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(
            String ipAddress) {

        this.ipAddress = ipAddress;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(
            String errorMessage) {

        this.errorMessage = errorMessage;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(
            LocalDateTime occurredAt) {

        this.occurredAt = occurredAt;
    }
}