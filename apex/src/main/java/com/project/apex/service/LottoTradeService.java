package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.*;
import com.project.apex.model.LottoTrade;
import com.project.apex.repository.LottoTradeRepository;
import com.project.apex.util.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import static com.project.apex.data.trades.TradeLeg.STOP;
import static com.project.apex.data.trades.TradeLeg.TRIM1;
import static com.project.apex.util.Calculate.getPercentValue;
import static com.project.apex.util.Convert.roundedDouble;
import static com.project.apex.util.TradeOrder.*;

@Service
public class LottoTradeService extends TradeService<LottoTrade> {

    private static final Logger logger = LoggerFactory.getLogger(LottoTradeService.class);


    @Autowired
    public LottoTradeService(AccountService accountService, MarketService marketService, LottoTradeRepository lottoTradeRepository) {
        super(accountService, marketService, lottoTradeRepository);
    }

    @Override
    public void calculateStopsAndTrims(LottoTrade trade) {
        int trim1Quantity = (int) Math.round(trade.getQuantity() * 0.7);
        int runnersQuantity = trade.getQuantity() - trim1Quantity;
        double ask = trade.getFillPrice();
        double stopPrice = roundedDouble(ask * (1 - trade.getStopPercentage()));
        double trim1Price = roundedDouble(ask * (1 + trade.getTrim1Percentage()));
        trade.setStopPrice(stopPrice);
        trade.setTrim1Price(trim1Price);
        trade.setFillPrice(ask);
        trade.setTrim1Quantity(trim1Quantity);
        trade.setRunnersQuantity(runnersQuantity);
        trade.setTradeAmount(getValueByQuantity(trade.getQuantity(), ask));
        new Record<>("LottoTrade.calculateStopsAndTrims", trade);
    }

    @Override
    public void handleOpenTrades(LottoTrade trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades) {
        if (trade.getTrimStatus() == 0 && (lastPrice >= trade.getTrim1Price())) {
            logger.info("LottoTradeManager.watch: {}: Trim 1 Hit! Floor is Active: {}", riskType, id);
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
            runnerTrades.add(id);
        }
    }

    @Override
    public void finalizeTrade(LottoTrade trade, TradeLegMap tradeLegMap) {
        logger.debug("LottoTradeService.finalizeTrade: Start: {}", trade.getId());
        int totalQuantity = trade.getQuantity();

        if (tradeLegMap.containsKey(TRIM1)) {
            JsonNode trim1 = tradeLegMap.get(TRIM1);
            logger.debug("LottoTradeService.finalizeTrade: Avg. Fill Price (TRIM1): {}", getPrice(trim1));
            double finalTrim1Price = getPrice(trim1);
            trade.setTrim1PriceFinal(finalTrim1Price);
            trade.setFinalAmount((int) (trade.getFinalAmount() + (trade.getTrim1Quantity() * (finalTrim1Price * 100))));
            totalQuantity = totalQuantity - trade.getTrim1Quantity();
        }

        JsonNode stop = tradeLegMap.get(STOP);
        logger.debug("LottoTradeService.finalizeTrade: Avg. Fill Price (STOP): {}", getPrice(stop));
        double finalStopPrice = getPrice(stop);
        trade.setStopPriceFinal(finalStopPrice);
        trade.setFinalAmount((int) (trade.getFinalAmount() + (totalQuantity * (finalStopPrice * 100))));
        trade.setCloseDate(getCloseDate(stop));
        trade.setPl(trade.getFinalAmount() - trade.getTradeAmount());
        trade.setPostTradeBalance(trade.getPreTradeBalance() + trade.getPl());
    }

}