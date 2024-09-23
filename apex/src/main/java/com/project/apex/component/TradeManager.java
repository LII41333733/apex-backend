package com.project.apex.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.*;
import com.project.apex.model.BaseTrade;
import com.project.apex.repository.BaseTradeRepository;
import com.project.apex.service.BaseTradeService;
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
public class TradeManager {

    private static final Logger logger = LoggerFactory.getLogger(TradeManager.class);
    public final BaseTradeService baseTradeService;
    public final MarketService marketService;
    public final TradeMap currentTradeMap;
    private final BaseTradeRepository baseTradeRepository;
    private final Map<Long, BaseTrade> allTrades = new HashMap<>();
    private final List<Long> pendingTrades = new ArrayList<>();
    private final List<Long> openTrades = new ArrayList<>();
    private final List<Long> runnerTrades = new ArrayList<>();
    private final List<Long> filledTrades = new ArrayList<>();
    private final List<Long> canceledTrades = new ArrayList<>();
    private final List<Long> rejectedTrades = new ArrayList<>();

    @Autowired
    public TradeManager(
            BaseTradeService baseTradeService,
            TradeMap currentTradeMap,
            BaseTradeRepository baseTradeRepository,
            MarketService marketService) {
        this.baseTradeService = baseTradeService;
        this.currentTradeMap = currentTradeMap;
        this.baseTradeRepository = baseTradeRepository;
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

    public TradeRecord<BaseTrade> watch(TradeMap baseTradeMap) throws IOException, URISyntaxException {
        logger.debug("TradeManager.watch: Start");
        new Record<>("TradeManager.watch: TradeMap: {}", baseTradeMap);

        resetLists();

        if (!baseTradeMap.isEmpty()) {
            for (TradeLegMap.Entry<Long, TradeLegMap> baseOrderEntry : baseTradeMap.entrySet()) {
                Long id = baseOrderEntry.getKey();
                Optional<BaseTrade> tradeOpt = baseTradeRepository.findById(id);
                TradeLegMap tradeLegMap = baseOrderEntry.getValue();

                if (tradeOpt.isPresent()) {
                    BaseTrade trade = tradeOpt.get();
                    JsonNode fillOrder = tradeLegMap.get(FILL);
                    boolean hasFillOrder = fillOrder != null;

                    if (hasFillOrder) {
                        if (trade.isNew()) {
                            logger.info("TradeManager.watch: Initializing Trade (NEW): {}", id);
                            trade.initializeTrade(fillOrder);
                            trade.setStatus(PENDING);
                            logger.info("TradeManager.watch: (NEW -> PENDING): {}", id);
                        }

                        if (trade.isPending()) {
                            if (isFilled(fillOrder)) {
                                logger.info("TradeManager.watch: Order Filled (PENDING): {}", id);
                                logger.info("TradeManager.watch: Initializing Trade (PENDING): {}", id);
                                trade.initializeTrade(fillOrder);
                                trade.setStatus(OPEN);
                                logger.info("TradeManager.watch: (PENDING -> OPEN): {}", id);
                            } else if (isCanceled(fillOrder)) {
                                canceledTrades.add(id);
                                logger.info("TradeManager.watch: Order Canceled: {}", id);
                            } else if (isRejected(fillOrder)) {
                                rejectedTrades.add(id);
                                logger.info("TradeManager.watch: Order Rejected: {}", id);
                            } else {
                                pendingTrades.add(id);
                                logger.info("TradeManager.watch: Order Unfilled (PENDING): {}", id);
                            }
                        }

                        if (trade.getStatus().ordinal() < CANCELED.ordinal()) {
                            baseTradeService.setLastAndMaxPrices(trade);
                            double lastPrice = trade.getLastPrice();

                            if (trade.isOpen() || trade.hasRunners()) {
                                if (trade.getStatus().ordinal() > PENDING.ordinal()) {
                                    logger.debug("TradeManager.watch: Updating Stops and Trims: {}", id);

                                    if (lastPrice <= trade.getStopPrice()) {
                                        logger.info("TradeManager.watch: Stop Hit!: {}", id);
                                        baseTradeService.placeMarketSell(trade, STOP);
                                        filledTrades.add(id);
                                        trade.setStatus(FILLED);
                                        logger.info("TradeManager.watch: (OPEN/RUNNERS -> FILLED): {}", id);
                                    } else {
                                        openTrades.add(id);
                                        logger.info("TradeManager.watch: Order Open: {}", id);
                                    }

                                    if (trade.isOpen()) {
                                        if (trade.getTrimStatus() < 1 && (lastPrice >= trade.getTrim1Price())) {
                                            trade.setTrimStatus((byte) 1);
                                            logger.info("TradeManager.watch: Trim 1 Hit!: {}", id);
                                            baseTradeService.placeMarketSell(trade, TRIM1);
                                        }

                                        if (trade.getTrimStatus() < 2 && (lastPrice >= trade.getTrim2Price())) {
                                            trade.setTrimStatus((byte) 2);
                                            logger.info("TradeManager.watch: Trim 2 Hit! Moving Stops: {}", id);
                                            baseTradeService.placeMarketSell(trade, TRIM2);
                                            trade.setStopPrice(trade.getRunnersFloorPrice());
                                            trade.setStatus(RUNNERS);
                                            logger.info("TradeManager.watch: (OPEN -> RUNNERS): {}", id);
                                        }

                                        if (trade.getTrimStatus() > 1) {
                                            runnerTrades.add(id);
                                            logger.info("TradeManager.watch: Last Price: {}", lastPrice);
                                            logger.info("TradeManager.watch: Last Price > Stop Price: {}", lastPrice > trade.getStopPrice());
                                            if (lastPrice > (trade.getStopPrice() + trade.getRunnersDelta())) {
                                                double newFloor = roundedDouble(lastPrice - trade.getRunnersDelta());
                                                logger.info("TradeManager.watch: New Floor: {}", newFloor);
                                                trade.setStopPrice(newFloor);
                                            }
                                        }
                                    }
                                }
                            } else {
                                filledTrades.add(id);

                                if (!trade.isFinalized()) {
                                    baseTradeService.finalizeTrade(trade, tradeLegMap);
                                    trade.setStatus(FINALIZED);
                                    logger.info("TradeManager.watch: (FILLED -> FINALIZED): {}", id);
                                }
                            }
                        }

                        allTrades.put(id, trade);
                        baseTradeRepository.save(trade);
                        logger.debug("TradeManager.watch: Finish: Saving Trade to Repo");
                    } else {
                        logger.error("TradeManager.watch: Trade exists but no FILL order available. ID: {}", id);
                    }
                }
            }
        }
        new Record<>("TradeManager.watch: Completed: Base Trades:", allTrades);
        return new TradeRecord<>(allTrades, pendingTrades, openTrades, runnerTrades, filledTrades, canceledTrades, rejectedTrades);
    }


}