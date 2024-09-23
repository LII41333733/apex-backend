package com.project.apex.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.*;
import com.project.apex.model.LottoTrade;
import com.project.apex.repository.LottoTradeRepository;
import com.project.apex.service.LottoTradeService;
import com.project.apex.service.MarketService;
import com.project.apex.util.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import static com.project.apex.data.trades.TradeLeg.*;
import static com.project.apex.data.trades.TradeStatus.*;
import static com.project.apex.util.TradeOrder.*;
import static com.project.apex.util.Convert.roundedDouble;

@Component
public class LottoTradeManager {

    private static final Logger logger = LoggerFactory.getLogger(LottoTradeManager.class);
    public final LottoTradeService lottoTradeService;
    public final MarketService marketService;
    public final TradeMap currentTradeMap;
    private final LottoTradeRepository lottoTradeRepository;
    private final Map<Long, LottoTrade> allTrades = new HashMap<>();
    private final List<Long> pendingTrades = new ArrayList<>();
    private final List<Long> openTrades = new ArrayList<>();
    private final List<Long> runnerTrades = new ArrayList<>();
    private final List<Long> filledTrades = new ArrayList<>();
    private final List<Long> canceledTrades = new ArrayList<>();
    private final List<Long> rejectedTrades = new ArrayList<>();

    @Autowired
    public LottoTradeManager(
            LottoTradeService lottoTradeService,
            TradeMap currentTradeMap,
            LottoTradeRepository lottoTradeRepository,
            MarketService marketService) {
        this.lottoTradeService = lottoTradeService;
        this.currentTradeMap = currentTradeMap;
        this.lottoTradeRepository = lottoTradeRepository;
        this.marketService = marketService;
    }

    public void resetLists() {
        allTrades.clear();
        pendingTrades.clear();
        openTrades.clear();
        runnerTrades.clear();
        filledTrades.clear();
        canceledTrades.clear();
        rejectedTrades.clear();
    }

    public TradeRecord<LottoTrade> watch(TradeMap lottoTradeMap) throws IOException, URISyntaxException {
        logger.debug("LottoTradeManager.watch: Start");
        new Record<>("LottoTradeManager.watch: TradeMap: {}", lottoTradeMap);

        resetLists();

        if (!lottoTradeMap.isEmpty()) {
            for (TradeLegMap.Entry<Long, TradeLegMap> lottoOrderEntry : lottoTradeMap.entrySet()) {
                Long id = lottoOrderEntry.getKey();
                Optional<LottoTrade> tradeOpt = lottoTradeRepository.findById(id);
                TradeLegMap tradeLegMap = lottoOrderEntry.getValue();

                if (tradeOpt.isPresent()) {
                    LottoTrade trade = tradeOpt.get();
                    JsonNode fillOrder = tradeLegMap.get(FILL);
                    boolean hasFillOrder = fillOrder != null;

                    if (trade.isNew()) {
                        logger.info("LottoTradeManager.watch: Initializing Trade (NEW): {}", id);
                        trade.initializeTrade(fillOrder);
                        trade.setStatus(PENDING);
                        logger.info("LottoTradeManager.watch: (NEW -> PENDING): {}", id);
                    }

                    if (trade.isPending()) {
                        if (hasFillOrder) {
                            if (isFilled(fillOrder)) {
                                logger.info("BaseTradeManager.watch: Order Filled (PENDING): {}", id);
                                logger.info("BaseTradeManager.watch: Initializing Trade (PENDING): {}", id);
                                trade.initializeTrade(fillOrder);
                                trade.setStatus(OPEN);
                                logger.info("BaseTradeManager.watch: (PENDING -> OPEN): {}", id);
                            } else if (isCanceled(fillOrder)) {
                                canceledTrades.add(id);
                                logger.info("BaseTradeManager.watch: Order Canceled: {}", id);
                            } else if (isRejected(fillOrder)) {
                                rejectedTrades.add(id);
                                logger.info("BaseTradeManager.watch: Order Rejected: {}", id);
                            }
                        } else {
                            pendingTrades.add(id);
                            logger.info("BaseTradeManager.watch: Order Unfilled (PENDING): {}", id);
                        }
                    }

                    if (trade.getStatus().ordinal() < CANCELED.ordinal()) {
                        lottoTradeService.setLastAndMaxPrices(trade);
                        double lastPrice = trade.getLastPrice();

                        if (trade.isOpen() || trade.hasRunners()) {
                            if (trade.getStatus().ordinal() > PENDING.ordinal()) {
                                logger.debug("LottoTradeManager.watch: Updating Stops and Trims: {}", id);

                                if (lastPrice <= trade.getStopPrice()) {
                                    logger.info("LottoTradeManager.watch: Stop Hit!: {}", id);
                                    lottoTradeService.placeMarketSell(trade, STOP);
                                    filledTrades.add(id);
                                    trade.setStatus(FILLED);
                                    logger.info("LottoTradeManager.watch: (OPEN/RUNNERS -> FILLED): {}", id);
                                } else {
                                    openTrades.add(id);
                                    logger.info("LottoTradeManager.watch: Order Open: {}", id);
                                }

                                if (trade.isOpen()) {
                                    if (trade.getTrimStatus() < 1 && (lastPrice >= trade.getTrim1Price())) {
                                        trade.setTrimStatus((byte) 1);
                                        logger.info("LottoTradeManager.watch: Trim 1 Hit!: {}", id);
                                        lottoTradeService.placeMarketSell(trade, TRIM1);
                                        trade.setStopPrice(trade.getRunnersFloorPrice());
                                        trade.setStatus(RUNNERS);
                                        logger.info("LottoTradeManager.watch: (OPEN -> RUNNERS): {}", id);
                                    }

                                    if (trade.getTrimStatus() > 0) {
                                        runnerTrades.add(id);
                                        logger.info("LottoTradeManager.watch: Last Price: {}", lastPrice);
                                        logger.info("LottoTradeManager.watch: Last Price > Stop Price: {}", lastPrice > trade.getStopPrice());
                                        if (lastPrice > (trade.getStopPrice() + trade.getRunnersDelta())) {
                                            double newFloor = roundedDouble(lastPrice - trade.getRunnersDelta());
                                            logger.info("LottoTradeManager.watch: New Floor: {}", newFloor);
                                            trade.setStopPrice(newFloor);
                                        }
                                    }
                                }
                            }
                        } else {
                            filledTrades.add(id);

                            if (!trade.isFinalized()) {
                                lottoTradeService.finalizeTrade(trade, tradeLegMap);
                                trade.setStatus(FINALIZED);
                                logger.info("LottoTradeManager.watch: (FILLED -> FINALIZED): {}", id);
                            }
                        }
                    }

                    allTrades.put(id, trade);
                    lottoTradeRepository.save(trade);
                    logger.debug("LottoTradeManager.watch: Finish: Saving Trade to Repo");
                }
            }

        }

        new Record<>("LottoTradeManager.watch: Completed: Lotto Trades:", allTrades);
        return new TradeRecord<>(allTrades, pendingTrades, openTrades, runnerTrades, filledTrades, canceledTrades, rejectedTrades);
    }
}