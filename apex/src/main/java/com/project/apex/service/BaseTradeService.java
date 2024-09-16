package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.config.EnvConfig;
import com.project.apex.data.account.Balance;
import com.project.apex.data.orders.OrderStatus;
import com.project.apex.data.trades.BaseTrade.BaseTradeLeg;
import com.project.apex.data.trades.BaseTrade.BaseTradeStatus;
import com.project.apex.data.trades.BaseTrade.BaseTradeSummary;
import com.project.apex.data.trades.BuyData;
import com.project.apex.model.BaseTrade;
import com.project.apex.repository.BaseTradeRepository;
import com.project.apex.util.BaseTradeOrder;
import com.project.apex.util.Convert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.project.apex.util.Convert.roundedDouble;

@Service
public class BaseTradeService {

    private static final Logger logger = LogManager.getLogger(BaseTradeService.class);
    protected final EnvConfig envConfig;
    private final MarketService marketService;
    private final AccountService accountService;
    private final BaseTradeRepository baseTradeRepository;
    private final BaseTradeSummary baseTradeSummary;


    @Autowired
    public BaseTradeService(
            EnvConfig envConfig,
            MarketService marketService,
            AccountService accountService,
            BaseTradeRepository baseTradeRepository,
            BaseTradeSummary baseTradeSummary
    ) {
        this.envConfig = envConfig;
        this.marketService = marketService;
        this.accountService = accountService;
        this.baseTradeRepository = baseTradeRepository;
        this.baseTradeSummary = baseTradeSummary;
    }

    public List<BaseTrade> fetchTrades() {
        return baseTradeRepository.findAll();
    }

    public void compileSummary() {
        List<BaseTrade> trades = baseTradeRepository.findAll();
        baseTradeSummary.compile(trades);
        // Send Record Summary over ClientWebsocket


    }

    public void placeFillOrder(BuyData buyData) throws IOException {
        Balance balance = accountService.getBalanceData();
        double totalEquity = balance.getTotalEquity();
        double totalCash = balance.getTotalCash();
        int tradeAllotment = (int) Math.floor(totalEquity * BaseTrade.tradePercentModifier);
        Long id = Convert.getMomentAsCode();

        if (tradeAllotment < totalCash) {
            Map<String, String> parameters = setFillTradeParameters(buyData, tradeAllotment, id);

            String response = accountService.post("/orders", parameters);
            JsonNode jsonNode = new ObjectMapper().readTree(response).get("order");

            if (jsonNode == null) {
                logger.error(response);
            } else {
                if (BaseTradeOrder.isOk(jsonNode)) {
                    BaseTrade trade = new BaseTrade();
                    trade.setPreTradeBalance(totalEquity);
                    trade.setId(id);
                    trade.setOpenDate(BaseTradeOrder.getCreateDate(jsonNode));
                    initializeTrade(trade, jsonNode, BaseTradeStatus.PENDING);
                    baseTradeRepository.save(trade); // baseTradeRepository.flush();  // Ensure that the changes are flushed to the database
                    logger.info("FILL Order created: " + trade.getId());
                } else {
                    logger.error("FILL Order Error: " + BaseTradeOrder.getStatus(jsonNode));
                }
            }
        } else {
            logger.error("Not enough cash available to make trade");
        }
    }

    private static Map<String, String> setFillTradeParameters(BuyData buyData, int tradeAllotment, Long id) {
        double ask = buyData.getPrice();
        double contractCost = ask * 100;
        int contractQuantity = (int) Math.floor(tradeAllotment /contractCost);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("class", "option");
        parameters.put("duration", "day");
        parameters.put("quantity", String.valueOf(contractQuantity));
        parameters.put("side", "buy_to_open");
        parameters.put("option_symbol", buyData.getOption());
        parameters.put("price", String.valueOf(ask));
        parameters.put("type", "limit");
        parameters.put("tag", buyData.getRiskType() + "-" + id + "-" +  BaseTradeLeg.FILL.name());
        return parameters;
    }

