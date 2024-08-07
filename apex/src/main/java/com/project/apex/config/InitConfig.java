package com.project.apex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitConfig {

    @Value("${tradier.clientId}")
    private String clientId;

    @Value("${tradier.clientSecret}")
    private String clientSecret;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
