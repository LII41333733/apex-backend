package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.*;
import com.project.apex.model.BaseTrade;
import com.project.apex.repository.BaseTradeRepository;
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
import static com.project.apex.util.Calculate.getValueByQuantity;
import static com.project.apex.util.Convert.roundedDouble;
import static com.project.apex.util.TradeOrder.*;

@Service
public class BaseTradeService extends TradeService<BaseTrade> {

    private static final Logger logger = LoggerFactory.getLogger(BaseTradeService.class);

    @Autowired
    public BaseTradeService(AccountService accountService, MarketService marketService, BaseTradeRepository baseTradeRepository) {
        super(accountService, marketService, baseTradeRepository);
    }

    @Override
    public void calculateStopsAndTrims(BaseTrade trade) {
        List<Integer> quantities = Quantities.divideIntoThreeGroups(trade.getQuantity());
        int trim1Quantity = quantities.get(0);
        int trim2Quantity = quantities.get(1);
        int runnersQuantity = quantities.get(2);
        double ask = trade.getFillPrice();
        double stopPrice = roundedDouble(ask * (1 - trade.getTradeProfile().getStopPercentage()));
        double trim1Price = roundedDouble(ask * (1 + trade.getTradeProfile().getTrim1Percentage()));
        double trim2Price = roundedDouble(ask * (1 + trade.getTradeProfile().getTrim2Percentage()));
        trade.setStopPrice(stopPrice);
        trade.setTrim1Price(trim1Price);
        trade.setTrim2Price(trim2Price);
        trade.setFillPrice(ask);
        trade.setTrim1Quantity(trim1Quantity);
        trade.setTrim2Quantity(trim2Quantity);
        trade.setRunnersQuantity(runnersQuantity);
        trade.setTradeAmount(getValueByQuantity(trade.getQuantity(), ask));
        new Record<>("BaseTrade.calculateStopsAndTrims", trade);
    }

    @Override
    public void handleOpenTrades(BaseTrade trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades) {
        if (trade.getTrimStatus() == 0 && (lastPrice >= trade.getTrim1Price())) {
            logger.info("BaseTradeManager.watch: {}: Trim 1 Hit! Setting Stops to Break Even: {}", riskType, id);
            trade.setTrimStatus(1);
            prepareMarketSell(trade, TRIM1);
            trade.setRunnersFloorIsActive(true);
            double floorPrice = getPercentValue(trade.getFillPrice(), 0.4);
            trade.setStopPrice(Math.max(trade.getFillPrice(), floorPrice));
        }

        if (trade.getTrimStatus() == 1) {
            double floorPrice = getPercentValue(lastPrice, 0.4);
            if (trade.getStopPrice() < floorPrice) {
                trade.setStopPrice(floorPrice);
            }
        }

        if (trade.getTrimStatus() < 2 && (lastPrice >= trade.getTrim2Price()) && trade.getTrim2Quantity() > 0) {
            logger.info("BaseTradeManager.watch: {}: Trim 2 Hit! Moving Stops: {} (OPEN -> RUNNERS)", riskType, id);
            trade.setTrimStatus(2);
            prepareMarketSell(trade, TRIM2);
            trade.setStatus(RUNNERS);
            double floorPrice = getPercentValue(lastPrice, 0.25);
            trade.setStopPrice(floorPrice);
        }

        if (trade.getTrimStatus() == 2) {
            double floorPrice = getPercentValue(lastPrice, 0.25);
            if (trade.getStopPrice() < floorPrice) {
                trade.setStopPrice(floorPrice);
            }
            runnerTrades.add(id);
        }
    }

    @Override
    public void finalizeTrade(BaseTrade trade, TradeLegMap tradeLegMap) {
        logger.info("BaseTradeService.finalizeTrade: Start: {}", trade.getId());
        int totalQuantity = trade.getQuantity();

        if (tradeLegMap.containsKey(TRIM1)) {
            JsonNode trim1 = tradeLegMap.get(TRIM1);
            logger.info("BaseTradeService.finalizeTrade: Avg. Fill Price (TRIM1): {}", getPrice(trim1));
            double finalTrim1Price = getPrice(trim1);
            trade.setTrim1PriceFinal(finalTrim1Price);
            trade.setFinalAmount(trade.getFinalAmount() + getValueByQuantity(trade.getTrim1Quantity(), finalTrim1Price));
            totalQuantity = totalQuantity - trade.getTrim1Quantity();
        }

        if (tradeLegMap.containsKey(TRIM2)) {
            JsonNode trim2 = tradeLegMap.get(TRIM2);
            logger.info("BaseTradeService.finalizeTrade: Avg. Fill Price (TRIM2): {}", getPrice(trim2));
            double finalTrim2Price = getPrice(trim2);
            trade.setTrim2PriceFinal(finalTrim2Price);
            trade.setFinalAmount(trade.getFinalAmount() + getValueByQuantity(trade.getTrim2Quantity() , finalTrim2Price));
            totalQuantity = totalQuantity - trade.getTrim2Quantity();
        }

        JsonNode stop = tradeLegMap.get(STOP);
        logger.info("BaseTradeService.finalizeTrade: Avg. Fill Price (STOP): {}", getPrice(stop));
        double finalStopPrice = getPrice(stop);
        trade.setStopPriceFinal(finalStopPrice);
        trade.setFinalAmount(trade.getFinalAmount() + getValueByQuantity(totalQuantity , finalStopPrice));

        trade.setCloseDate(getCloseDate(stop));
        trade.setPl(trade.getFinalAmount() - trade.getTradeAmount());
        trade.setPostTradeBalance(trade.getPreTradeBalance() + trade.getPl());
    }

}