package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.config.EnvConfig;
import com.project.apex.data.Balance;
import com.project.apex.data.QuoteData;
import com.project.apex.data.BuyData;
import com.project.apex.data.SandboxTradeRequest;
import com.project.apex.model.Trade;
import com.project.apex.repository.TradeRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class TradeService {

    private static final Logger logger = LogManager.getLogger(TradeService.class);

    protected final EnvConfig envConfig;
    private final MarketService marketService;
    private final AccountService accountService;
    private final TradeRepository tradeRepository;

    @Autowired
    public TradeService(EnvConfig envConfig,
                        MarketService marketService,
                        AccountService accountService,
                        TradeRepository tradeRepository) {
        this.envConfig = envConfig;
        this.marketService = marketService;
        this.accountService = accountService;
        this.tradeRepository = tradeRepository;
    }

    public List<Trade> fetchTrades() {        List<Trade> trades =  tradeRepository.findAll();
        System.out.println(trades);
        return trades;
    }

    public String placeTrade(BuyData buyData) throws URISyntaxException {
        try {
            Optional<Trade> lastLossTradeEntity = tradeRepository.findLastLossTradeWithoutLossId();
            Trade lastLossTrade = lastLossTradeEntity.orElse(null);

            BigDecimal lastLossPl = BigDecimal.valueOf(0);

            if (lastLossTradeEntity.isPresent()) {
                lastLossPl = lastLossTrade.getPl();
            }

            BigDecimal ask = buyData.getPrice().setScale(2, RoundingMode.UP);
            Balance balance = accountService.getBalanceData();
            BigDecimal totalEquity = balance.getTotalEquity().setScale(2, RoundingMode.UP);
            BigDecimal totalCash = balance.getTotalCash().setScale(2, RoundingMode.UP);
            double tradePercentModifier = 0.03;
            BigDecimal tradeAmount = totalEquity.multiply(BigDecimal.valueOf(tradePercentModifier)).setScale(0, RoundingMode.UP);
            BigDecimal tradeAmountWithLosses = tradeAmount.add(lastLossPl).setScale(0, RoundingMode.UP);

            if (tradeAmountWithLosses.compareTo(totalCash) < 0) {
                Map<String, String> parameters = new HashMap<>();
                BigDecimal contractCost = ask.multiply(BigDecimal.valueOf(100));
                BigDecimal contractsAmount = tradeAmountWithLosses.divide(contractCost, 0, RoundingMode.UP);
                BigDecimal stopPrice = ask.divide(BigDecimal.valueOf(2), 2, RoundingMode.UP);
                BigDecimal targetPrice = ask.multiply(BigDecimal.valueOf(2));
                BigDecimal tradeAmountAfterContracts = contractsAmount.multiply(contractCost);

                System.out.println("TRADE DETAILS ------------------------");
                System.out.println("Symbol: " + buyData.getOption());
                System.out.println("Ask: " + ask);
                System.out.println("Total equity: " + totalEquity);
                System.out.println("Total cash: " + totalCash);
                System.out.println("Recovering Losses: " + lastLossPl);
                System.out.println("Trade amount: " + tradeAmount);
                System.out.println("Trade amount with losses: " + tradeAmountWithLosses);
                System.out.println("Trade amount after contracts: " + tradeAmountAfterContracts);
                System.out.println("Contracts amount: " + contractsAmount);
                System.out.println("Stop Price: " + stopPrice);
                System.out.println("Target Price: " + targetPrice);
                System.out.println("---------------------------------------");


                parameters.put("class", "otoco");
                parameters.put("duration[0]", "day");
                parameters.put("duration[1]", "gtc");
                parameters.put("duration[2]", "gtc");

                parameters.put("quantity[0]", String.valueOf(contractsAmount));
                parameters.put("quantity[1]", String.valueOf(contractsAmount));
                parameters.put("quantity[2]", String.valueOf(contractsAmount));

                parameters.put("side[0]", "buy_to_open");
                parameters.put("side[1]", "sell_to_close");
                parameters.put("side[2]", "sell_to_close");

                parameters.put("option_symbol[0]", buyData.getOption());
                parameters.put("option_symbol[1]", buyData.getOption());
                parameters.put("option_symbol[2]", buyData.getOption());

                parameters.put("price[0]", String.valueOf(ask));
                parameters.put("price[1]", String.valueOf(targetPrice));
                parameters.put("price[2]", String.valueOf(stopPrice));
                parameters.put("stop[2]", String.valueOf(stopPrice));

                parameters.put("type[0]", "limit");
                parameters.put("type[1]", "limit");
                parameters.put("type[2]", "stop");

                String response = accountService.post("/orders", parameters);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response).get("order");

                if (jsonNode == null) {
                    logger.error(response);
                    return response;
                } else {
                    logger.info("Order response: " + jsonNode);
                    String status = jsonNode.get("status").asText();
                    Integer id = jsonNode.get("id").asInt();

                    if (status.equals("ok")) {
                        Trade newTrade = new Trade();
                        newTrade.setOrderId(id);
                        newTrade.setTradeAmount(tradeAmountAfterContracts);
                        newTrade.setBalance(totalEquity);

                        if (lastLossTradeEntity.isPresent()) {
                            lastLossTrade.setLossId(id);
                            newTrade.setRecoveryId(lastLossTrade.getOrderId());
                            tradeRepository.save(lastLossTrade);
                        }

                        tradeRepository.save(newTrade);
                        return "Order returned ok";
                    } else {
                        return "Order returned not ok";
                    }
                }
            } else {
                logger.warn("Not enough cash available to make trade");
                return "Not enough cash available to make trade";
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.error(e);
            return e.getMessage();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.error(e);
            return e.getMessage();
        }
    }
}

// Positions, Options Chain, Orders, Trades
