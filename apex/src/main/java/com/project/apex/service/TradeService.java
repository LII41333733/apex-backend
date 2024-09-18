package com.project.apex.service;

import com.project.apex.config.EnvConfig;
import com.project.apex.data.trades.BuyData;
import com.project.apex.data.trades.RiskType;
import com.project.apex.model.BaseTrade;
import com.project.apex.repository.BaseTradeRepository;
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
import java.util.*;

@Service
public class TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    protected final EnvConfig envConfig;
    private final MarketService marketService;
    private final AccountService accountService;
    private final BaseTradeRepository baseTradeRepository;
    private final BaseTradeService baseTradeService;

    @Autowired
    public TradeService(EnvConfig envConfig,
                        MarketService marketService,
                        AccountService accountService,
                        BaseTradeRepository baseTradeRepository,
                        BaseTradeService baseTradeService) {
        this.envConfig = envConfig;
        this.marketService = marketService;
        this.accountService = accountService;
        this.baseTradeRepository = baseTradeRepository;
        this.baseTradeService = baseTradeService;
    }

    public List<BaseTrade> fetchTrades() {
        return baseTradeRepository.findAll();
    }

    public void cancelTrade(String orderId) throws IOException {
        String url = envConfig.getApiEndpoint() + "/v1/accounts/" + envConfig.getClientId() + "/orders/" + orderId;
        System.out.println(url);
        System.out.println(url);
        HttpUriRequest request = RequestBuilder
                .delete(url)
                .addHeader("Authorization", "Bearer " + envConfig.getClientSecret())
                .addHeader("Accept", "application/json")
                .build();

        final HttpResponse response = HttpClientBuilder.create().build().execute(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);
    }

    public void placeTrade(BuyData buyData) {
        try {
            switch (RiskType.valueOf(buyData.getRiskType())) {
                case LOTTO -> handleLottoTrade(buyData);
                case OTOCO -> handleOtocoTrade(buyData);
            };
        } catch (Exception e) {
            logger.error("placeTrade", e);
        }
    }

    public String handleLottoTrade(BuyData buyData) {
        return "";
    }
    public String handleOtocoTrade(BuyData buyData) {
        return "";
    }

