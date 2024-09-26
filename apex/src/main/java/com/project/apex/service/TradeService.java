package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.account.Balance;
import com.project.apex.data.orders.OrderFillRecord;
import com.project.apex.data.trades.*;
import com.project.apex.model.Trade;
import com.project.apex.repository.TradeRepository;
import com.project.apex.util.Convert;
import com.project.apex.util.Record;
import com.project.apex.util.TradeOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import static com.project.apex.data.trades.TradeLeg.FILL;
import static com.project.apex.data.trades.TradeLeg.STOP;
import static com.project.apex.data.trades.TradeStatus.*;
import static com.project.apex.util.TradeOrder.*;
import static com.project.apex.util.TradeOrder.isRejected;

public abstract class TradeService<T extends Trade> {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    private final AccountService accountService;
    private final MarketService marketService;
    private final TradeRepository<T> tradeRepository;

    public TradeService(AccountService accountService,
                        MarketService marketService,
                        TradeRepository<T> tradeRepository) {
        this.accountService = accountService;
        this.marketService = marketService;
        this.tradeRepository = tradeRepository;
    }

    public List<T> fetchAllTrades() {
        return tradeRepository.findAll();
    }

    public T placeTrade(T trade, BuyDataRecord buyDataRecord) {
        RiskType riskType = buyDataRecord.riskType();
        String riskTypeName = riskType.name();

        try {
            Long id = Convert.getMomentAsCode();
            logger.info(riskTypeName + ".placeFill: Start: {}", id);
            Balance balance = accountService.getBalanceData();
            double totalEquity = balance.getTotalEquity();
            logger.info("Total Equity: {}", totalEquity);
            double totalCash = balance.getTotalCash();
            logger.info("Total Cash: {}", totalCash);
            int tradeAllotment = (int) Math.floor(totalEquity * trade.getTradePercentModifier());
            logger.info("Trade Allotment: {}", tradeAllotment);
            logger.info("Trade Allotment < Total Cash: {}", tradeAllotment < totalCash);
            if (tradeAllotment < totalCash) {
                double ask = buyDataRecord.price();
                logger.info("Ask: {}", ask);
                double contractCost = ask * 100;
                logger.info("Contract Cost: {}", contractCost);
                int quantity = (int) Math.floor(tradeAllotment / contractCost);
                logger.info("Quantity: {}", quantity);
                Map<String, String> parameters = new HashMap<>();
                parameters.put("class", "option");
                parameters.put("duration", "day");
                parameters.put("quantity", String.valueOf(quantity));
                parameters.put("side", "buy_to_open");
                parameters.put("option_symbol", buyDataRecord.option());
                parameters.put("price", String.valueOf(ask));
                parameters.put("type", "limit");
                parameters.put("tag", riskTypeName + "-" + id + "-" + TradeLeg.FILL);

                new Record<>(riskTypeName + ".placeFill: Fill Parameters", new OrderFillRecord(
                        id,
                        totalEquity,
                        totalCash,
                        tradeAllotment,
                        buyDataRecord.price(),
                        contractCost,
                        quantity,
                        parameters
                ));

                JsonNode json = accountService.post("/orders", parameters);
                JsonNode jsonNode = json.get("order");

                if (isOk(jsonNode)) {
                    logger.info("{}.placeFill: Fill Successful: {}", riskTypeName, id);
                    Long orderId = jsonNode.get("id").asLong();
                    trade.setId(id);
                    trade.setPreTradeBalance(totalEquity);
                    trade.setInitialAsk(ask);
                    trade.setFillPrice(ask);
                    trade.setQuantity(quantity);
                    trade.setFillOrderId(orderId);
                    trade.calculateStopsAndTrims();
                    tradeRepository.save(trade);
                } else {
                    logger.error("{}.placeFill: Fill UnSuccessful: {}", riskTypeName, id);
                }
            } else {
                logger.error(riskTypeName + ".placeFill: Not enough cash available to make trade: {}", id);
            }
        } catch (Exception e) {
            logger.error(riskTypeName + ".placeFill: ERROR: Exception", e);
        }

        return trade;
    }

    public void modifyTrade(ModifyTradeRecord modifyTradeRecord) {
        Long id = modifyTradeRecord.id();
        TradeLeg tradeLeg = modifyTradeRecord.tradeLeg();
        double price = modifyTradeRecord.price();
        Optional<T> tradeOpt = tradeRepository.findById(id);

        if (tradeOpt.isPresent()) {
            T trade = tradeOpt.get();
            switch (tradeLeg) {
                case STOP -> trade.setStopPrice(price);
                case TRIM1 -> trade.setTrim1Price(price);
                case TRIM2 -> trade.setTrim2Price(price);
            }

            tradeRepository.save(trade);
        }
    }

    public void sellTrade(SellTradeRecord sellTradeRecord) {
        Long id = sellTradeRecord.id();
        Optional<T> tradeOpt = tradeRepository.findById(id);

        if (tradeOpt.isPresent()) {
            T trade = tradeOpt.get();
            placeMarketSell(trade, STOP);
        }
    }