    public void modifyStopOrder(Integer orderId, Double newPrice, BaseTrade trade) {
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("stop", newPrice.toString());

            String response = accountService.put("/orders/" + orderId, parameters);
            JsonNode jsonNode = new ObjectMapper().readTree(response).get("order");

            if (jsonNode == null) {
                logger.error(response);
            } else {
                if (BaseTradeOrder.isOk(jsonNode)) {
                    logger.info("STOP Order modified: " + trade.getId());
                } else {
                    logger.error("Stop Modify Order Error: " + BaseTradeOrder.getStatus(jsonNode));
                }
            }
        } catch (Exception e) {
            logger.error("There was an error while modifying stop order", e);
        }
    }

    public void placeStopOrder(BaseTrade trade) {
        logger.info("Placing STOP Order for BaseTrade - id: {}", trade.getId());

        try {
            Map<String, String> parameters = setStopTradeParameters(trade);

            String response = accountService.post("/orders", parameters);
            JsonNode jsonNode = new ObjectMapper().readTree(response).get("order");

            if (jsonNode == null) {
                logger.error(response);
            } else {
                if (BaseTradeOrder.isOk(jsonNode)) {
                    logger.info("STOP Order created: " + trade.getId());
                    placeTrims(trade);
                } else {
                    logger.error("STOP Order Error: " + BaseTradeOrder.getStatus(jsonNode));
                }
            }
        } catch (Exception e) {
            logger.error("STOP Order Error:", e);
        }
    }

    private static Map<String, String> setStopTradeParameters(BaseTrade trade) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("class", "option");
        parameters.put("duration", "gtc");
        parameters.put("option_symbol", trade.getOptionSymbol());
        parameters.put("side", "sell_to_close");
        parameters.put("quantity", trade.getQuantity().toString());
        parameters.put("stop", trade.getStopPrice().toString());
        parameters.put("type", "stop");
        parameters.put("tag", trade.getRiskType() + "-" + trade.getId() + "-" +  BaseTradeLeg.STOP.name());
        return parameters;
    }

    @Transactional
    public void placeTrims(BaseTrade trade) {
        logger.info("Placing TRIM1 Order for BaseTrade - id: {}", trade.getId());

        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("class", "option");
            parameters.put("duration", "gtc");
            parameters.put("option_symbol", trade.getOptionSymbol());
            parameters.put("side", "sell_to_close");
            parameters.put("quantity", String.valueOf(trade.getTrim1Quantity()));
            parameters.put("price", String.valueOf(trade.getTrim1Price()));
            parameters.put("type", "limit");
            parameters.put("tag", trade.getRiskType() + "-" + trade.getId() + "-" +  BaseTradeLeg.TRIM1.name());

            String trim1Response = accountService.post("/orders", parameters);
            JsonNode trim1JsonNode = new ObjectMapper().readTree(trim1Response).get("order");

            if (trim1JsonNode == null) {
                logger.error(trim1Response);
            } else {
                if (BaseTradeOrder.isOk(trim1JsonNode)) {
                    logger.info("TRIM1 Order created: " + trade.getId());
                    logger.info("Placing TRIM2 Order for BaseTrade - id: {}", trade.getId());

                    if (trade.getTrim2Quantity() > 0) {
                        try {
                            parameters.put("tag", trade.getRiskType() + "-" + Convert.getMomentAsCode() + "-" +  BaseTradeLeg.TRIM2.name());
                            parameters.put("price", String.valueOf(trade.getTrim2Price()));

                            String trim2Response = accountService.post("/orders", parameters);
                            JsonNode trim2JsonNode = new ObjectMapper().readTree(trim2Response).get("order");

                            if (trim2JsonNode == null) {
                                logger.error(trim2Response);
                            } else {
                                if (BaseTradeOrder.isOk(trim2JsonNode)) {
                                    logger.info("TRIM2 Order created: " + trade.getId());
                                    trade.setStatus(BaseTradeStatus.OPEN);
                                    logger.info("Trade Initialized with TRIM2: " + trade.getId());
                                } else {
                                    logger.error("TRIM2 Order Error: " + BaseTradeOrder.getStatus(trim2JsonNode));
                                }
                            }
                        } catch (Exception e) {
                            logger.error("TRIM2 Order Error", e);
                        }
                    } else {
                        logger.info("Trade Initialized (No TRIM2 quantity available): " + trade.getId());
                    }
                } else {
                    logger.error("TRIM1 Order Error: " + BaseTradeOrder.getStatus(trim1JsonNode));
                }
            }
        } catch (Exception e) {
            logger.error("TRIM1 Order Error", e);
        }
    }

    public void initializeTrade(BaseTrade trade, JsonNode order, BaseTradeStatus status) {
        double ask = BaseTradeOrder.getPrice(order);
        int quantity = BaseTradeOrder.getQuantity(order);
        double initialRunnersFloorPrice = trade.getTrim2Price() / 2;
        trade.setStopPrice(roundedDouble(ask * (1 - BaseTrade.stopLossPercentage)));
        trade.setTrim1Price(roundedDouble(ask * (1 + BaseTrade.trim1Percentage)));
        trade.setTrim2Price(roundedDouble(ask * (1 + BaseTrade.trim2Percentage)));
        trade.setRunnersFloorPrice(initialRunnersFloorPrice);
        trade.setRunnersDelta(trade.getTrim2Price() - initialRunnersFloorPrice);
        trade.setQuantity(quantity);
        trade.setOptionSymbol(BaseTradeOrder.getOptionSymbol(order));
        trade.setSymbol(BaseTradeOrder.getSymbol(order));
        trade.setFillPrice(ask);
        trade.setTradeAmount((int) (ask * 100) * quantity);
        trade.setStatus(status);
    }

    public void setPrices(BaseTrade trade) throws IOException, URISyntaxException {
        JsonNode quote = marketService.getPrices(trade.getOptionSymbol());
        double bid = quote.get("bid").asDouble();
        trade.setLastPrice(bid);
        trade.setMaxPrice(Math.max(trade.getMaxPrice(), bid));
    }

    @Scheduled(fixedRate = 10000)
    public void fetchTradesSchedule() {
        // Only during market hours
        List<BaseTrade> trades = fetchTrades();

//        List<BaseTrade> filteredTrades = trades.stream().filter(e -> e.);
    }
}