//    public String handleLottoTrade(BuyData buyData) throws IOException {
//        Optional<Trade> lastLossTradeEntity = baseTradeRepository.findLastLossTradeWithoutLossId();
//        Trade lastLossTrade = lastLossTradeEntity.orElse(null);
//
//        Double lastLossPl = Double.valueOf(0);
//
//        if (lastLossTradeEntity.isPresent()) {
//            lastLossPl = lastLossTrade.getPl();
//        }
//
//        Double ask = buyData.getPrice().setScale(2, RoundingMode.UP);
//        Balance balance = accountService.getBalanceData();
//        Double totalEquity = balance.getTotalEquity().setScale(2, RoundingMode.HALF_UP);
//        Double totalCash = balance.getTotalCash().setScale(2, RoundingMode.HALF_UP);
//        double tradePercentModifier = 0.03;
//        Double tradeAmount = totalEquity.multiply(Double.valueOf(tradePercentModifier)).setScale(0, RoundingMode.UP);
//        Double tradeAmountWithLosses = tradeAmount.add(lastLossPl.abs()).setScale(0, RoundingMode.UP);
//
//        if (tradeAmountWithLosses.compareTo(totalCash) < 0) {
//            Map<String, String> parameters = new HashMap<>();
//            Double contractCost = ask.multiply(Double.valueOf(100));
//            Double contractsAmount = tradeAmountWithLosses.divide(contractCost, 0, RoundingMode.UP);
//            Double stopPrice = ask.divide(Double.valueOf(2), 2, RoundingMode.UP);
//            Double targetPrice = ask.multiply(Double.valueOf(2));
//            Double tradeAmountAfterContracts = contractsAmount.multiply(contractCost);
//
//            System.out.println("TRADE DETAILS ------------------------");
//            System.out.println("Symbol: " + buyData.getOption());
//            System.out.println("Ask: " + ask);
//            System.out.println("Total equity: " + totalEquity);
//            System.out.println("Total cash: " + totalCash);
//            System.out.println("Recovering Losses: " + lastLossPl);
//            System.out.println("Trade amount: " + tradeAmount);
//            System.out.println("Trade amount with losses: " + tradeAmountWithLosses);
//            System.out.println("Trade amount after contracts: " + tradeAmountAfterContracts);
//            System.out.println("Contracts amount: " + contractsAmount);
//            System.out.println("Stop Price: " + stopPrice);
//            System.out.println("Target Price: " + targetPrice);
//            System.out.println("---------------------------------------");
//
//
//            parameters.put("class", "otoco");
//            parameters.put("duration[0]", "day");
//            parameters.put("duration[1]", "gtc");
//            parameters.put("duration[2]", "gtc");
//
//            parameters.put("quantity[0]", String.valueOf(contractsAmount));
//            parameters.put("quantity[1]", String.valueOf(contractsAmount));
//            parameters.put("quantity[2]", String.valueOf(contractsAmount));
//
//            parameters.put("side[0]", "buy_to_open");
//            parameters.put("side[1]", "sell_to_close");
//            parameters.put("side[2]", "sell_to_close");
//
//            parameters.put("option_symbol[0]", buyData.getOption());
//            parameters.put("option_symbol[1]", buyData.getOption());
//            parameters.put("option_symbol[2]", buyData.getOption());
//
//            parameters.put("price[0]", String.valueOf(ask));
//            parameters.put("price[1]", String.valueOf(targetPrice));
//            parameters.put("price[2]", String.valueOf(stopPrice));
//            parameters.put("stop[2]", String.valueOf(stopPrice));
//
//            parameters.put("type[0]", "limit");
//            parameters.put("type[1]", "limit");
//            parameters.put("type[2]", "stop");
//
//            String response = accountService.post("/orders", parameters);
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode jsonNode = objectMapper.readTree(response).get("order");
//
//            if (jsonNode == null) {
//                logger.error(response);
//                return response;
//            } else {
//                logger.info("Order response: " + jsonNode);
//                String status = jsonNode.get("status").asText();
//                Integer id = jsonNode.get("id").asInt();
//
//                if (status.equals("ok")) {
//                    Trade newTrade = new Trade();
//                    newTrade.setOrderId(id);
//                    newTrade.setTradeAmount(tradeAmountAfterContracts);
//                    newTrade.setBalance(totalEquity);
//
//                    if (lastLossTradeEntity.isPresent()) {
//                        lastLossTrade.setLossId(id);
//                        newTrade.setRecoveryId(lastLossTrade.getOrderId());
//                        baseTradeRepository.save(lastLossTrade);
//                    }
//
//                    baseTradeRepository.save(newTrade);
//                    return "Order returned ok";
//                } else {
//                    return "Order returned not ok";
//                }
//            }
//        } else {
//            logger.warn("Not enough cash available to make trade");
//            return "Not enough cash available to make trade";
//        }
//    }
//
//    public String handleRecoveryTrade(BuyData buyData) throws IOException {
//        Optional<Trade> lastLossTradeEntity = baseTradeRepository.findLastLossTradeWithoutLossId();
//        Trade lastLossTrade = lastLossTradeEntity.orElse(null);
//
//        Double lastLossPl = Double.valueOf(0);
//
//        if (lastLossTradeEntity.isPresent()) {
//            lastLossPl = lastLossTrade.getPl();
//        }
//
//        Double ask = buyData.getPrice().setScale(2, RoundingMode.UP);
//        Balance balance = accountService.getBalanceData();
//        Double totalEquity = balance.getTotalEquity().setScale(2, RoundingMode.HALF_UP);
//        Double totalCash = balance.getTotalCash().setScale(2, RoundingMode.HALF_UP);
//        double tradePercentModifier = 0.03;
//        Double tradeAmount = totalEquity.multiply(Double.valueOf(tradePercentModifier)).setScale(0, RoundingMode.UP);
//        Double tradeAmountWithLosses = tradeAmount.add(lastLossPl.abs()).setScale(0, RoundingMode.UP);
//
//        if (tradeAmountWithLosses.compareTo(totalCash) < 0) {
//            Map<String, String> parameters = new HashMap<>();
//            Double contractCost = ask.multiply(Double.valueOf(100));
//            Double contractsAmount = tradeAmountWithLosses.divide(contractCost, 0, RoundingMode.UP);
//            Double stopPrice = ask.divide(Double.valueOf(2), 2, RoundingMode.UP);
//            Double targetPrice = ask.multiply(Double.valueOf(2));
//            Double tradeAmountAfterContracts = contractsAmount.multiply(contractCost);
//
//            System.out.println("TRADE DETAILS ------------------------");
//            System.out.println("Symbol: " + buyData.getOption());
//            System.out.println("Ask: " + ask);
//            System.out.println("Total equity: " + totalEquity);
//            System.out.println("Total cash: " + totalCash);
//            System.out.println("Recovering Losses: " + lastLossPl);
//            System.out.println("Trade amount: " + tradeAmount);
//            System.out.println("Trade amount with losses: " + tradeAmountWithLosses);
//            System.out.println("Trade amount after contracts: " + tradeAmountAfterContracts);
//            System.out.println("Contracts amount: " + contractsAmount);
//            System.out.println("Stop Price: " + stopPrice);
//            System.out.println("Target Price: " + targetPrice);
//            System.out.println("---------------------------------------");
//
//
//            parameters.put("class", "otoco");
//            parameters.put("duration[0]", "day");
//            parameters.put("duration[1]", "gtc");
//            parameters.put("duration[2]", "gtc");
//
//            parameters.put("quantity[0]", String.valueOf(contractsAmount));
//            parameters.put("quantity[1]", String.valueOf(contractsAmount));
//            parameters.put("quantity[2]", String.valueOf(contractsAmount));
//
//            parameters.put("side[0]", "buy_to_open");
//            parameters.put("side[1]", "sell_to_close");
//            parameters.put("side[2]", "sell_to_close");
//
//            parameters.put("option_symbol[0]", buyData.getOption());
//            parameters.put("option_symbol[1]", buyData.getOption());
//            parameters.put("option_symbol[2]", buyData.getOption());
//
//            parameters.put("price[0]", String.valueOf(ask));
//            parameters.put("price[1]", String.valueOf(targetPrice));
//            parameters.put("price[2]", String.valueOf(stopPrice));
//            parameters.put("stop[2]", String.valueOf(stopPrice));
//
//            parameters.put("type[0]", "limit");
//            parameters.put("type[1]", "limit");
//            parameters.put("type[2]", "stop");
//
//            String response = accountService.post("/orders", parameters);
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode jsonNode = objectMapper.readTree(response).get("order");
//
//            if (jsonNode == null) {
//                logger.error(response);
//                return response;
//            } else {
//                logger.info("Order response: " + jsonNode);
//                String status = jsonNode.get("status").asText();
//                Integer id = jsonNode.get("id").asInt();
//
//                if (status.equals("ok")) {
//                    Trade newTrade = new Trade();
//                    newTrade.setOrderId(id);
//                    newTrade.setTradeAmount(tradeAmountAfterContracts);
//                    newTrade.setBalance(totalEquity);
//
//                    if (lastLossTradeEntity.isPresent()) {
//                        lastLossTrade.setLossId(id);
//                        newTrade.setRecoveryId(lastLossTrade.getOrderId());
//                        baseTradeRepository.save(lastLossTrade);
//                    }
//
//                    baseTradeRepository.save(newTrade);
//                    return "Order returned ok";
//                } else {
//                    return "Order returned not ok";
//                }
//            }
//        } else {
//            logger.warn("Not enough cash available to make trade");
//            return "Not enough cash available to make trade";
//        }
//    }
}

// Positions, Options Chain, Orders, Trades
