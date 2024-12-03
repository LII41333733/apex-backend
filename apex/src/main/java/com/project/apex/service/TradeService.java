package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.account.Balance;
import com.project.apex.data.orders.OrderFillRecord;
import com.project.apex.data.trades.*;
import com.project.apex.interfaces.Trim1Tradeable;
import com.project.apex.interfaces.Trim2Tradeable;
import com.project.apex.model.Trade;
import com.project.apex.model.VisionTrade;
import com.project.apex.repository.TradeRepository;
import com.project.apex.util.Convert;
import com.project.apex.util.Record;
import com.project.apex.util.TradeOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import static com.project.apex.data.trades.TradeLeg.FILL;
import static com.project.apex.data.trades.TradeLeg.STOP;
import static com.project.apex.data.trades.TradeStatus.*;
import static com.project.apex.util.TradeOrder.*;
import static com.project.apex.util.TradeOrder.isRejected;

@Component
public abstract class TradeService<T extends Trade> implements TradeServiceInterface<T>  {

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

    public void placeTrade(T trade, BuyDataRecord buyDataRecord) throws Exception {
        RiskType riskType = buyDataRecord.riskType();
        String riskTypeName = riskType.name();
        String error;
        Long id = Convert.getMomentAsCode();
        logger.info("TradeService.placeTrade: Start: {}", id);
        Balance balance = accountService.getBalanceData();
        double totalEquity = balance.getTotalEquity();
        logger.info("Total Equity: {}", totalEquity);
        double totalCash = balance.getTotalCash();
        logger.info("Total Cash: {}", totalCash);
        int tradeAllotment =
                trade instanceof VisionTrade ? 100 : (int) Math.floor(totalEquity * trade.getTradeAmountPercentage());
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
            if (quantity < 1) {
                throw new Exception("Not enough BP for this trade");
            }
            new Record<>("TradeService.placeTrade: Fill Parameters", new OrderFillRecord(
                id,
                totalEquity,
                totalCash,
                tradeAllotment,
                buyDataRecord.price(),
                contractCost,
                quantity,
                parameters
            ));
            trade.setId(id);
            trade.setPreTradeBalance(totalEquity);
            trade.setInitialAsk(ask);
            trade.setFillPrice(ask);
            trade.setQuantity(quantity);
            this.calculateStopsAndTrims(trade);
            Long orderId = accountService.placeOrder(trade.getId(), parameters,"TradeService.placeTrade");
            trade.setFillOrderId(orderId);
            tradeRepository.save(trade);
        } else {
            error = "TradeService.placeTrade: Not enough cash available to make trade: {}";
            logger.error(error, id);
            throw new Exception(error);
        }
    }

    public void modifyTrade(ModifyTradeRecord modifyTradeRecord) {
        Long id = modifyTradeRecord.id();
        TradeLeg tradeLeg = modifyTradeRecord.tradeLeg();
        double oldPrice;
        double newPrice = modifyTradeRecord.price();
        Optional<T> tradeOpt = tradeRepository.findById(id);
        if (tradeOpt.isPresent()) {
            T trade = tradeOpt.get();
            switch (tradeLeg) {
                case STOP: {
                    oldPrice = trade.getStopPrice();
                    trade.setStopPrice(newPrice);
                };
                break;
                case TRIM1: {
                    if (trade instanceof Trim1Tradeable trim1Tradeable) {
                        oldPrice = trim1Tradeable.getTrim1Price();
                        trim1Tradeable.setTrim1Price(newPrice);
                    }
                }
                case TRIM2: {
                    if (trade instanceof Trim2Tradeable trim2Tradeable) {
                        oldPrice = trim2Tradeable.getTrim2Price();
                        trim2Tradeable.setTrim2Price(newPrice);
                    }
                }
            }
            tradeRepository.save(trade);
        }
    }

    public boolean placeMarketSell(T trade, TradeLeg tradeLeg, Map<String, String> parameters) {
        logger.info("TradeService.placeMarketSell: Start");
        boolean result = true;
        try {
            parameters.put("class", "option");
            parameters.put("duration", "day");
            parameters.put("type", "market");
            parameters.put("option_symbol", trade.getOptionSymbol());
            parameters.put("side", "sell_to_close");
            parameters.put("tag", trade.getRiskType().name() + "-" + trade.getId() + "-" + tradeLeg.name());
            new Record<>("TradeService.placeMarketSell: Parameters:", parameters);
            JsonNode response = accountService.post("/orders", parameters);
            JsonNode order = response.get("order");
            if (isOk(order)) {
                logger.info("TradeService.placeMarketSell: Market Sell Successful: {}", trade.getId());
            } else {
                JsonNode err = response.get("errors").get("error");
                String err1 = err.get(0).asText();
                String err2 = err.get(1).asText();
                logger.error("TradeService.placeMarketSell: Market Sell UnSuccessful: {} Tradier Error: {} - {}", trade.getId(), err1, err2);
            }
        } catch (Exception e) {
            logger.error("TradeService.placeMarketSell: ERROR: Exception: {}, ID: {}", e.getMessage(), trade.getId(), e);
            result = false;
        }
        return result;
    }

    public TradeRecord<T> watch(RiskType riskType, TradeMap tradeMap) throws IOException, URISyntaxException {
        logger.debug("TradeManager.watch: Start: Risk Type: {}", riskType);
        List<T> allTrades = new ArrayList<>();
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
                    boolean hasStopOrder = stopOrder != null;
                    boolean isOpen = trade.isOpen() || trade.hasRunners();
                    boolean isFilled = trade.isFilled() || trade.isFinalized();
                    if (trade.isNew()) {
                        logger.info("TradeManager.watch: {}: Initializing Trade (NEW): {}", riskType, id);
                        trade.setOpenDate(TradeOrder.getCreateDate(fillOrder));
                        trade.setOptionSymbol(TradeOrder.getOptionSymbol(fillOrder));
                        trade.setSymbol(TradeOrder.getSymbol(fillOrder));
                        trade.setFillPrice(TradeOrder.getPrice(fillOrder));
                        this.calculateStopsAndTrims(trade);
                        trade.setStatus(PENDING);
                        logger.info("TradeManager.watch: {}: (NEW -> PENDING): {}", riskType, id);
                    }
                    if (trade.isPending()) {
                        if (isOpen(fillOrder) || isFilled(fillOrder)) {
                            logger.info("TradeManager.watch: {}: Order Filled (PENDING): {}", riskType, id);
                            logger.info("TradeManager.watch: {}: Initializing Trade (PENDING): {}", riskType, id);
                            trade.setFillPrice(TradeOrder.getPrice(fillOrder));
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

                            if (!hasStopOrder && (lastPrice <= trade.getStopPrice())) {
                                prepareMarketSell(trade, STOP);
                                trade.setStatus(FILLED);
                                logger.info("TradeManager.watch: {}: (OPEN/RUNNERS -> FILLED): {}", riskType, id);
                            } else if (hasStopOrder && isFilled(stopOrder)) {
                                trade.setStatus(FILLED);
                                logger.info("TradeManager.watch: {}: (OPEN/RUNNERS -> FILLED): {}", riskType, id);
                            } else {
                                handleOpenTrades(trade, lastPrice, id, riskType, runnerTrades);
                                openTrades.add(id);
                                logger.info("TradeManager.watch: {}: Order Open: {}", riskType, id);
                            }
                        } else if (isFilled) {
                            filledTrades.add(id);

                            if (trade.isFilled() && !trade.isFinalized() && hasStopOrder) {
                                finalizeTrade(trade, tradeLegMap);
                                trade.setStatus(FINALIZED);
                                logger.info("TradeManager.watch: {}: (FILLED -> FINALIZED): {}", riskType, id);
                            }
                        }
                    }
                    allTrades.add(trade);
                    tradeRepository.save(trade);
                    logger.debug("TradeManager.watch: {}: Finish: Saving Trade to Repo: {}", riskType, id);
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

    public void sellTrade(SellTradeRecord sellTradeRecord) {
        Long id = sellTradeRecord.id();
        Optional<T> tradeOpt = tradeRepository.findById(id);
        if (tradeOpt.isPresent()) {
            T trade = tradeOpt.get();
            prepareMarketSell(trade, STOP);
        }
    }

    public static int getValueByQuantity(int quantity, double price) {
        int contractCost = (int) (price * 100);
        return quantity * contractCost;
    }

    public abstract void calculateStopsAndTrims(T trade);
    public abstract void handleOpenTrades(T trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades);
    public abstract void finalizeTrade(T trade, TradeLegMap tradeLegMap);
    public abstract boolean prepareMarketSell(T trade, TradeLeg tradeLeg);
}
