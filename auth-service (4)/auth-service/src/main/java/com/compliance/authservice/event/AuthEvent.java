package com.compliance.authservice.event;

import com.compliance.authservice.enums.AuthEventType;

import java.time.LocalDateTime;

public class AuthEvent {

    private String eventId;
    private AuthEventType eventType;
    private Long userId;
    private String username;
    private String email;
    private String message;
    private LocalDateTime occurredAt;

    public AuthEvent() {
    }

    public AuthEvent(
            String eventId,
            AuthEventType eventType,
            Long userId,
            String username,
            String email,
            String message,
            LocalDateTime occurredAt) {

        this.eventId = eventId;
        this.eventType = eventType;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.message = message;
        this.occurredAt = occurredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public AuthEventType getEventType() {
        return eventType;
    }

    public void setEventType(AuthEventType eventType) {
        this.eventType = eventType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}