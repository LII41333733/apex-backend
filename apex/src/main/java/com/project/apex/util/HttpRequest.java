package com.project.apex.util;

import com.project.apex.config.EnvConfig;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

public class HttpRequest {
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

    public static String getParsedResponse(HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }
}
