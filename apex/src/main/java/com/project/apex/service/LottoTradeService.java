package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.component.TradeManagerInterface;
import com.project.apex.config.EnvConfig;
import com.project.apex.data.account.Balance;
import com.project.apex.data.orders.OrderFillRecord;
import com.project.apex.data.trades.*;
import com.project.apex.model.BaseTrade;
import com.project.apex.model.LottoTrade;
import com.project.apex.model.Trade;
import com.project.apex.repository.LottoTradeRepository;
import com.project.apex.util.Convert;
import com.project.apex.util.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static com.project.apex.data.trades.TradeLeg.STOP;
import static com.project.apex.data.trades.TradeLeg.TRIM1;
import static com.project.apex.data.trades.TradeStatus.RUNNERS;
import static com.project.apex.util.Convert.roundedDouble;
import static com.project.apex.util.TradeOrder.*;

@Service
public class LottoTradeService extends TradeService {

    /**
     * For base trades, on the UI, hide all options with asks less than .11
     * They should be saved for lottos
     */

    private static final Logger logger = LoggerFactory.getLogger(LottoTradeService.class);

    @Autowired
    public LottoTradeService(
            AccountService accountService,
            EnvConfig envConfig,
            MarketService marketService,
            TradeFactory tradeFactory,
            LottoTradeRepository lottoTradeRepository
    ) {
        super(accountService, envConfig, marketService, tradeFactory, lottoTradeRepository);
    }

    @Override
    public void finalizeTrade(Trade trade, TradeLegMap tradeLegMap) {
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
    public void handleOpenTrades(Trade trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades) {
        if (trade.getTrimStatus() < 1 && (lastPrice >= trade.getTrim1Price())) {
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