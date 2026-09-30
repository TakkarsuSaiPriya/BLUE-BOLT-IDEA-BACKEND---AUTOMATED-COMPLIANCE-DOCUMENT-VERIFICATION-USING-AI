package com.compliance.authservice.config;

public final class MessagingDestinations {

    public static final String USER_REGISTERED_TOPIC =
            "VirtualTopic.UserRegistered";

    public static final String USER_LOGGED_IN_TOPIC =
            "VirtualTopic.UserLoggedIn";

    public static final String TOKEN_REFRESHED_TOPIC =
            "VirtualTopic.TokenRefreshed";

    public static final String AUTH_USER_REGISTERED_QUEUE =
            "Consumer.AuthService.VirtualTopic.UserRegistered";

    public static final String AUTH_USER_LOGGED_IN_QUEUE =
            "Consumer.AuthService.VirtualTopic.UserLoggedIn";

    public static final String AUTH_TOKEN_REFRESHED_QUEUE =
            "Consumer.AuthService.VirtualTopic.TokenRefreshed";

    private MessagingDestinations() {
    }
}
