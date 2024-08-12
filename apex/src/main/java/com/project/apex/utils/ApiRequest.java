package com.project.apex.utils;

import com.project.apex.config.InitConfig;
import com.project.apex.service.MarketService;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

@Component
public class ApiRequest {

    private static final Logger logger = LogManager.getLogger(ApiRequest.class);

    private static String clientSecret;

    @Autowired
    public ApiRequest(Environment environment) {
        ApiRequest.clientSecret = environment.getProperty("tradier.clientSecret");
    }
    /**
     * Builds an HTTP GET request with authorization and query parameters.
     *
     * @param url            The URL for the request
     * @param queryParams    A map of query parameters
     * @return An HttpUriRequest object
     * @throws URISyntaxException if the URI syntax is incorrect
     */
    public static String get(String url, Map<String, String> queryParams) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }

        HttpUriRequest request = RequestBuilder.get(uriBuilder.build())
                .addHeader("Authorization", "Bearer " + ApiRequest.clientSecret)
                .addHeader("Accept", "application/json")
                .build();

        final HttpResponse response = HttpClientBuilder.create().build().execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    public static String post(String url, Map<String, String> queryParams) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }

        HttpUriRequest request = RequestBuilder.get(uriBuilder.build())
                .addHeader("Authorization", "Bearer " + ApiRequest.clientSecret)
                .addHeader("Accept", "application/json")
                .build();

        final HttpResponse response = HttpClientBuilder.create().build().execute(request);
        return EntityUtils.toString(response.getEntity());
    }
}


