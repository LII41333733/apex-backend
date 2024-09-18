package com.project.apex.data.trades.BaseTrade;

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

import static com.project.apex.data.trades.BaseTrade.BaseTradeLeg.*;
import static com.project.apex.data.trades.BaseTrade.BaseTradeStatus.*;
import static com.project.apex.util.BaseTradeOrder.*;
import static com.project.apex.util.Convert.roundedDouble;

@Component
public class BaseTradeManager {

    private static final Logger logger = LoggerFactory.getLogger(BaseTradeManager.class);
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
    public BaseTradeManager(
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

    public BaseTradeRecord watch(Map<Long, TradeLegMap> baseTradeMap) throws IOException, URISyntaxException {
        logger.info("BaseTradeManager.watch: Start");
        new Record<>("BaseTradeManager.watch: TradeMap: {}", baseTradeMap);

        resetLists();

        for (Map.Entry<Long, TradeLegMap> baseOrderEntry : baseTradeMap.entrySet()) {
            Long id = baseOrderEntry.getKey();
            Optional<BaseTrade> tradeOpt = baseTradeRepository.findById(id);
            Map<TradeLeg, JsonNode> tradeLegMap = baseOrderEntry.getValue();

            if (tradeOpt.isPresent()) {
                BaseTrade trade = tradeOpt.get();
                JsonNode fillOrder = tradeLegMap.get(FILL);
                JsonNode trim1Order = tradeLegMap.get(TRIM1);

                if (trade.getStatus() == NEW) {
                    logger.info("BaseTradeManager.watch: Initializing Trade (NEW): {}", id);
                    trade.initializeTrade(fillOrder);
                    trade.setStatus(PENDING);
                    logger.info("BaseTradeManager.watch: (NEW -> PENDING): {}", id);
                }

                if (trade.getStatus() == PENDING) {
                    if (isFilled(fillOrder)) {
                        logger.info("BaseTradeManager.watch: Order Filled (PENDING): {}", id);
                        logger.info("BaseTradeManager.watch: Initializing Trade (PENDING): {}", id);
                        trade.initializeTrade(fillOrder);
                        trade.setStatus(PREOPEN);
                        logger.info("BaseTradeManager.watch: (PENDING -> PREOPEN): {}", id);
                    } else {
                        pendingTrades.add(id);
                        logger.info("BaseTradeManager.watch: Order Unfilled (PENDING): {}", id);
                    }
                }

                if (trade.getStatus() == PREOPEN && !isRejected(trim1Order)) {
                    baseTradeService.placeTrims(trade);
                    openTrades.add(id);
                    trade.setStatus(OPEN);
                    logger.info("BaseTradeManager.watch: (PREOPEN -> OPEN): {}", id);
                }

                if (isCanceled(fillOrder) && !canceledTrades.contains(id)) {
                    canceledTrades.add(id);
                    logger.info("BaseTradeManager.watch: Order Canceled: {}", id);
                } else if (trade.getStatus() == REJECTED && !rejectedTrades.contains(id)) {
                    rejectedTrades.add(id);
                    logger.info("BaseTradeManager.watch: Order Rejected: {}", id);
                }

                if (trade.getStatus().ordinal() < CANCELED.ordinal()) {
                    baseTradeService.setLastAndMaxPrices(trade);

                    if (trade.getStatus().ordinal() > PREOPEN.ordinal()) {
                        logger.info("BaseTradeManager.watch: Updating Trim Statuses: {}", id);
                        JsonNode trim2Order = tradeLegMap.get(TRIM2);
                        JsonNode runnersOrder = tradeLegMap.get(TRIM3);
                        Integer stopOrderId = getId(runnersOrder);

                        if (trade.getTrimStatus() < 1 && isFilled(trim1Order)) {
                            trade.setTrimStatus((byte) 1);
                            logger.info("BaseTradeManager.watch: Trim 1 Hit!: {}", id);
                        }

                        if (trim2Order != null && trade.getTrimStatus() < 2 && isFilled(trim2Order)) {
                            trade.setTrimStatus((byte) 2);
                            logger.info("BaseTradeManager.watch: Trim 2 Hit! Moving Stops: {}", id);
                            baseTradeService.modifyStopOrder(stopOrderId, trade.getRunnersFloorPrice(), trade);
                        }

                        if (trade.getStatus() != FILLED) {
                            boolean allOrdersFilled = !isOpen(trim1Order) && !isOpen(trim2Order) && !isOpen(runnersOrder);

                            if (allOrdersFilled) {
                                logger.info("BaseTradeManager.watch: All Orders Filled {}", id);
                                filledTrades.add(id);
                                trade.setStatus(FILLED);
                                logger.info("BaseTradeManager.watch: (OPEN -> FILLED): {}", id);
                                baseTradeService.finalizeTrade(trade);
                            } else if (trade.getTrimStatus() > 1) {
                                runnerTrades.add(id);
                                trade.setStatus(RUNNERS);
                                logger.info("BaseTradeManager.watch: (OPEN -> RUNNERS): {}", id);
                                double last = trade.getLastPrice();
                                logger.info("BaseTradeManager.watch: Last Price: {}", last);
                                logger.info("BaseTradeManager.watch: Last Price > Runners Floor: {}", last > trade.getRunnersFloorPrice());
                                if (last > trade.getRunnersFloorPrice()) {
                                    double newFloor = roundedDouble(last - trade.getRunnersDelta());
                                    logger.info("BaseTradeManager.watch: New Floor: {}", newFloor);
                                    trade.setRunnersFloorPrice(newFloor);
                                    baseTradeService.modifyStopOrder(stopOrderId, newFloor, trade);
                                }
                            }
                        }
                    }
                }

                allTrades.put(id, trade);
                baseTradeRepository.save(trade);
                logger.info("BaseTradeManager.watch: Finish: Saving Trade to Repo");
            }
        }

        new Record<>("BaseTradeManager.watch: Completed: Base Trades:", allTrades);
        return new BaseTradeRecord(allTrades, pendingTrades, openTrades, runnerTrades, filledTrades, canceledTrades, rejectedTrades);
    }
}