package com.project.apex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    @Value("${spring.profiles.active}")
    private String environment;

    @Value("${tradier.clientId}")
    private String clientId;

    @Value("${tradier.clientSecret}")
    private String clientSecret;

    @Value("${tradier.prodClientSecret}")
    private String prodClientSecret;

    @Value("${tradier.apiEndpoint}")
    private String apiEndpoint;

    public String getClientId() {
        return clientId;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public boolean isSandbox() {
        return environment.equals("sandbox");
    }

    public boolean isProduction() {
        return environment.equals("production");
    }

    public boolean isDemo() {
        return environment.equals("demo");
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public String getProdClientSecret() {
        return prodClientSecret;
    }
}
