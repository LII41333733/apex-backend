package com.project.apex.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.config.EnvConfig;
import com.project.apex.service.AccountService;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class HttpRequest {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static RequestBuilder addHeaders(RequestBuilder requestBuilder, EnvConfig envConfig) {
        return requestBuilder
            .addHeader("Authorization", "Bearer " + envConfig.getClientSecret())
            .addHeader("Accept", "application/json");
    }

    public static void addParameters(RequestBuilder request, Map<String, String> parameters) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            request.addParameter(entry.getKey(), entry.getValue());
        }
    }

    public static JsonNode getParsedResponse(HttpResponse response) throws IOException {
        String parsedResponse = EntityUtils.toString(response.getEntity());
        logger.info("HttpRequest.getParsedResponse: {}", parsedResponse);
        return objectMapper.readTree(parsedResponse);
    }
}
