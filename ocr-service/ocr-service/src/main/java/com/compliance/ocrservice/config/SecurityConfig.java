package com.compliance.ocrservice.config;

import com.compliance.ocrservice.security.JwtAccessDeniedHandler;
import com.compliance.ocrservice.security.JwtAuthenticationEntryPoint;
import com.compliance.ocrservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    private final JwtAuthenticationEntryPoint
            jwtAuthenticationEntryPoint;

    private final JwtAccessDeniedHandler
            jwtAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.jwtAuthenticationEntryPoint =
                jwtAuthenticationEntryPoint;

        this.jwtAccessDeniedHandler =
                jwtAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf ->
                        csrf.disable()
                )
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        jwtAuthenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        jwtAccessDeniedHandler
                                )
                )
                .authorizeHttpRequests(authorize ->
                        authorize
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
                                        HttpMethod.OPTIONS,
                                        "/**"
                                )
                                .permitAll()

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/ocr/process"
                                )
                                .hasAnyRole(
                                        "USER",
                                        "ADMIN",
                                        "VERIFIER"
                                )

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/ocr/results/*/retry"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "VERIFIER"
                                )

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/ocr/results/status/**",
                                        "/api/ocr/results/summary"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "VERIFIER",
                                        "AUDITOR"
                                )

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/ocr/results/**"
                                )
                                .hasAnyRole(
                                        "USER",
                                        "ADMIN",
                                        "VERIFIER",
                                        "AUDITOR"
                                )

                                .requestMatchers(
                                        "/api/ocr/**"
                                )
                                .authenticated()

                                .anyRequest()
                                .authenticated()
                )
                .formLogin(form ->
                        form.disable()
                )
                .httpBasic(basic ->
                        basic.disable()
                )
                .logout(logout ->
                        logout.disable()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOriginPatterns(
                List.of(
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        HttpHeaders.AUTHORIZATION,
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.ACCEPT,
                        "X-Requested-With"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        HttpHeaders.CONTENT_DISPOSITION,
                        HttpHeaders.CONTENT_LENGTH
                )
        );

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}