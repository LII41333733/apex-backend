package com.project.apex.data.trades.BaseTrade;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.*;
import com.project.apex.model.BaseTrade;
import com.project.apex.repository.BaseTradeRepository;
import com.project.apex.service.BaseTradeService;
import com.project.apex.service.MarketService;
import com.project.apex.util.BaseTradeOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

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

    public void placeTrade(BuyData buyData) throws IOException {
        baseTradeService.placeFillOrder(buyData);
    }

    public BaseTradeRecord watch(Map<Long, TradeLegMap> newTradeMap) throws IOException, URISyntaxException {
        for (Map.Entry<Long, TradeLegMap> baseOrderEntry : newTradeMap.entrySet()) {
            Long id = baseOrderEntry.getKey();
            Optional<BaseTrade> tradeOpt = baseTradeRepository.findById(id);
            Map<TradeLeg, JsonNode> tradeLegMap = baseOrderEntry.getValue();

            if (tradeOpt.isPresent()) {
                BaseTrade trade = tradeOpt.get();
                allTrades.put(id, trade);

                if (trade.getStatus() == BaseTradeStatus.PENDING) {
                    JsonNode fillOrder = tradeLegMap.get(BaseTradeLeg.FILL);

                    if (BaseTradeOrder.isFilled(fillOrder)) {
                        baseTradeService.initializeTrade(trade, fillOrder, BaseTradeStatus.PRE_OPEN);
                        baseTradeService.placeStopOrder(trade);
                        openTrades.add(id);
                    } else if (BaseTradeOrder.isCanceled(fillOrder)) {
                        canceledTrades.add(id);
                    } else {
                        pendingTrades.add(id);
                    }
                } else if (trade.getStatus() == BaseTradeStatus.CANCELED) {
                    canceledTrades.add(id);
                } else if (trade.getStatus() == BaseTradeStatus.REJECTED) {
                    rejectedTrades.add(id);
                }

                if (trade.getStatus().ordinal() < BaseTradeStatus.CANCELED.ordinal()) {
                    baseTradeService.setPrices(trade);

                    if (trade.getStatus().ordinal() > BaseTradeStatus.PRE_OPEN.ordinal()) {
                        JsonNode trim1Order = tradeLegMap.get(BaseTradeLeg.TRIM1);
                        JsonNode trim2Order = tradeLegMap.get(BaseTradeLeg.TRIM2);
                        JsonNode stopOrder = tradeLegMap.get(BaseTradeLeg.STOP);
                        Integer stopOrderId = BaseTradeOrder.getId(stopOrder);

                        if (trade.getTrimStatus() < 1 && BaseTradeOrder.isFilled(trim1Order)) {
                            trade.setTrimStatus((byte) 1);
                        }

                        if (trade.getTrimStatus() < 2 && BaseTradeOrder.isFilled(trim2Order)) {
                            trade.setTrimStatus((byte) 2);
                            baseTradeService.modifyStopOrder(stopOrderId, trade.getRunnersFloorPrice(), trade);
                        }

                        if (trade.getStatus() != BaseTradeStatus.FILLED) {
                            if (BaseTradeOrder.isFilled(stopOrder)) {
                                filledTrades.add(id);
                                trade.setStatus(BaseTradeStatus.FILLED);

                                if (trade.getTrimStatus() > 0) {
                                    trade.setPostTradeBalance(trade.getPreTradeBalance() + trade.getTradeAmount());
                                } else {
                                    trade.setPostTradeBalance(trade.getPreTradeBalance() - trade.getTradeAmount());
                                }
                            } else if (trade.getTrimStatus() > 1) {
                                runnerTrades.add(id);
                                trade.setStatus(BaseTradeStatus.RUNNERS);
                                double last = trade.getLastPrice();

                                if (last > trade.getRunnersFloorPrice()) {
                                    double newFloor = last - trade.getRunnersDelta();
                                    trade.setRunnersFloorPrice(newFloor);
                                    baseTradeService.modifyStopOrder(stopOrderId, newFloor, trade);
                                }
                            }
                        }
                    }
                }

                baseTradeRepository.save(trade);
            }
        }

        return new BaseTradeRecord(allTrades, pendingTrades, openTrades, runnerTrades, filledTrades, canceledTrades, rejectedTrades);
    }
}