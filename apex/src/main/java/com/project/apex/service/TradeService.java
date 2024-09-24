package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.config.EnvConfig;
import com.project.apex.data.account.Balance;
import com.project.apex.data.orders.OrderFillRecord;
import com.project.apex.data.trades.BuyData;
import com.project.apex.data.trades.ModifyTradeRecord;
import com.project.apex.data.trades.RiskType;
import com.project.apex.data.trades.TradeLeg;
import com.project.apex.model.BaseTrade;
import com.project.apex.model.LottoTrade;
import com.project.apex.model.Trade;
import com.project.apex.repository.BaseTradeRepository;
import com.project.apex.util.Convert;
import com.project.apex.util.Record;
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

import static com.project.apex.util.TradeOrder.isOk;

@Service
public class TradeService<R> {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    protected final EnvConfig envConfig;
    private final AccountService accountService;

    @Autowired
    public TradeService(EnvConfig envConfig,
                        AccountService accountService,
                        BaseTradeRepository baseTradeRepository) {
        this.envConfig = envConfig;
        this.accountService = accountService;
    }


    public List<BaseTrade> fetchTrades() {
        return baseTradeRepository.findAll();
    }

    public void cancelTrade(String id) throws IOException {
        JsonNode response = accountService.delete("/orders/" + id);
        System.out.println(response);
    }

    public void placeFill(BuyData buyData) {
        try {
            Long id = Convert.getMomentAsCode();
            logger.info("LottoTradeService.placeFill: Start: {}", id);
            Balance balance = accountService.getBalanceData();
            double totalEquity = balance.getTotalEquity();
            logger.info("Total Equity: {}", totalEquity);
            double totalCash = balance.getTotalCash();
            logger.info("Total Cash: {}", totalCash);
            int tradeAllotment = (int) Math.floor(totalEquity * LottoTrade.tradePercentModifier);
            logger.info("Trade Allotment: {}", tradeAllotment);
            logger.info("Trade Allotment < Total Cash: {}", tradeAllotment < totalCash);
            if (tradeAllotment < totalCash) {
                double ask = buyData.getPrice();
                logger.info("Ask: {}", ask);
                double contractCost = ask * 100;
                logger.info("Contract Cost: {}", contractCost);
                int quantity = (int) Math.floor(tradeAllotment /contractCost);
                logger.info("Quantity: {}", quantity);
                Map<String, String> parameters = new HashMap<>();
                parameters.put("class", "option");
                parameters.put("duration", "day");
                parameters.put("quantity", String.valueOf(quantity));
                parameters.put("side", "buy_to_open");
                parameters.put("option_symbol", buyData.getOption());
                parameters.put("price", String.valueOf(ask));
                parameters.put("type", "limit");
                parameters.put("tag", buyData.getRiskType().toUpperCase() + "-" + id + "-" +  TradeLeg.FILL);

                new Record<>("LottoTradeService.placeFill: Fill Parameters", new OrderFillRecord(
                        id,
                        totalEquity,
                        totalCash,
                        tradeAllotment,
                        buyData.getPrice(),
                        contractCost,
                        quantity,
                        parameters
                ));

                JsonNode json = accountService.post("/orders", parameters);
                JsonNode jsonNode = json.get("order");

                if (isOk(jsonNode)) {
                    logger.info("LottoTradeService.placeFill: Fill Successful: {}", id);
                    Long orderId = jsonNode.get("id").asLong();
                    LottoTrade trade = new LottoTrade(id, totalEquity, ask, quantity, orderId);
                    lottoTradeRepository.save(trade);
                } else {
                    logger.error("LottoTradeService.placeFill: Fill UnSuccessful: {}", id);
                }
            } else {
                logger.error("LottoTradeService.placeFill: Not enough cash available to make trade: {}", id);
            }
        } catch (Exception e) {
            logger.error("LottoTradeService.placeFill: ERROR: Exception", e);
        }
    }

//    public void modifyStopOrder(Integer orderId, Double newPrice, BaseTrade trade) {
//        logger.info("BaseTradeService.modifyStopOrder: Start: ID: {} Order ID: {} New Price: {}", trade.getId(), orderId, newPrice);
//        try {
//            Map<String, String> parameters = new HashMap<>();
//            parameters.put("stop[0]", newPrice.toString());
//            parameters.put("limit[0]", newPrice.toString());
//
//            JsonNode response = accountService.put("/orders/" + orderId, parameters);
//
//            if (isOk(response)) {
//                logger.info("BaseTradeService.modifyStopOrder: Modify Stop Successful");
//            } else {
//                logger.error("BaseTradeService.modifyStopOrder: Modify Stop UnSuccessful");
//            }
//        } catch (Exception e) {
//            logger.error("BaseTradeService.modifyStopOrder: ERROR: Exception: {}", e.getMessage(), e);
//        }
//    }

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
