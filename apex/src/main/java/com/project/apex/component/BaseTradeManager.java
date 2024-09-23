package com.project.apex.component;

import com.project.apex.data.trades.RiskType;
import com.project.apex.model.BaseTrade;
import com.project.apex.repository.BaseTradeRepository;
import com.project.apex.service.BaseTradeService;
import com.project.apex.service.MarketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.project.apex.data.trades.TradeLeg.*;
import static com.project.apex.data.trades.TradeStatus.*;
import static com.project.apex.util.Convert.roundedDouble;

@Component
public class BaseTradeManager extends TradeManager<BaseTrade, BaseTradeRepository, BaseTradeService> {

    private static final Logger logger = LoggerFactory.getLogger(BaseTradeManager.class);
    BaseTradeService baseTradeService;

    @Autowired
    public BaseTradeManager(
            BaseTradeRepository baseTradeRepository,
            BaseTradeService baseTradeService,
            MarketService marketService
    ) {
        super(baseTradeService, baseTradeRepository, marketService);
    }

    @Override
    public void handleOpenTrades(BaseTrade trade, double lastPrice, Long id, RiskType riskType) {
        if (trade.getTrimStatus() < 1 && (lastPrice >= trade.getTrim1Price())) {
            trade.setTrimStatus((byte) 1);
            logger.info("BaseTradeManager.watch: {}: Trim 1 Hit!: {}", riskType, id);
            baseTradeService.placeMarketSell(trade, TRIM1);
        }

        if (trade.getTrimStatus() < 2 && (lastPrice >= trade.getTrim2Price())) {
            trade.setTrimStatus((byte) 2);
            logger.info("BaseTradeManager.watch: {}: Trim 2 Hit! Moving Stops: {}", riskType, id);
            baseTradeService.placeMarketSell(trade, TRIM2);
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