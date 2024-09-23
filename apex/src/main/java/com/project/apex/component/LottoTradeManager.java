package com.project.apex.component;

import com.project.apex.data.trades.RiskType;
import com.project.apex.model.LottoTrade;
import com.project.apex.repository.LottoTradeRepository;
import com.project.apex.service.LottoTradeService;
import com.project.apex.service.MarketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.project.apex.data.trades.TradeLeg.*;
import static com.project.apex.data.trades.TradeStatus.*;
import static com.project.apex.util.Convert.roundedDouble;

@Component
public class LottoTradeManager extends TradeManager<LottoTrade, LottoTradeRepository, LottoTradeService> {

    private static final Logger logger = LoggerFactory.getLogger(LottoTradeManager.class);
    LottoTradeService lottoTradeService;

    @Autowired
    public LottoTradeManager(
            LottoTradeService lottoTradeService,
            LottoTradeRepository lottoTradeRepository,
            MarketService marketService
    ) {
        super(lottoTradeService, lottoTradeRepository, marketService);
    }

    @Override
    public void handleOpenTrades(LottoTrade trade, double lastPrice, Long id, RiskType riskType) {
        if (trade.getTrimStatus() < 1 && (lastPrice >= trade.getTrim1Price())) {
            trade.setTrimStatus((byte) 1);
            logger.info("LottoTradeManager.watch: {}: Trim 1 Hit!: {}", riskType, id);
            lottoTradeService.placeMarketSell(trade, TRIM1);
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