package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.config.EnvConfig;
import com.project.apex.data.account.AccountBalance;
import com.project.apex.data.account.Balance;
import com.project.apex.repository.AccountBalanceRepository;
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
import java.math.RoundingMode;
import java.util.Map;

@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final EnvConfig envConfig;
    private final AccountBalanceRepository accountBalanceRepository;

    @Autowired
    public AccountService (EnvConfig envConfig, AccountBalanceRepository accountBalanceRepoitory
    ) {
        this.envConfig = envConfig;
        this.accountBalanceRepository = accountBalanceRepoitory;
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
        return EntityUtils.toString(response.getEntity());
    }

    public String post(String url, Map<String, String> parameters) throws IOException {
        RequestBuilder request = RequestBuilder
                .post(getBaseApi() + url)
                .addHeader("Authorization", "Bearer " + envConfig.getClientSecret())
                .addHeader("Accept", "application/json");

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            request.addParameter(entry.getKey(), entry.getValue());
        }

        final HttpResponse response = HttpClientBuilder.create().build().execute(request.build());
        return EntityUtils.toString(response.getEntity());
    }

    public String put(String url, Map<String, String> parameters) throws IOException {
        RequestBuilder request = RequestBuilder
                .put(getBaseApi() + url)
                .addHeader("Authorization", "Bearer " + envConfig.getClientSecret())
                .addHeader("Accept", "application/json");

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
            Double totalCash = balances.get("total_cash").asDouble();
            balance.setUnsettledFunds((double) 0);
            balance.setCashAvailable(totalCash);
            balance.setTotalCash(totalCash);
        } else {
            balance.setUnsettledFunds(balances.get("cash").get("unsettled_funds").asDouble());
            balance.setCashAvailable(balances.get("cash").get("cash_available").asDouble());
            balance.setTotalCash(balance.getCashAvailable());
        }

        balance.setTotalEquity(balances.get("total_equity").asDouble());
        balance.setMarketValue(balances.get("market_value").asDouble());
        balance.setOpenPl(balances.get("open_pl").asDouble());
        balance.setClosePl(balances.get("close_pl").asDouble());
        balance.setPendingCash(balances.get("pending_cash").asDouble());
        balance.setUnclearedFunds(balances.get("uncleared_funds").asDouble());
        return balance;
    }

    public void addNewAccountBalance(AccountBalance accountBalance) throws IOException {
        accountBalanceRepository.save(accountBalance);
    }
}
