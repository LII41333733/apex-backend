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
import static com.project.apex.util.Calculate.getPercentValue;
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
        double stopPrice = roundedDouble(ask * (1 - trade.getStopPercentage()));
        double trim1Price = roundedDouble(ask * (1 + trade.getTrim1Percentage()));
        double trim2Price = roundedDouble(ask * (1 + trade.getTrim2Percentage()));
        trade.setStopPrice(stopPrice);
        trade.setTrim1Price(trim1Price);
        trade.setTrim2Price(trim2Price);
        trade.setFillPrice(ask);
        trade.setTrim1Quantity(trim1Quantity);
        trade.setTrim2Quantity(trim2Quantity);
        trade.setRunnersQuantity(runnersQuantity);
        trade.setTradeAmount(getValueByQuantity(trade.getQuantity(), ask));
        new Record<>("VisionTrade.calculateStopsAndTrims", trade);
    }

    @Override
    public void handleOpenTrades(VisionTrade trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades) {
        if (trade.getTrimStatus() == 0 && (lastPrice >= trade.getTrim1Price())) {
            logger.info("VisionTradeManager.watch: {}: Trim 1 Hit! Floor is Active: {}", riskType, id);
            trade.setTrimStatus(1);
            prepareMarketSell(trade, TRIM1);
            trade.setRunnersFloorIsActive(true);
            double floorPrice = getPercentValue(trade.getFillPrice(), 0.4);
            trade.setStopPrice(Math.max(trade.getFillPrice(), floorPrice));
        }

        if (trade.getTrimStatus() == 1) {
            double floorPrice = getPercentValue(lastPrice, 0.4);
            if (trade.getStopPrice() < floorPrice) {
                logger.info("LottoTradeManager.watch: {}: New Floor: {}", riskType, floorPrice);
                trade.setStopPrice(floorPrice);
            }
        }

        if (trade.getTrimStatus() < 2 && (lastPrice >= trade.getTrim2Price()) && trade.getTrim2Quantity() > 0) {
            logger.info("VisionTradeManager.watch: {}: Trim 2 Hit! Moving Stops: {} (OPEN -> RUNNERS)", riskType, id);
            trade.setTrimStatus(2);
            prepareMarketSell(trade, TRIM2);
            trade.setStatus(RUNNERS);
            double floorPrice = getPercentValue(lastPrice, 0.25);
            trade.setStopPrice(floorPrice);
        }

        if (trade.getTrimStatus() == 2) {
            double floorPrice = getPercentValue(lastPrice, 0.25);
            if (trade.getStopPrice() < floorPrice)
                logger.info("LottoTradeManager.watch: {}: New Floor: {}", riskType, floorPrice);{
                trade.setStopPrice(floorPrice);
            }
            runnerTrades.add(id);
        }
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