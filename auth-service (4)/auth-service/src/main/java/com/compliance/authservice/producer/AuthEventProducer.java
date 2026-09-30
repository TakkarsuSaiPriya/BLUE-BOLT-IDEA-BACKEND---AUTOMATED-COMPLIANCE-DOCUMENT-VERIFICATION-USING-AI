package com.compliance.authservice.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AuthEventProducer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AuthEventProducer.class);

    private static final String USER_REGISTERED_TOPIC =
            "VirtualTopic.UserRegistered";

    private static final String USER_LOGGED_IN_TOPIC =
            "VirtualTopic.UserLoggedIn";

    private static final String TOKEN_REFRESHED_TOPIC =
            "VirtualTopic.TokenRefreshed";

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    public AuthEventProducer(
            JmsTemplate jmsTemplate,
            ObjectMapper objectMapper) {

        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishUserRegistered(
            Long userId,
            String username,
            String email) {

        publishEvent(
                USER_REGISTERED_TOPIC,
                "USER_REGISTERED",
                userId,
                username,
                email,
                "User registered successfully"
        );
    }

    public void publishUserLoggedIn(
            Long userId,
            String username,
            String email) {

        publishEvent(
                USER_LOGGED_IN_TOPIC,
                "USER_LOGGED_IN",
                userId,
                username,
                email,
                "User logged in successfully"
        );
    }

    public void publishTokenRefreshed(
            Long userId,
            String username,
            String email) {

        publishEvent(
                TOKEN_REFRESHED_TOPIC,
                "TOKEN_REFRESHED",
                userId,
                username,
                email,
                "Access token refreshed successfully"
        );
    }

    private void publishEvent(
            String topicName,
            String eventType,
            Long userId,
            String username,
            String email,
            String message) {

        Map<String, Object> event = new LinkedHashMap<>();

        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", eventType);
        event.put("userId", userId);
        event.put("username", username);
        event.put("email", email);
        event.put("message", message);
        event.put("occurredAt", LocalDateTime.now().toString());

        try {
            String payload =
                    objectMapper.writeValueAsString(event);

            ActiveMQTopic destination =
                    new ActiveMQTopic(topicName);

            jmsTemplate.convertAndSend(
                    destination,
                    payload
            );

            LOGGER.info(
                    "Published event {} to {} for user {}",
                    eventType,
                    topicName,
                    username
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to serialize authentication event",
                    exception
            );
        }
    }
}