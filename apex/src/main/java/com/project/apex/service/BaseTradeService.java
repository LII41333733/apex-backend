package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.config.EnvConfig;
import com.project.apex.data.account.Balance;
import com.project.apex.data.orders.OrderFillRecord;
import com.project.apex.data.trades.BaseTrade.BaseTradeLeg;
import com.project.apex.data.trades.BaseTrade.BaseTradeSummary;
import com.project.apex.data.trades.BuyData;
import com.project.apex.model.BaseTrade;
import com.project.apex.repository.BaseTradeRepository;
import com.project.apex.util.Convert;
import com.project.apex.util.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.*;

import static com.project.apex.util.BaseTradeOrder.getStatus;
import static com.project.apex.util.BaseTradeOrder.isOk;
import static com.project.apex.util.Convert.roundedDouble;

@Service
public class BaseTradeService {

    /**
     * For base trades, on the UI, hide all options with asks less than .11
     * They should be saved for lottos
     */

    private static final Logger logger = LoggerFactory.getLogger(BaseTradeService.class);
    protected final EnvConfig envConfig;
    private final MarketService marketService;
    private final AccountService accountService;
    private final BaseTradeRepository baseTradeRepository;

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
    }

    public void placeFill(BuyData buyData) {

        try {
            Long id = Convert.getMomentAsCode();
            logger.info("BaseTradeService.placeFill: Start: {}", id);
            Balance balance = accountService.getBalanceData();
            double totalEquity = balance.getTotalEquity();
            logger.info("Total Equity: {}", totalEquity);
            double totalCash = balance.getTotalCash();
            logger.info("Total Cash: {}", totalCash);
            int tradeAllotment = (int) Math.floor(totalEquity * BaseTrade.tradePercentModifier);
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
                parameters.put("tag", buyData.getRiskType().toUpperCase() + "-" + id + "-" +  BaseTradeLeg.FILL.name());

                new Record<>("BaseTradeService.placeFill: Fill Parameters", new OrderFillRecord(
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

                if (jsonNode != null) {
                   logger.info("BaseTradeService.placeFill: Fill Successful: {}", id);
                   BaseTrade trade = new BaseTrade(id, totalEquity, ask, quantity);
                   baseTradeRepository.save(trade);
                } else {
                   logger.error("BaseTradeService.placeFill: Fill UnSuccessful");
                }
            } else {
               logger.error("BaseTradeService.placeFill: Not enough cash available to make trade");
            }
       } catch (Exception e) {
           logger.error("BaseTradeService.placeFill: ERROR: Exception", e);
       }
    }



    public void placeTrims(BaseTrade trade) {
        logger.info("BaseTradeService.placeTrims: Start");

        try {
            Map<String, String> trim1Parameters = new HashMap<>();
            trim1Parameters.put("class", "oco");
            trim1Parameters.put("duration", "gtc");
            trim1Parameters.put("option_symbol[0]", trade.getOptionSymbol());
            trim1Parameters.put("option_symbol[1]", trade.getOptionSymbol());
            trim1Parameters.put("side[0]", "sell_to_close");
            trim1Parameters.put("side[1]", "sell_to_close");
            trim1Parameters.put("quantity[0]", String.valueOf(trade.getTrim1Quantity()));
            trim1Parameters.put("quantity[1]", String.valueOf(trade.getTrim1Quantity()));
            trim1Parameters.put("price[0]", String.valueOf(trade.getStopPrice()));
            trim1Parameters.put("stop[0]", String.valueOf(trade.getStopPrice()));
            trim1Parameters.put("price[1]", String.valueOf(trade.getTrim1Price()));
            trim1Parameters.put("type[0]", "stop_limit");
            trim1Parameters.put("type[1]", "limit");
            trim1Parameters.put("tag", trade.getRiskType().name() + "-" + trade.getId() + "-" +  BaseTradeLeg.TRIM1.name());

            new Record<>("BaseTradeService.placeTrim1: Trim 1 Parameters", trim1Parameters);

            JsonNode trim1Json = accountService.post("/orders", trim1Parameters);
            JsonNode trim1JsonNode = trim1Json.get("order");

            if (trim1JsonNode != null) {
                logger.info("BaseTradeService.placeTrim1: Trim 1 Successful");

                if (trade.getTrim2Quantity() > 0) {
                   placeTrim2(trade);
                } else {
                    logger.info("BaseTradeService.placeTrim1: No Trim 2 quantity available)");
                }
            } else {
                logger.error("BaseTradeService.placeTrim1: Trim 1 UnSuccessful");
            }
        } catch (Exception e) {
            logger.error("BaseTradeService.placeTrim1: ERROR: Exception: {}", e.getMessage(), e);
        }
    }

    public void placeTrim2(BaseTrade trade) {
        logger.info("BaseTradeService.placeTrim2: Start");
        try {
            Map<String, String> trim2Parameters = new HashMap<>();
            trim2Parameters.put("class", "oco");
            trim2Parameters.put("duration", "gtc");
            trim2Parameters.put("option_symbol[0]", trade.getOptionSymbol());
            trim2Parameters.put("option_symbol[1]", trade.getOptionSymbol());
            trim2Parameters.put("side[0]", "sell_to_close");
            trim2Parameters.put("side[1]", "sell_to_close");
            trim2Parameters.put("quantity[0]", String.valueOf(trade.getTrim2Quantity()));
            trim2Parameters.put("quantity[1]", String.valueOf(trade.getTrim2Quantity()));
            trim2Parameters.put("stop[0]", String.valueOf(roundedDouble(trade.getStopPrice())));
            trim2Parameters.put("price[0]", String.valueOf(trade.getStopPrice() - 0.2));
            trim2Parameters.put("price[1]", String.valueOf(trade.getTrim2Price()));
            trim2Parameters.put("type[0]", "stop_limit");
            trim2Parameters.put("type[1]", "limit");
            trim2Parameters.put("tag", trade.getRiskType().name() + "-" + trade.getId() + "-" +  BaseTradeLeg.TRIM2.name());

            new Record<>("BaseTradeService.placeTrim2: Trim 2 Parameters", trim2Parameters);

            JsonNode trim2JsonNode = accountService.post("/orders", trim2Parameters);
            JsonNode trim2Json = trim2JsonNode.get("order");

            if (trim2Json != null) {
                logger.info("BaseTradeService.placeTrim2: Trim 2 Successful");

                if (trade.getRunnersQuantity() > 0) {
                    placeRunners(trade);
                } else {
                    logger.info("BaseTradeService.placeTrim2: No Runners quantity available)");
                }
            } else {
                logger.error("BaseTradeService.placeTrim2: Trim 2 UnSuccessful");
            }
        } catch (Exception e) {
            logger.error("BaseTradeService.placeTrim2: ERROR: Exception: {}", e.getMessage(), e);
        }
    }

    public void placeRunners(BaseTrade trade) {
        logger.info("BaseTradeService.placeRunners: Start");
        try {
            Map<String, String> runnersParameters = new HashMap<>();
            runnersParameters.put("class", "option");
            runnersParameters.put("duration", "gtc");
            runnersParameters.put("option_symbol", trade.getOptionSymbol());
            runnersParameters.put("side", "sell_to_close");
            runnersParameters.put("quantity", trade.getRunnersQuantity().toString());
            runnersParameters.put("stop", trade.getStopPrice().toString());
            runnersParameters.put("type", "stop");
            runnersParameters.put("tag", trade.getRiskType().name() + "-" + trade.getId() + "-" +  BaseTradeLeg.TRIM3.name());

            new Record<>("BaseTradeService.placeRunners: Runners Parameters", runnersParameters);

            JsonNode runnersJsonNode = accountService.post("/orders", runnersParameters);
            JsonNode runnersJson = runnersJsonNode.get("order");

            if (runnersJson != null) {
                logger.info("BaseTradeService.placeRunners: Runners Successful");
            } else {
                logger.error("BaseTradeService.placeRunners: Runners UnSuccessful");
            }
        } catch (Exception e) {
            logger.error("BaseTradeService.placeRunners: ERROR: Exception: {}", e.getMessage(), e);
        }
    }

    public void modifyStopOrder(Integer orderId, Double newPrice, BaseTrade trade) {
        logger.info("BaseTradeService.modifyStopOrder: Start: ID: {} Order ID: {} New Price: {}", trade.getId(), orderId, newPrice);
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("stop[0]", newPrice.toString());
            parameters.put("limit[0]", newPrice.toString());

            JsonNode response = accountService.put("/orders/" + orderId, parameters);

            if (isOk(response)) {
                logger.info("BaseTradeService.modifyStopOrder: Modify Stop Successful");
            } else {
                logger.error("BaseTradeService.modifyStopOrder: Modify Stop UnSuccessful");
            }
        } catch (Exception e) {
            21 logger.error("BaseTradeService.modifyStopOrder: ERROR: Exception: {}", e.getMessage(), e);
        }
    }

    public void setLastAndMaxPrices(BaseTrade trade) throws IOException, URISyntaxException {
        logger.info("BaseTradeService.setLastAndMaxPrices: Start: {}", trade.getId());
        JsonNode quote = marketService.getPrices(trade.getOptionSymbol());
        double bid = quote.get("bid").asDouble();
        double tradeMaxPrice = trade.getMaxPrice();
        logger.info("Last Price: {}", bid);
        trade.setLastPrice(bid);
        logger.info("Max Price: {}", Math.max(tradeMaxPrice, bid));
        trade.setMaxPrice(Math.max(tradeMaxPrice, bid));
    }

    public void finalizeTrade(BaseTrade trade) {
        logger.info("BaseTradeService.finalizeTrade: Start: {}", trade.getId());
        logger.info("Trim Status: {}", trade.getTrimStatus());
        if (trade.getTrimStatus() > 0) {
            logger.info("Post Trade Balance: {}", trade.getPreTradeBalance() + trade.getTradeAmount());
            trade.setPostTradeBalance(trade.getPreTradeBalance() + trade.getTradeAmount());
            logger.info("P/L: {}", trade.getTradeAmount());
            trade.setPl(trade.getTradeAmount());
        } else {
            logger.info("Post Trade Balance: {}", trade.getPreTradeBalance() - trade.getTradeAmount());
            trade.setPostTradeBalance(trade.getPreTradeBalance() - trade.getTradeAmount());
            logger.info("P/L: {}", -trade.getTradeAmount());
            trade.setPl(-trade.getTradeAmount());
        }
    }

    public List<BaseTrade> fetchTrades() {
        return baseTradeRepository.findAll();
    }

    @Scheduled(fixedRate = 10000)
    public void fetchTradesSchedule() {
        // Only during market hours
        List<BaseTrade> trades = fetchTrades();

//        List<BaseTrade> filteredTrades = trades.stream().filter(e -> e.);
    }
}