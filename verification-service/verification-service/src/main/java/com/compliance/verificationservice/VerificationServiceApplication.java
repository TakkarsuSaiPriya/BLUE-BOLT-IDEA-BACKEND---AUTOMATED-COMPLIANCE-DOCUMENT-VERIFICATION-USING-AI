package com.compliance.verificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication(
        exclude = UserDetailsServiceAutoConfiguration.class
)
@EnableDiscoveryClient
@EnableFeignClients
@EnableJms
public class VerificationServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                VerificationServiceApplication.class,
                args
        );
    }
}