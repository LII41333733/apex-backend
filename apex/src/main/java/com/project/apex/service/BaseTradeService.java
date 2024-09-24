package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.component.BaseTradeManager;
import com.project.apex.component.TradeManagerInterface;
import com.project.apex.config.EnvConfig;
import com.project.apex.data.account.Balance;
import com.project.apex.data.orders.OrderFillRecord;
import com.project.apex.data.trades.BuyData;
import com.project.apex.data.trades.RiskType;
import com.project.apex.data.trades.TradeLeg;
import com.project.apex.data.trades.TradeLegMap;
import com.project.apex.model.BaseTrade;
import com.project.apex.repository.BaseTradeRepository;
import com.project.apex.util.Convert;
import com.project.apex.util.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import static com.project.apex.data.trades.TradeLeg.*;
import static com.project.apex.data.trades.TradeStatus.RUNNERS;
import static com.project.apex.util.Convert.roundedDouble;
import static com.project.apex.util.TradeOrder.*;

@Service
public class BaseTradeService implements TradeManagerInterface<BaseTrade> {

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
            BaseTradeRepository baseTradeRepository
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
                int quantity = (int) Math.floor(tradeAllotment / contractCost);
                logger.info("Quantity: {}", quantity);
                Map<String, String> parameters = new HashMap<>();
                parameters.put("class", "option");
                parameters.put("duration", "day");
                parameters.put("quantity", String.valueOf(quantity));
                parameters.put("side", "buy_to_open");
                parameters.put("option_symbol", buyData.getOption());
                parameters.put("price", String.valueOf(ask));
                parameters.put("type", "limit");
                parameters.put("tag", buyData.getRiskType().toUpperCase() + "-" + id + "-" + TradeLeg.FILL);

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

