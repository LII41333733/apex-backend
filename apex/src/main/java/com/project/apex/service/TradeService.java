package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.config.EnvConfig;
import com.project.apex.data.Balance;
import com.project.apex.data.QuoteData;
import com.project.apex.data.BuyData;
import com.project.apex.data.SandboxTradeRequest;
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

    @Autowired
    public TradeService(EnvConfig envConfig, MarketService marketService, AccountService accountService) {
        this.envConfig = envConfig;
        this.marketService = marketService;
        this.accountService = accountService;
    }

    public void placeTrade(BuyData buyData) {
        System.out.println(buyData.toString());
    }

    public String placeSandboxTrade(SandboxTradeRequest sandboxTradeRequest) throws URISyntaxException {
        try {
            List<QuoteData> optionsChain = marketService.getOptionsChain(sandboxTradeRequest.getSymbol(), sandboxTradeRequest.getOptionType());
            BigDecimal ask = optionsChain.get(0).getAsk();
            String symbol = optionsChain.get(0).getSymbol();
            Balance balance = accountService.getBalanceData();
            BigDecimal totalEquity = balance.getTotalEquity().setScale(0, RoundingMode.UP);
            BigDecimal totalCash = balance.getTotalCash().setScale(0, RoundingMode.UP);
            double tradePercentModifier = 0.03;
            BigDecimal tradeAmount = totalEquity.multiply(BigDecimal.valueOf(tradePercentModifier)).setScale(0, RoundingMode.UP);

            if (tradeAmount.compareTo(totalCash) < 0) {
                BigDecimal contractsAmount = tradeAmount.divide(ask.multiply(new BigDecimal(100)), 0, RoundingMode.CEILING);
                BigDecimal stopPrice = ask.divide(BigDecimal.valueOf(2), 2, RoundingMode.UP);
                BigDecimal targetPrice = ask.multiply(BigDecimal.valueOf(2).setScale(0, RoundingMode.UP));

                System.out.println("TRADE DETAILS ------------------------");
                System.out.println("Symbol: " + symbol);
                System.out.println("Ask: " + ask);
                System.out.println("Total equity: " + totalEquity);
                System.out.println("Total cash: " + totalCash);
                System.out.println("Trade amount: " + tradeAmount);
                System.out.println("Contracts amount: " + contractsAmount);
                System.out.println("Stop Price: " + stopPrice);
                System.out.println("Target Price: " + targetPrice);
                System.out.println("---------------------------------------");

                Map<String, String> parameters = new HashMap<>();
                parameters.put("class", "otoco");
                parameters.put("duration", "day");

                parameters.put("quantity[0]", String.valueOf(contractsAmount));
                parameters.put("quantity[1]", String.valueOf(contractsAmount));
                parameters.put("quantity[2]", String.valueOf(contractsAmount));

                parameters.put("side[0]", "buy_to_open");
                parameters.put("side[1]", "sell_to_close");
                parameters.put("side[2]", "sell_to_close");

                parameters.put("option_symbol[0]", "SPY240826C00562000");
                parameters.put("option_symbol[1]", "SPY240826C00562000");
                parameters.put("option_symbol[2]", "SPY240826C00562000");

                parameters.put("price[0]", String.valueOf(ask));
                parameters.put("price[1]", String.valueOf(targetPrice));
                parameters.put("price[2]", String.valueOf(stopPrice));
                parameters.put("stop[2]", String.valueOf(stopPrice));

                parameters.put("type[0]", "limit");
                parameters.put("type[1]", "limit");
                parameters.put("type[2]", "stop");

                // {"order":{"id":13832634,"status":"ok","partner_id":"3a8bbee1-5184-4ffe-8a0c-294fbad1aee9"}}
                String response = accountService.post("/orders", parameters);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response).get("order");
                System.out.println(jsonNode);

                String status = jsonNode.get("status").asText();
                String partnerId = jsonNode.get("partner_id").asText();
                String orderId = jsonNode.get("id").asText();

                if (status.equals("ok")) {
                    // Start interval loop to see when filled

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                String response = accountService.getOrder(orderId);
                                System.out.println(response);

                                if (new ObjectMapper().readTree(response).get("status").asText().equals("pending")) {
//                                    timer.cancel();
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

//                            timer.cancel();
                        }
                    }, 0, 5000); // Runs every 5 seconds


//                    String res = accountService.getOrders();
//                    System.out.println(res);
                }

                return jsonNode.toString();
//                accountService.post("/orders", objectMapper.writeValueAsString(parameters));
            } else {
                logger.warn("Not enough cash available to make trade");
                return "Not enough cash available to make trade";
            }
        } catch (IOException e) {
            logger.error(e);
            return e.getMessage();
        }
    }
}

// Positions, Options Chain, Orders, Trades
