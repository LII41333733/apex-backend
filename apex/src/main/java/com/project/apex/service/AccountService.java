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
import java.util.Map;

import static com.project.apex.util.HttpRequest.*;

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

    public JsonNode get(String url) throws IOException {
        logger.info("AccountService.get: Start: Url: {}", getBaseApi() + url);

        try {
            HttpUriRequest request = addHeaders(RequestBuilder.get(getBaseApi() + url), envConfig).build();
            HttpResponse response = HttpClientBuilder.create().build().execute(request);
            return getParsedResponse(response);
        } catch (IOException e) {
            logger.error("AccountService.get: ERROR: IOException:", e);
            throw e;
        } catch (Exception e) {
            logger.error("AccountService.get: ERROR: Exception: {}", e.getMessage(), e);
            throw new IOException(e);
        }
    }

    public JsonNode post(String url, Map<String, String> parameters) throws IOException {
        logger.info("AccountService.post: Start: Url: {} Parameters: {}", getBaseApi() + url, parameters);

        try {
            RequestBuilder request = addHeaders(RequestBuilder.post(getBaseApi() + url), envConfig);
            addParameters(request, parameters);
            HttpResponse response = HttpClientBuilder.create().build().execute(request.build());
            return getParsedResponse(response);
        } catch (IOException e) {
            logger.error("AccountService.post: ERROR: IOException:", e);
            throw e;
        } catch (Exception e) {
            logger.error("AccountService.post: ERROR: Exception: {}", e.getMessage(), e);
            throw new IOException(e);
        }
    }

    public JsonNode put(String url, Map<String, String> parameters) throws IOException {
        logger.info("AccountService.put: Start: Url: {} Parameters: {}", getBaseApi() + url, parameters);

        try {
            RequestBuilder request = addHeaders(RequestBuilder.put(getBaseApi() + url), envConfig);
            addParameters(request, parameters);
            HttpResponse response = HttpClientBuilder.create().build().execute(request.build());
            return getParsedResponse(response);
        } catch (IOException e) {
            logger.error("AccountService.put: ERROR: IOException:", e);
            throw e;
        } catch (Exception e) {
            logger.error("AccountService.put: ERROR: Exception: {}", e.getMessage(), e);
            throw new IOException(e);
        }
    }

    public Balance getBalanceData() throws IOException {
        logger.info("AccountService.getBalanceData: Retrieving balance data");
        Balance balance = new Balance();
        JsonNode balancesJson = get("/balances");
        JsonNode balances = balancesJson.get("balances");

        if (balances != null) {
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
        } else {
            logger.info("AccountService.getBalanceData: Balance Data returned null");
            return balance;
        }
    }

    public void addNewAccountBalance(AccountBalance accountBalance) throws IOException {
        accountBalanceRepository.save(accountBalance);
    }
}
