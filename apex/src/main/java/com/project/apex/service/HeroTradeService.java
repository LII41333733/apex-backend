package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.*;
import com.project.apex.model.HeroTrade;
import com.project.apex.repository.HeroTradeRepository;
import com.project.apex.util.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import static com.project.apex.data.trades.TradeLeg.STOP;
import static com.project.apex.data.trades.TradeLeg.TRIM1;
import static com.project.apex.data.trades.TradeStatus.RUNNERS;
import static com.project.apex.util.Calculate.getPercentValue;
import static com.project.apex.util.Calculate.getValueByQuantity;
import static com.project.apex.util.Convert.roundedDouble;
import static com.project.apex.util.TradeOrder.*;

@Service
public class HeroTradeService extends TradeService<HeroTrade> {

    private static final Logger logger = LoggerFactory.getLogger(HeroTradeService.class);

    @Autowired
    public HeroTradeService(AccountService accountService, MarketService marketService, HeroTradeRepository heroTradeRepository) {
        super(accountService, marketService, heroTradeRepository);
    }

    @Override
    public void calculateStopsAndTrims(HeroTrade trade) {
        double ask = trade.getFillPrice();
        double stopPrice = 0.02;
        trade.setStopPrice(stopPrice);
        trade.setFillPrice(ask);
        trade.setRunnersQuantity(trade.getQuantity());
        trade.setTradeAmount(getValueByQuantity(trade.getQuantity(), ask));
        new Record<>("calculateStopsAndTrims", trade);
    }

    @Override
    public void handleOpenTrades(HeroTrade trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades) {
        runnerTrades.add(id);
        if (trade.getStatus() != RUNNERS) {
            logger.info("HeroTradeManager.watch: {}: Floor is Active: {}", riskType, id);
            trade.setStatus(RUNNERS);
            trade.setRunnersFloorIsActive(true);
            double floorPrice = getPercentValue(trade.getFillPrice(), 0.4);
            trade.setStopPrice(floorPrice);
        } else {
            double lastFloorPrice = getPercentValue(lastPrice, 0.4);
            if (trade.getStopPrice() < lastFloorPrice) {
                trade.setStopPrice(lastFloorPrice);
                logger.info("LottoTradeManager.watch: {}: New Floor: {}", riskType, lastFloorPrice);
            }
        }
    }

    @Override
    public void finalizeTrade(HeroTrade trade, TradeLegMap tradeLegMap) {
        logger.debug("finalizeTrade: Start: {}", trade.getId());
        int totalQuantity = trade.getQuantity();
        JsonNode stop = tradeLegMap.get(STOP);
        new Record<>("Stop Order", stop);
        trade.setStopPriceFinal(getPrice(stop));
        trade.setFinalAmount((int) (trade.getFinalAmount() + (totalQuantity * (getPrice(stop) * 100))));
        trade.setCloseDate(getCloseDate(stop));
        trade.setPl(trade.getFinalAmount() - trade.getTradeAmount());
        trade.setPostTradeBalance(trade.getPreTradeBalance() + trade.getPl());
    }
}