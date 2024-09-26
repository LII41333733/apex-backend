package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.*;
import com.project.apex.model.BaseTrade;
import com.project.apex.repository.BaseTradeRepository;
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
public class BaseTradeService extends TradeService<BaseTrade> implements TradeServiceInterface<BaseTrade> {

    private static final Logger logger = LoggerFactory.getLogger(BaseTradeService.class);

    @Autowired
    public BaseTradeService(AccountService accountService, MarketService marketService, BaseTradeRepository baseTradeRepository) {
        super(accountService, marketService, baseTradeRepository);
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

    @Override
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