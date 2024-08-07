package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.config.InitConfig;
import com.project.apex.model.Balance;
import com.project.apex.websocket.ClientWebSocket;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final ClientWebSocket clientWebSocket;
    private final InitConfig initConfig;

    @Autowired
    public AccountService (ClientWebSocket clientWebSocket, InitConfig initConfig) {
        this.clientWebSocket = clientWebSocket;
        this.initConfig = initConfig;
    }

    private String getBaseApi() {
        return "https://api.tradier.com/v1/accounts/" + initConfig.getClientId();
    }

    public Balance getBalance() throws IOException {
        JsonNode json = get("/balances");

        logger.info(json.toString());

        ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode cashNode = new ObjectMapper().readTree(json.toString()).path("balances").path("cash");
        return objectMapper.readValue(cashNode.toString(), Balance.class);
    }

    public JsonNode get(String url) throws IOException {
        System.out.println(getBaseApi() + url);
        HttpUriRequest request = RequestBuilder
                .get(getBaseApi() + url)
                .addHeader("Authorization", "Bearer " + initConfig.getClientSecret())
                .addHeader("Accept", "application/json")
                .build();

        final HttpResponse response = HttpClientBuilder.create().build().execute(request);
        final String jsonString = EntityUtils.toString(response.getEntity());
        return new ObjectMapper().readTree(jsonString);
    }

//    public void sendBalance() throws IOException {
//        final JsonNode json = AccountService.getBalance();
//        clientWebSocket.sendMessageToAll(json.asText());
//    }

//    public Balance getBalance() throws IOException {
//        JsonNode json = get(getBaseApi() + GET_BALANCE);
//
//
//




}
