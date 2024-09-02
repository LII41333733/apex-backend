package com.project.apex.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.component.AccountState;
import com.project.apex.config.EnvConfig;
import com.project.apex.data.AccountBalance;
import com.project.apex.data.Balance;
import com.project.apex.data.Order;
import com.project.apex.repository.AccountBalanceRepository;
import com.project.apex.component.ClientWebSocket;
import jakarta.annotation.PostConstruct;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;


import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final ClientWebSocket clientWebSocket;
    private final EnvConfig envConfig;
    private final AccountBalanceRepository accountBalanceRepository;
    private final AccountState accountState;
    private final MarketService marketService;

    @Autowired
    public AccountService (
            ClientWebSocket clientWebSocket,
            EnvConfig envConfig,
            AccountBalanceRepository accountBalanceRepoitory,
            AccountState accountState,
            MarketService marketService
    ) {
        this.clientWebSocket = clientWebSocket;
        this.envConfig = envConfig;
        this.accountBalanceRepository = accountBalanceRepoitory;
        this.accountState = accountState;
        this.marketService = marketService;
    }

    public void init() throws IOException {
//        Balance balanceData = getBalanceData();
//        accountState.setBalanceData(balanceData);
//        accountStream.startStream();
    }

    private String getBaseApi() {
        return envConfig.getApiEndpoint() + "/v1/accounts/" + envConfig.getClientId();
    }

    public String get(String url) throws IOException {
        HttpUriRequest request = RequestBuilder
                .get(getBaseApi() + url)
                .addHeader("Authorization", "Bearer " + envConfig.getClientSecret())
                .addHeader("Accept", "application/json")
                .build();

        final HttpResponse response = HttpClientBuilder.create().build().execute(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        return responseBody;
    }

    public String post(String url, Map<String, String> parameters) throws IOException {
        RequestBuilder request = RequestBuilder
                .post(getBaseApi() + url)
                .addHeader("Authorization", "Bearer " + envConfig.getClientSecret())
                .addHeader("Accept", "application/json");
//                .setEntity(entity)
//                .build();

        // Iterate over the map and add each parameter
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            request.addParameter(entry.getKey(), entry.getValue());
        }

        final HttpResponse response = HttpClientBuilder.create().build().execute(request.build());
        return EntityUtils.toString(response.getEntity());
    }

    public Balance getBalanceData() throws IOException {
        logger.info("Retrieving balance data");
        Balance balance = new Balance();
        JsonNode balances = new ObjectMapper().readTree(get("/balances")).get("balances");

        if (envConfig.isSandbox()) {
            BigDecimal totalCash = new BigDecimal(balances.get("total_cash").asText()).setScale(0, RoundingMode.HALF_UP);
            balance.setUnsettledFunds(new BigDecimal(0));
            balance.setCashAvailable(totalCash);
            balance.setTotalCash(totalCash);
        } else {
            balance.setUnsettledFunds(new BigDecimal(balances.get("cash").get("unsettled_funds").asText()).setScale(2, RoundingMode.HALF_UP));
            balance.setCashAvailable(new BigDecimal(balances.get("cash").get("cash_available").asText()).setScale(2, RoundingMode.HALF_UP));
            balance.setTotalCash(balance.getCashAvailable());
        }

        balance.setTotalEquity(new BigDecimal(balances.get("total_equity").asText()).setScale(0, RoundingMode.HALF_UP));
        balance.setMarketValue(new BigDecimal(balances.get("market_value").asText()).setScale(2, RoundingMode.HALF_UP));
        balance.setOpenPl(new BigDecimal(balances.get("open_pl").asText()).setScale(2, RoundingMode.HALF_UP));
        balance.setClosePl(new BigDecimal(balances.get("close_pl").asText()).setScale(2, RoundingMode.HALF_UP));
        balance.setPendingCash(new BigDecimal(balances.get("pending_cash").asText()).setScale(2, RoundingMode.HALF_UP));
        balance.setUnclearedFunds(new BigDecimal(balances.get("uncleared_funds").asText()).setScale(2, RoundingMode.HALF_UP));
        return balance;
    }

    // Order
    public String getOrder(String id) throws IOException {
        logger.info("Retrieving order data");
//        Balance balanceData = new Balance();
        JsonNode order = new ObjectMapper().readTree(get("/orders/" + id)).get("order");
        return order.toString();
    }

    public String getOrders() throws IOException {
        logger.trace("Retrieving orders data");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode response = new ObjectMapper().readTree(get("/orders")).get("orders");

        if (response.asText().equals("null")) {
            throw new IOException("Orders is null");
        }

        List<Order> orders = objectMapper.readValue(objectMapper.readTree(response.toString()).get("order").toString(), new TypeReference<>() {});

        for (Order order : orders) {
            System.out.println(order.getStatus());
        }

        return orders.toString();
    }

    public void addNewAccountBalance(AccountBalance accountBalance) throws IOException {
        accountBalanceRepository.save(accountBalance);
    }

//    @Scheduled(fixedRate = 10000)
//    public void fetchOrdersSchedule() {
//        if (!clientWebSocket.isConnected() && !ordersAreEmpty) {
//            fetchOrders();
//        }
//    }

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
