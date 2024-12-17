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
import static com.project.apex.data.trades.TradeStatus.RUNNERS;
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
        int runnersQuantity = trade.getQuantity();
        double ask = trade.getFillPrice();
        double stopPrice = 0.02;
        trade.setStopPrice(stopPrice);
        trade.setFillPrice(ask);
        trade.setRunnersQuantity(runnersQuantity);
        trade.setTradeAmount(getValueByQuantity(trade.getQuantity(), ask));
        new Record<>("calculateStopsAndTrims", trade);
    }

    @Override
    public void handleOpenTrades(HeroTrade trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades) {
        if (trade.getTrimStatus() < 1 && (lastPrice >= trade.getRunnersFloorPrice() + trade.getRunnersDelta())) {
            trade.setTrimStatus((byte) 1);
            logger.info("handleOpenTrades: {}: Moving Floor Engaged!: {}", riskType, id);
            trade.setStopPrice(trade.getRunnersFloorPrice());
            trade.setStatus(RUNNERS);
            logger.info("handleOpenTrades: {}: (OPEN -> RUNNERS): {}", riskType, id);
        }

        if (trade.getTrimStatus() > 0) {
            runnerTrades.add(id);
            logger.info("handleOpenTrades: {}: Last Price: {}", riskType, lastPrice);
            logger.info("handleOpenTrades: {}: Last Price > Stop Price: {}", riskType, lastPrice > trade.getStopPrice());
            if (lastPrice > (trade.getStopPrice() + trade.getRunnersDelta())) {
                double newFloor = roundedDouble(lastPrice - trade.getRunnersDelta());
                logger.info("handleOpenTrades: {}: New Floor: {}", riskType, newFloor);
                trade.setStopPrice(newFloor);
            }
        }
    }

    @Override
    public boolean prepareMarketSell(HeroTrade trade, TradeLeg tradeLeg) {
        logger.info("placeMarketSell: Start");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("quantity", String.valueOf(trade.getQuantity()));
        return super.placeMarketSell(trade, tradeLeg, parameters);
    }

    @Override
    public void finalizeTrade(HeroTrade trade, TradeLegMap tradeLegMap) {
        logger.debug("finalizeTrade: Start: {}", trade.getId());
        int totalQuantity = trade.getQuantity();
        JsonNode stop = tradeLegMap.get(STOP);
        new Record<>("Stop Order", stop);
        double finalStopPrice = getPrice(stop);
        trade.setStopPriceFinal(finalStopPrice);
        trade.setFinalAmount((int) (trade.getFinalAmount() + (totalQuantity * (finalStopPrice * 100))));
        trade.setCloseDate(getCloseDate(stop));
        trade.setPl(trade.getFinalAmount() - trade.getTradeAmount());
        trade.setPostTradeBalance(trade.getPreTradeBalance() + trade.getPl());
    }
}