                if (isOk(jsonNode)) {
                    logger.info("BaseTradeService.placeFill: Fill Successful: {}", id);
                    Long orderId = jsonNode.get("id").asLong();
                    BaseTrade trade = new BaseTrade(id, totalEquity, ask, quantity, orderId);
                    baseTradeRepository.save(trade);
                } else {
                    logger.error("BaseTradeService.placeFill: Fill UnSuccessful: {}", id);
                }
            } else {
                logger.error("BaseTradeService.placeFill: Not enough cash available to make trade: {}", id);
            }
        } catch (Exception e) {
            logger.error("BaseTradeService.placeFill: ERROR: Exception", e);
        }
    }

    @Override
    public boolean placeMarketSell(BaseTrade trade, TradeLeg TradeLeg) {
        logger.info("BaseTradeService.placeMarketSell: Start");
        boolean result = true;

        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("class", "option");
            parameters.put("duration", "day");
            parameters.put("type", "market");
            parameters.put("option_symbol", trade.getOptionSymbol());
            parameters.put("side", "sell_to_close");
            parameters.put("tag", trade.getRiskType().name() + "-" + trade.getId() + "-" + TradeLeg.name());

            switch (TradeLeg) {
                case TRIM1 -> parameters.put("quantity", String.valueOf(trade.getTrim1Quantity()));
                case TRIM2 -> parameters.put("quantity", String.valueOf(trade.getTrim2Quantity()));
                case STOP -> {
                    switch (trade.getTrimStatus()) {
                        case 0 ->
                                parameters.put("quantity", String.valueOf(trade.getTrim1Quantity() + trade.getTrim2Quantity() + trade.getRunnersQuantity()));
                        case 1 ->
                                parameters.put("quantity", String.valueOf(trade.getTrim2Quantity() + trade.getRunnersQuantity()));
                        case 2 -> parameters.put("quantity", String.valueOf(trade.getRunnersQuantity()));
                    }
                }
            }

            new Record<>("BaseTradeService.placeMarketSell: Parameters:", parameters);

            JsonNode response = accountService.post("/orders", parameters);
            JsonNode order = response.get("order");

            if (isOk(order)) {
                logger.info("BaseTradeService.placeMarketSell: Market Sell Successful: {}", trade.getId());
            } else {
                logger.error("BaseTradeService.placeMarketSell: Market Sell UnSuccessful: {}", trade.getId());
            }
        } catch (Exception e) {
            logger.error("BaseTradeService.placeMarketSell: ERROR: Exception: {}, ID: {}", e.getMessage(), trade.getId(), e);
            result = false;
        }

        return result;
    }

    @Override
    public void setLastAndMaxPrices(BaseTrade trade) throws IOException, URISyntaxException {
        logger.debug("BaseTradeService.setLastAndMaxPrices: Start: {}", trade.getId());
        JsonNode quote = marketService.getPrices(trade.getOptionSymbol());
        double bid = quote.get("bid").asDouble();
        double tradeMaxPrice = trade.getMaxPrice();
        trade.setLastPrice(bid);
        trade.setMaxPrice(Math.max(tradeMaxPrice, bid));
    }

    @Override
    public void finalizeTrade(BaseTrade trade, TradeLegMap tradeLegMap) {
        logger.info("BaseTradeService.finalizeTrade: Start: {}", trade.getId());
        int totalQuantity = trade.getQuantity();

        if (tradeLegMap.containsKey(TRIM1)) {
            JsonNode trim1 = tradeLegMap.get(TRIM1);
            logger.info("BaseTradeService.finalizeTrade: Avg. Fill Price (TRIM1): {}", getAvgFillPrice(trim1));
            double finalTrim1Price = getAvgFillPrice(trim1);
            trade.setTrim1PriceFinal(finalTrim1Price);
            trade.setFinalAmount((int) (trade.getFinalAmount() + (trade.getTrim1Quantity() * (finalTrim1Price * 100))));
            totalQuantity = totalQuantity - trade.getTrim1Quantity();
        }

        if (tradeLegMap.containsKey(TRIM2)) {
            JsonNode trim2 = tradeLegMap.get(TRIM2);
            logger.info("BaseTradeService.finalizeTrade: Avg. Fill Price (TRIM2): {}", getAvgFillPrice(trim2));
            double finalTrim2Price = getAvgFillPrice(trim2);
            trade.setTrim2PriceFinal(finalTrim2Price);
            trade.setFinalAmount((int) (trade.getFinalAmount() + (trade.getTrim1Quantity() * (finalTrim2Price * 100))));
            totalQuantity = totalQuantity - trade.getTrim2Quantity();
        }

        JsonNode stop = tradeLegMap.get(STOP);
        logger.info("BaseTradeService.finalizeTrade: Avg. Fill Price (STOP): {}", getAvgFillPrice(stop));
        double finalStopPrice = getAvgFillPrice(stop);
        trade.setStopPriceFinal(finalStopPrice);
        trade.setFinalAmount((int) (trade.getFinalAmount() + (totalQuantity * (finalStopPrice * 100))));
        trade.setCloseDate(getCloseDate(stop));
        trade.setPl(trade.getFinalAmount() - trade.getTradeAmount());
        trade.setPostTradeBalance(trade.getPreTradeBalance() + trade.getPl());
    }

    public void handleOpenTrades(BaseTrade trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades) {
        if (trade.getTrimStatus() < 1 && (lastPrice >= trade.getTrim1Price())) {
            trade.setTrimStatus((byte) 1);
            logger.info("BaseTradeManager.watch: {}: Trim 1 Hit!: {}", riskType, id);
            placeMarketSell(trade, TRIM1);
        }

        if (trade.getTrimStatus() < 2 && (lastPrice >= trade.getTrim2Price())) {
            trade.setTrimStatus((byte) 2);
            logger.info("BaseTradeManager.watch: {}: Trim 2 Hit! Moving Stops: {}", riskType, id);
            placeMarketSell(trade, TRIM2);
            trade.setStopPrice(trade.getRunnersFloorPrice());
            trade.setStatus(RUNNERS);
            logger.info("BaseTradeManager.watch: {}: (OPEN -> RUNNERS): {}", riskType, id);
        }

        if (trade.getTrimStatus() > 1) {
            runnerTrades.add(id);
            logger.info("BaseTradeManager.watch: {}: Last Price: {}", riskType, lastPrice);
            logger.info("BaseTradeManager.watch: {}: Last Price > Stop Price: {}", riskType, lastPrice > trade.getStopPrice());
            if (lastPrice > (trade.getStopPrice() + trade.getRunnersDelta())) {
                double newFloor = roundedDouble(lastPrice - trade.getRunnersDelta());
                logger.info("BaseTradeManager.watch: {}: New Floor: {}", riskType, newFloor);
                trade.setStopPrice(newFloor);
            }
        }
    }
}