package com.compliance.verificationservice.config;

import com.compliance.verificationservice.security.CustomAccessDeniedHandler;
import com.compliance.verificationservice.security.CustomAuthenticationEntryPoint;
import com.compliance.verificationservice.security.InternalServiceKeyFilter;
import com.compliance.verificationservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    private final InternalServiceKeyFilter
            internalServiceKeyFilter;

    private final CustomAuthenticationEntryPoint
            authenticationEntryPoint;

    private final CustomAccessDeniedHandler
            accessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            InternalServiceKeyFilter internalServiceKeyFilter,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.internalServiceKeyFilter =
                internalServiceKeyFilter;

        this.authenticationEntryPoint =
                authenticationEntryPoint;

        this.accessDeniedHandler =
                accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http
                .csrf(
                        csrf ->
                                csrf.disable()
                )
                .cors(
                        cors -> {
                        }
                )
                .httpBasic(
                        httpBasic ->
                                httpBasic.disable()
                )
                .formLogin(
                        formLogin ->
                                formLogin.disable()
                )
                .logout(
                        logout ->
                                logout.disable()
                )
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )
                .exceptionHandling(
                        exceptionHandling ->
                                exceptionHandling
                                        .authenticationEntryPoint(
                                                authenticationEntryPoint
                                        )
                                        .accessDeniedHandler(
                                                accessDeniedHandler
                                        )
                )
                .authorizeHttpRequests(
                        authorization ->
                                authorization

                                        .requestMatchers(
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                "/actuator/health",
                                                "/actuator/info"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                "/api/internal/**"
                                        )
                                        .hasRole(
                                                "INTERNAL_SERVICE"
                                        )

                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/verification-audit-logs/**"
                                        )
                                        .hasAnyRole(
                                                "REVIEWER",
                                                "ADMIN"
                                        )

                                        .requestMatchers(
                                                HttpMethod.PATCH,
                                                "/api/verifications/*/review"
                                        )
                                        .hasAnyRole(
                                                "REVIEWER",
                                                "ADMIN"
                                        )

                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/verifications/summary"
                                        )
                                        .hasAnyRole(
                                                "REVIEWER",
                                                "ADMIN"
                                        )

                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/verifications/manual"
                                        )
                                        .hasAnyRole(
                                                "USER",
                                                "REVIEWER",
                                                "ADMIN"
                                        )

                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/verifications/*/retry"
                                        )
                                        .hasAnyRole(
                                                "USER",
                                                "REVIEWER",
                                                "ADMIN"
                                        )

                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/verifications/**"
                                        )
                                        .hasAnyRole(
                                                "USER",
                                                "REVIEWER",
                                                "ADMIN"
                                        )

                                        .requestMatchers(
                                                "/actuator/**"
                                        )
                                        .hasRole(
                                                "ADMIN"
                                        )

                                        .anyRequest()
                                        .authenticated()
                )
                .addFilterBefore(
                        internalServiceKeyFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}