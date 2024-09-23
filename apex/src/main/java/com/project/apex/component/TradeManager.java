package com.project.apex.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.*;
import com.project.apex.model.Trade;
import com.project.apex.service.MarketService;
import com.project.apex.util.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import static com.project.apex.data.trades.TradeLeg.*;
import static com.project.apex.data.trades.TradeStatus.*;
import static com.project.apex.util.TradeOrder.*;

public abstract class TradeManager<T extends Trade, R extends JpaRepository<T, Long>, S extends TradeManagerInterface<T>> {

    private static final Logger logger = LoggerFactory.getLogger(TradeManager.class);
    protected final R tradeRepository;
    protected final S tradeService;
    protected final MarketService marketService;
    private final Map<Long, T> allTrades = new HashMap<>();
    private final List<Long> pendingTrades = new ArrayList<>();
    private final List<Long> openTrades = new ArrayList<>();
    final List<Long> runnerTrades = new ArrayList<>();
    private final List<Long> filledTrades = new ArrayList<>();
    private final List<Long> canceledTrades = new ArrayList<>();
    private final List<Long> rejectedTrades = new ArrayList<>();

    public TradeManager(S tradeService, R tradeRepository, MarketService marketService) {
        this.marketService = marketService;
        this.tradeRepository = tradeRepository;
        this.tradeService = tradeService;
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

    public TradeRecord<T> watch(TradeMap tradeMap, RiskType riskType) throws IOException, URISyntaxException {
        logger.debug("TradeManager.watch: Start: Risk Type: {}", riskType);
        new Record<>("TradeManager.watch: " + riskType + ": TradeMap: {}", tradeMap);

        resetLists();

        if (tradeMap != null && !tradeMap.isEmpty()) {
            for (TradeLegMap.Entry<Long, TradeLegMap> baseOrderEntry : tradeMap.entrySet()) {
                Long id = baseOrderEntry.getKey();
                Optional<T> tradeOpt = tradeRepository.findById(id);
                TradeLegMap tradeLegMap = baseOrderEntry.getValue();

                if (tradeOpt.isPresent()) {
                    T trade = tradeOpt.get();
                    JsonNode fillOrder = tradeLegMap.get(FILL);
                    boolean hasFillOrder = fillOrder != null;

                    if (hasFillOrder) {
                        if (trade.isNew()) {
                            logger.info("TradeManager.watch: {}: Initializing Trade (NEW): {}", riskType, id);
                            trade.initializeTrade(fillOrder);
                            trade.setStatus(PENDING);
                            logger.info("TradeManager.watch: {}: (NEW -> PENDING): {}", riskType, id);
                        }

                        if (trade.isPending()) {
                            if (isFilled(fillOrder)) {
                                logger.info("TradeManager.watch: {}: Order Filled (PENDING): {}", riskType, id);
                                logger.info("TradeManager.watch: {}: Initializing Trade (PENDING): {}", riskType, id);
                                trade.initializeTrade(fillOrder);
                                trade.setStatus(OPEN);
                                logger.info("TradeManager.watch: {}: (PENDING -> OPEN): {}", riskType, id);
                            } else if (isCanceled(fillOrder)) {
                                canceledTrades.add(id);
                                logger.info("TradeManager.watch: {}: Order Canceled: {}", riskType, id);
                            } else if (isRejected(fillOrder)) {
                                rejectedTrades.add(id);
                                logger.info("TradeManager.watch: {}: Order Rejected: {}", riskType, id);
                            } else {
                                pendingTrades.add(id);
                                logger.info("TradeManager.watch: {}: Order Unfilled (PENDING): {}", riskType, id);
                            }
                        }

                        if (trade.getStatus().ordinal() < CANCELED.ordinal()) {
                            tradeService.setLastAndMaxPrices(trade);
                            double lastPrice = trade.getLastPrice();

                            if (trade.isOpen() || trade.hasRunners()) {
                                if (trade.getStatus().ordinal() > PENDING.ordinal()) {
                                    logger.debug("TradeManager.watch: {}: Updating Stops and Trims: {}", riskType, id);

                                    if (lastPrice <= trade.getStopPrice()) {
                                        logger.info("TradeManager.watch: {}: Stop Hit!: {}", riskType, id);
                                        if (tradeService.placeMarketSell(trade, STOP)) {
                                            filledTrades.add(id);
                                            trade.setStatus(FILLED);
                                            logger.info("TradeManager.watch: {}: (OPEN/RUNNERS -> FILLED): {}", riskType, id);
                                        }
                                    } else {
                                        openTrades.add(id);
                                        logger.info("TradeManager.watch: {}: Order Open: {}", riskType, id);
                                    }

                                    if (trade.isOpen()) {
                                       handleOpenTrades(trade, lastPrice, id, riskType);
                                    }
                                }
                            }

                            if (trade.isFilled()) {
                                filledTrades.add(id);

                                if (!trade.isFinalized()) {
                                    tradeService.finalizeTrade(trade, tradeLegMap);
                                    trade.setStatus(FINALIZED);
                                    logger.info("TradeManager.watch: {}: (FILLED -> FINALIZED): {}", riskType, id);
                                }
                            }
                        }

                        allTrades.put(id, trade);
                        tradeRepository.save(trade);
                        logger.debug("TradeManager.watch: {}: Finish: Saving Trade to Repo", riskType);
                    } else {
                        logger.error("TradeManager.watch: {}: Trade exists but no FILL order available. ID: {}", riskType, id);
                    }
                }
            }
        }

        new Record<>("TradeManager.watch: " + riskType + ": Completed! Trades:", allTrades);
        return new TradeRecord<T>(allTrades, pendingTrades, openTrades, runnerTrades, filledTrades, canceledTrades, rejectedTrades);
    }

    public void handleOpenTrades(T trade, double lastPrice, Long id, RiskType riskType) {}
}