    public boolean placeMarketSell(T trade, TradeLeg TradeLeg) {
        logger.info("BaseTradeService.placeMarketSell: Start");
        boolean result = true;

        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("class", "option");
            parameters.put("duration", "day");
            parameters.put("type", "market");
            parameters.put("option_symbol", trade.getOptionSymbol());
            parameters.put("side", "sell_to_close");
            parameters.put("tag", trade.getRiskType().name() + "-" + trade.getId() + "-" + TradeLeg.name());

            switch (TradeLeg) {
                case TRIM1 -> parameters.put("quantity", String.valueOf(trade.getTrim1Quantity()));
                case TRIM2 -> parameters.put("quantity", String.valueOf(trade.getTrim2Quantity()));
                case STOP -> {
                    switch (trade.getTrimStatus()) {
                        case 0 ->
                                parameters.put("quantity", String.valueOf(trade.getTrim1Quantity() + trade.getTrim2Quantity() + trade.getRunnersQuantity()));
                        case 1 ->
                                parameters.put("quantity", String.valueOf(trade.getTrim2Quantity() + trade.getRunnersQuantity()));
                        case 2 -> parameters.put("quantity", String.valueOf(trade.getRunnersQuantity()));
                    }
                }
            }

            new Record<>("BaseTradeService.placeMarketSell: Parameters:", parameters);

            JsonNode response = accountService.post("/orders", parameters);
            JsonNode order = response.get("order");

            if (isOk(order)) {
                logger.info("BaseTradeService.placeMarketSell: Market Sell Successful: {}", trade.getId());
            } else {
                logger.error("BaseTradeService.placeMarketSell: Market Sell UnSuccessful: {}", trade.getId());
            }
        } catch (Exception e) {
            logger.error("BaseTradeService.placeMarketSell: ERROR: Exception: {}, ID: {}", e.getMessage(), trade.getId(), e);
            result = false;
        }

        return result;
    }

    public TradeRecord<T> watch(RiskType riskType, TradeMap tradeMap) throws IOException, URISyntaxException {
        logger.debug("TradeManager.watch: Start: Risk Type: {}", riskType);
        Map<Long, T> allTrades = new HashMap<>();
        List<Long> pendingTrades = new ArrayList<>();
        List<Long> openTrades = new ArrayList<>();
        List<Long> runnerTrades = new ArrayList<>();
        List<Long> filledTrades = new ArrayList<>();
        List<Long> canceledTrades = new ArrayList<>();
        List<Long> rejectedTrades = new ArrayList<>();
        new Record<>("TradeManager.watch: " + riskType + ": TradeMap: {}", tradeMap);

        if (tradeMap != null && !tradeMap.isEmpty()) {
            for (TradeLegMap.Entry<Long, TradeLegMap> baseOrderEntry : tradeMap.entrySet()) {
                Long id = baseOrderEntry.getKey();
                Optional<T> tradeOpt = tradeRepository.findById(id);
                TradeLegMap tradeLegMap = baseOrderEntry.getValue();

                if (tradeOpt.isPresent()) {
                    T trade = tradeOpt.get();
                    JsonNode fillOrder = tradeLegMap.get(FILL);
                    JsonNode stopOrder = tradeLegMap.get(STOP);
                    boolean hasFillOrder = fillOrder != null;
                    boolean hasStopOrder = stopOrder != null;
                    boolean isOpen = trade.isOpen() || trade.hasRunners();

                    if (hasFillOrder) {
                        if (trade.getFillOrderId() == null) {
                            trade.setFillOrderId(fillOrder.get("id").asLong());
                        }

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
                                trade.setStatus(CANCELED);
                                logger.info("TradeManager.watch: {}: (PENDING -> CANCELED): {}", riskType, id);
                            } else if (isRejected(fillOrder)) {
                                rejectedTrades.add(id);
                                logger.info("TradeManager.watch: {}: Order Rejected: {}", riskType, id);
                            } else {
                                pendingTrades.add(id);
                                logger.info("TradeManager.watch: {}: Order Unfilled (PENDING): {}", riskType, id);
                            }
                        }

                        if (trade.getStatus().ordinal() < CANCELED.ordinal()) {
                            setLastAndMaxPrices(trade);
                            double lastPrice = trade.getLastPrice();

                            if (isOpen) {
                                logger.debug("TradeManager.watch: {}: Updating Stops and Trims: {}", riskType, id);

                                if ((hasStopOrder && TradeOrder.isFilled(stopOrder))
                                        || (lastPrice <= trade.getStopPrice() && placeMarketSell(trade, STOP))) {
                                    trade.setStatus(FILLED);
                                    logger.info("TradeManager.watch: {}: (OPEN/RUNNERS -> FILLED): {}", riskType, id);
                                } else {
                                    handleOpenTrades(trade, lastPrice, id, riskType, runnerTrades);
                                    openTrades.add(id);
                                    logger.info("TradeManager.watch: {}: Order Open: {}", riskType, id);
                                }
                            }

                            if (trade.isFilled()) {
                                filledTrades.add(id);

                                if (!trade.isFinalized()) {
                                    finalizeTrade(trade, tradeLegMap);
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
        return new TradeRecord<>(allTrades, pendingTrades, openTrades, runnerTrades, filledTrades, canceledTrades, rejectedTrades);
    }

    public void setLastAndMaxPrices(T trade) throws IOException, URISyntaxException {
        logger.debug("BaseTradeService.setLastAndMaxPrices: Start: {}", trade.getId());
        JsonNode quote = marketService.getPrices(trade.getOptionSymbol());
        double bid = quote.get("bid").asDouble();
        double tradeMaxPrice = trade.getMaxPrice();
        trade.setLastPrice(bid);
        trade.setMaxPrice(Math.max(tradeMaxPrice, bid));
    }

    public void handleOpenTrades(T trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades) {}

    public void finalizeTrade(T trade, TradeLegMap tradeLegMap) {}
}