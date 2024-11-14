package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.*;
import com.project.apex.model.VisionTrade;
import com.project.apex.repository.VisionTradeRepository;
import com.project.apex.util.Quantities;
import com.project.apex.util.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import static com.project.apex.data.trades.TradeLeg.*;
import static com.project.apex.data.trades.TradeStatus.RUNNERS;
import static com.project.apex.util.Convert.roundedDouble;
import static com.project.apex.util.TradeOrder.*;

@Service
public class VisionTradeService extends TradeService<VisionTrade> {

    private static final Logger logger = LoggerFactory.getLogger(VisionTradeService.class);

    @Autowired
    public VisionTradeService(AccountService accountService, MarketService marketService, VisionTradeRepository baseTradeRepository) {
        super(accountService, marketService, baseTradeRepository);
    }

    @Override
    public void calculateStopsAndTrims(VisionTrade trade) {
        List<Integer> quantities = Quantities.divideIntoThreeGroups(trade.getQuantity());
        int trim1Quantity = quantities.get(0);
        int trim2Quantity = quantities.get(1);
        int runnersQuantity = quantities.get(2);
        double ask = trade.getFillPrice();
        double stopPrice = roundedDouble(ask * (1 - trade.getStopLossPercentage()));
        double trim1Price = roundedDouble(ask * (1 + trade.getTrim1Percentage()));
        double trim2Price = roundedDouble(ask * (1 + trade.getTrim2Percentage()));
        double initialRunnersFloorPrice = roundedDouble(trim2Price / (1 + trade.getRunnersFloorPercentage()));
        trade.setStopPrice(stopPrice);
        trade.setTrim1Price(trim1Price);
        trade.setTrim2Price(trim2Price);
        trade.setRunnersFloorPrice(initialRunnersFloorPrice);
        trade.setRunnersDelta(roundedDouble(trade.getTrim2Price() - initialRunnersFloorPrice));
        trade.setFillPrice(ask);
        trade.setTrim1Quantity(trim1Quantity);
        trade.setTrim2Quantity(trim2Quantity);
        trade.setRunnersQuantity(runnersQuantity);
        trade.setTradeAmount(getValueByQuantity(trade.getQuantity(), ask));
        new Record<>("VisionTrade.calculateStopsAndTrims", trade);
    }

    @Override
    public void handleOpenTrades(VisionTrade trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades) {
        if (trade.getTrimStatus() < 1 && (lastPrice >= trade.getTrim1Price())) {
            trade.setTrimStatus((byte) 1);
            logger.info("VisionTradeManager.watch: {}: Trim 1 Hit! Setting Stops to Break Even: {}", riskType, id);
            if (prepareMarketSell(trade, TRIM1)) {
                trade.setStopPrice(trade.getFillPrice());
            }
        }

        if (trade.getTrimStatus() < 2 && (lastPrice >= trade.getTrim2Price()) && trade.getTrim2Quantity() > 0) {
            trade.setTrimStatus((byte) 2);
            logger.info("VisionTradeManager.watch: {}: Trim 2 Hit! Moving Stops: {}", riskType, id);
            prepareMarketSell(trade, TRIM2);
            trade.setStopPrice(trade.getRunnersFloorPrice());
            trade.setStatus(RUNNERS);
            logger.info("VisionTradeManager.watch: {}: (OPEN -> RUNNERS): {}", riskType, id);
        }

        if (trade.getTrimStatus() > 1 && trade.getRunnersQuantity() > 0) {
            runnerTrades.add(id);
            logger.info("VisionTradeManager.watch: {}: Last Price: {}", riskType, lastPrice);
            logger.info("VisionTradeManager.watch: {}: Last Price > Stop Price: {}", riskType, lastPrice > trade.getStopPrice());
            if (lastPrice > (trade.getStopPrice() + trade.getRunnersDelta())) {
                double newFloor = roundedDouble(lastPrice - trade.getRunnersDelta());
                logger.info("VisionTradeManager.watch: {}: New Floor: {}", riskType, newFloor);
                trade.setStopPrice(newFloor);
            }
        }
    }

    @Override
    public boolean prepareMarketSell(VisionTrade trade, TradeLeg tradeLeg) {
        logger.info("placeMarketSell: Start");
        Map<String, String> parameters = new HashMap<>();

        int quantity = switch (tradeLeg) {
            case TRIM1 -> trade.getTrim1Quantity();
            case TRIM2 -> trade.getTrim2Quantity();
            case STOP -> switch (trade.getTrimStatus()) {
                case 0 -> trade.getTrim1Quantity() + trade.getTrim2Quantity() + trade.getRunnersQuantity();
                case 1 -> trade.getTrim2Quantity() + trade.getRunnersQuantity();
                case 2 -> trade.getRunnersQuantity();
                default -> throw new IllegalStateException("Unexpected value: " + trade.getTrimStatus());
            };
            default -> throw new IllegalStateException("Unexpected value: " + tradeLeg);
        };

        parameters.put("quantity", String.valueOf(quantity));
        return super.placeMarketSell(trade, tradeLeg, parameters);
    }

    @Override
    public void finalizeTrade(VisionTrade trade, TradeLegMap tradeLegMap) {
        logger.info("finalizeTrade: Start: {}", trade.getId());
        int totalQuantity = trade.getQuantity();

        if (tradeLegMap.containsKey(TRIM1)) {
            JsonNode trim1 = tradeLegMap.get(TRIM1);
            logger.info("finalizeTrade: Avg. Fill Price (TRIM1): {}", getPrice(trim1));
            double finalTrim1Price = getPrice(trim1);
            trade.setTrim1PriceFinal(finalTrim1Price);
            trade.setFinalAmount((int) (trade.getFinalAmount() + (trade.getTrim1Quantity() * (finalTrim1Price * 100))));
            totalQuantity = totalQuantity - trade.getTrim1Quantity();
        }

        if (tradeLegMap.containsKey(TRIM2)) {
            JsonNode trim2 = tradeLegMap.get(TRIM2);
            logger.info("finalizeTrade: Avg. Fill Price (TRIM2): {}", getPrice(trim2));
            double finalTrim2Price = getPrice(trim2);
            trade.setTrim2PriceFinal(finalTrim2Price);
            trade.setFinalAmount((int) (trade.getFinalAmount() + (trade.getTrim2Quantity() * (finalTrim2Price * 100))));
            totalQuantity = totalQuantity - trade.getTrim2Quantity();
        }

        JsonNode stop = tradeLegMap.get(STOP);
        logger.info("finalizeTrade: Avg. Fill Price (STOP): {}", getPrice(stop));
        double finalStopPrice = getPrice(stop);
        trade.setStopPriceFinal(finalStopPrice);
        trade.setFinalAmount((int) (trade.getFinalAmount() + (totalQuantity * (finalStopPrice * 100))));
        trade.setCloseDate(getCloseDate(stop));
        trade.setPl(trade.getFinalAmount() - trade.getTradeAmount());
        trade.setPostTradeBalance(trade.getPreTradeBalance() + trade.getPl());
    }

}