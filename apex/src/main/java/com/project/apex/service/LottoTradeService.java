package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.*;
import com.project.apex.model.LottoTrade;
import com.project.apex.repository.LottoTradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import static com.project.apex.data.trades.TradeLeg.STOP;
import static com.project.apex.data.trades.TradeLeg.TRIM1;
import static com.project.apex.data.trades.TradeStatus.RUNNERS;
import static com.project.apex.util.Convert.roundedDouble;
import static com.project.apex.util.TradeOrder.*;

@Service
public class LottoTradeService extends TradeService<LottoTrade> implements TradeServiceInterface<LottoTrade> {

    private static final Logger logger = LoggerFactory.getLogger(LottoTradeService.class);

    @Autowired
    public LottoTradeService(AccountService accountService, MarketService marketService, LottoTradeRepository lottoTradeRepository) {
        super(accountService, marketService, lottoTradeRepository);
    }

    @Override
    public void finalizeTrade(LottoTrade trade, TradeLegMap tradeLegMap) {
        logger.debug("LottoTradeService.finalizeTrade: Start: {}", trade.getId());
        int totalQuantity = trade.getQuantity();

        if (tradeLegMap.containsKey(TRIM1)) {
            JsonNode trim1 = tradeLegMap.get(TRIM1);
            logger.debug("LottoTradeService.finalizeTrade: Avg. Fill Price (TRIM1): {}", getAvgFillPrice(trim1));
            double finalTrim1Price = getAvgFillPrice(trim1);
            trade.setTrim1PriceFinal(finalTrim1Price);
            trade.setFinalAmount((int) (trade.getFinalAmount() + (trade.getTrim1Quantity() * (finalTrim1Price * 100))));
            totalQuantity = totalQuantity - trade.getTrim1Quantity();
        }

        JsonNode stop = tradeLegMap.get(STOP);
        logger.debug("LottoTradeService.finalizeTrade: Avg. Fill Price (STOP): {}", getAvgFillPrice(stop));
        double finalStopPrice = getAvgFillPrice(stop);
        trade.setStopPriceFinal(finalStopPrice);
        trade.setFinalAmount((int) (trade.getFinalAmount() + (totalQuantity * (finalStopPrice * 100))));
        trade.setCloseDate(getCloseDate(stop));
        trade.setPl(trade.getFinalAmount() - trade.getTradeAmount());
        trade.setPostTradeBalance(trade.getPreTradeBalance() + trade.getPl());
    }

    @Override
    public void handleOpenTrades(LottoTrade trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades) {
        if (trade.getTrimStatus() < 1 && (lastPrice >= trade.getTrim1Price()) && trade.getRunnersQuantity() > 0) {
            trade.setTrimStatus((byte) 1);
            logger.info("LottoTradeManager.watch: {}: Trim 1 Hit!: {}", riskType, id);
            placeMarketSell(trade, TRIM1);
            trade.setStopPrice(trade.getRunnersFloorPrice());
            trade.setStatus(RUNNERS);
            logger.info("LottoTradeManager.watch: {}: (OPEN -> RUNNERS): {}", riskType, id);
        }

        if (trade.getTrimStatus() > 0) {
            runnerTrades.add(id);
            logger.info("LottoTradeManager.watch: {}: Last Price: {}", riskType, lastPrice);
            logger.info("LottoTradeManager.watch: {}: Last Price > Stop Price: {}", riskType, lastPrice > trade.getStopPrice());
            if (lastPrice > (trade.getStopPrice() + trade.getRunnersDelta())) {
                double newFloor = roundedDouble(lastPrice - trade.getRunnersDelta());
                logger.info("LottoTradeManager.watch: {}: New Floor: {}", riskType, newFloor);
                trade.setStopPrice(newFloor);
            }
        }
    }

}