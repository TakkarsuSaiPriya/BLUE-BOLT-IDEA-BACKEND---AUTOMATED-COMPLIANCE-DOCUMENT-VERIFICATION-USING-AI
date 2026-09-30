package com.compliance.authservice.consumer;

import com.compliance.authservice.config.MessagingDestinations;
import com.compliance.authservice.event.AuthEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class AuthEventConsumer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AuthEventConsumer.class);

    private final ObjectMapper objectMapper;

    public AuthEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @JmsListener(
            destination =
                    MessagingDestinations.AUTH_USER_REGISTERED_QUEUE,
            containerFactory = "queueListenerFactory"
    )
    public void consumeUserRegisteredEvent(
            String eventPayload) {

        AuthEvent event = convertPayload(eventPayload);

        LOGGER.info(
                "Consumed USER_REGISTERED event. Event ID: {}, username: {}",
                event.getEventId(),
                event.getUsername()
        );
    }

    @JmsListener(
            destination =
                    MessagingDestinations.AUTH_USER_LOGGED_IN_QUEUE,
            containerFactory = "queueListenerFactory"
    )
    public void consumeUserLoggedInEvent(
            String eventPayload) {

        AuthEvent event = convertPayload(eventPayload);

        LOGGER.info(
                "Consumed USER_LOGGED_IN event. Event ID: {}, username: {}",
                event.getEventId(),
                event.getUsername()
        );
    }

    @JmsListener(
            destination =
                    MessagingDestinations.AUTH_TOKEN_REFRESHED_QUEUE,
            containerFactory = "queueListenerFactory"
    )
    public void consumeTokenRefreshedEvent(
            String eventPayload) {

        AuthEvent event = convertPayload(eventPayload);

        LOGGER.info(
                "Consumed TOKEN_REFRESHED event. Event ID: {}, username: {}",
                event.getEventId(),
                event.getUsername()
        );
    }

    private AuthEvent convertPayload(
            String eventPayload) {

        try {

            return objectMapper.readValue(
                    eventPayload,
                    AuthEvent.class
            );

        } catch (JsonProcessingException exception) {

            LOGGER.error(
                    "Failed to deserialize authentication event payload",
                    exception
            );

            throw new IllegalArgumentException(
                    "Invalid authentication event payload",
                    exception
            );
        }
    }
}