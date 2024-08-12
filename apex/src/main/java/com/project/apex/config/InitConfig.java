package com.project.apex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitConfig {

    @Value("DEV")
    private String environment;

    @Value("${tradier.clientId}")
    private String clientId;

    @Value("${tradier.clientSecret}")
    private String clientSecret;

    public String getClientId() {
        return clientId;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public boolean isMock() {
        return environment.equals("MOCK");
    }

    public boolean isDev() {
        return environment.equals("DEV");
    }
}
