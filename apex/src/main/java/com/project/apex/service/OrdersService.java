package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.component.ClientWebSocket;
import com.project.apex.data.trades.*;
import com.project.apex.data.orders.Order;
import com.project.apex.data.orders.OrderSummary;
import com.project.apex.data.trades.BaseTrade.BaseTradeLeg;
import com.project.apex.data.trades.BaseTrade.BaseTradeManager;
import com.project.apex.data.trades.BaseTrade.BaseTradeRecord;
import com.project.apex.model.OtocoTrade;
import com.project.apex.repository.BaseTradeRepository;
import com.project.apex.util.Record;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class OrdersService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersService.class);
    private final AccountService accountService;
    private final ClientWebSocket clientWebSocket;
//    private final BaseTradeRepository otocoTradeRepository;
    private final BaseTradeRepository baseTradeRepository;
    private final MarketService marketService;
    private final BaseTradeService baseTradeService;
    private final BaseTradeManager baseTradeManager;

    private boolean ordersAreEmpty = false;

    @Autowired
    public OrdersService(
            AccountService accountService,
            @Lazy ClientWebSocket clientWebSocket,
            MarketService marketService,
            BaseTradeRepository baseTradeRepository,
            BaseTradeService baseTradeService,
            BaseTradeManager baseTradeManager) {
        this.accountService = accountService;
        this.clientWebSocket = clientWebSocket;
        this.baseTradeRepository = baseTradeRepository;
        this.marketService = marketService;
        this.baseTradeService = baseTradeService;
        this.baseTradeManager = baseTradeManager;
    }

    @PostConstruct
    public void fetchOrders() {
        try {
            logger.info("Fetching orders");
            OrderSummary orderSummary = new OrderSummary();
            JsonNode orders = new ObjectMapper().readTree(accountService.get("/orders?includeTags=true")).get("orders").get("order");

            if (orders == null) {
                ordersAreEmpty = true;
                logger.info("No orders found");
            } else {
                RiskMap ordersByRiskType = handleMapOrdersByRiskType(orders);
                BaseTradeRecord baseTradeRecord = baseTradeManager.watch(ordersByRiskType.get(RiskType.BASE));
//                convertOrdersToLottoTrades(ordersByRiskType.get(RiskType.LOTTO));
//                convertOrdersToOtocoTrades(ordersByRiskType.get(RiskType.OTOCO));
//                orderSummary.mapOrderToSummary(orders);
//                syncTradeRepository(orderSummary);

                if (clientWebSocket.isConnected()) {
                    clientWebSocket.sendData(new Record<>("tradeSummary", new TradeSummary(baseTradeRecord)));
                }
            }

//                List<Order> allOrders = orderSummary.getAllOrders();
//
//                if (!allOrders.isEmpty()) {
//                    String symbols = allOrders.stream()
//                            .map((Order openOrder) -> {
//                                List<Leg> legs = openOrder.getLeg();
//                                return legs.get(0).getOptionSymbol();
//                            }) // Extract the symbol property
//                            .collect(Collectors.joining(",")); // Join with commas
//
//                    JsonNode quotes = marketService.getPrices(symbols);
//
//                    Consumer<JsonNode> processQuote = quote -> {
//                        String symbol = quote.get("symbol").asText();
//                        Double bid = quote.get("bid").asDouble();
//
//                        allOrders.stream()
//                                .filter(order -> order.getLeg().get(0).getOptionSymbol().equals(symbol))
//                                .forEach(order -> order.setLast(bid));
//                    };
//
//                    if (quotes.isObject()) {
//                        processQuote.accept(quotes);
//                    } else {
//                        for (JsonNode node : quotes) {
//                            processQuote.accept(node);
//                        }
//                    }
//                }
//
//                clientWebSocket.sendData(new Record<>("orderSummary", orderSummary));
//            }
        } catch (JsonMappingException e) {
            logger.error(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid tag values for order: {}", e);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

//    @Transactional
//    public void convertOrdersToBaseTrades(Map<MomentCode, Map<TradeLeg, JsonNode>> baseOrders) {
//        logger.info("Converting orders to base trade: {}", baseOrders);
//

//
//
//    }

    public RiskMap handleMapOrdersByRiskType(JsonNode orders) {
        RiskMap ordersByRiskType = new RiskMap();

        Consumer<JsonNode> mapOrders = orderJson -> {
            String[] tag = orderJson.get("tag").asText().split("-");
            RiskType riskType = RiskType.valueOf(tag[0]);
            Long id = Long.valueOf(tag[1]);
            BaseTradeLeg baseTradeLeg = BaseTradeLeg.valueOf(tag[2]);

            ordersByRiskType
                    .computeIfAbsent(riskType, e -> new TradeMap())
                    .computeIfAbsent(id, e -> new TradeLegMap())
                    .put(baseTradeLeg, orderJson);
        };

        if (orders.isObject()) {
            mapOrders.accept(orders);
        } else {
            for (JsonNode order : orders) {
                mapOrders.accept(order);
            }
        }

        return ordersByRiskType;
    }

    @Transactional
    public void convertOrdersToBaseTrades(Map<Long, Map<TradeLeg, JsonNode>> baseOrders) {
        logger.info("Converting orders to base trade: {}", baseOrders);

//        for (Map.Entry<MomentCode, Map<TradeLeg, JsonNode>> baseOrderEntry : baseOrders.entrySet()) {
//            MomentCode momentCode = baseOrderEntry.getKey();
//            Map<TradeLeg, JsonNode> tradeLegMap = baseOrderEntry.getValue();
//
//            for (Map.Entry<TradeLeg, JsonNode> tradeLegEntry : tradeLegMap.entrySet()) {
//                try {
//                    JsonNode jsonData = tradeLegEntry.getValue();
//
//                } catch (Exception e) {
//                    logger.error("Error processing trade: {}", tradeLegEntry, e);
//                }
//            }
//        }

        // Initialized only!!
        baseTradeService.compileSummary();
        // use base trade service to handle open trades,
        // requesting prices and handling trade updates/stops/fills, etc.
    }


//    public void convertOrdersToLottoTrades(Map<MomentCode, Map<TradeLeg, JsonNode>> lottoOrders) {
//    }
//
//    public void convertOrdersToOtocoTrades(Map<MomentCode, Map<TradeLeg, JsonNode>> otocoOrders) {
//    }

//    @Transactional
//    public void syncTradeRepository(OrderSummary orderSummary) {
//        List<Order> orders = orderSummary.getAllOrders();
//
//        orders.sort(Comparator.comparing(Order::getCreateDate));
//
//        for (Order order : orders) {
//            Optional<Trade> tradeEntity = tradeRepository.findById(order.getId());
//
//            if (tradeEntity.isPresent()) {
//                Trade trade = tradeEntity.get();
//
//                if (!trade.isFinalized()) {
//                    if (trade.getStatus() == null) {
//                        convertOrdersToTrades(order, trade);
//                    }
//
//                    if ("canceled".equals(order.getStatus())) {
////                        handleCanceledOrders(trade);
//                    }
//
//                    if ("filled".equals(order.getStatus())) {
//                        finalizeOtocoTrade(order, trade);
//                    }
//
//                    trade.setStatus(order.getStatus());
//                    tradeRepository.save(trade);
//                }
//            } else {
////                Trade trade = new Trade();
////                convertOrdersToTrades(order, trade);
////                tradeRepository.save(trade);
//            }
//        }
//    }

    public void convertOrdersToTrades(Order order, OtocoTrade trade) {
        Leg triggerLeg = order.getLeg().get(0);
        Leg limitLeg = order.getLeg().get(1);
        Leg stopLeg = order.getLeg().get(2);
        trade.setOrderId(order.getId());
        trade.setOptionSymbol(triggerLeg.getOptionSymbol());
        trade.setSymbol(triggerLeg.getSymbol());
        trade.setLossStreak(0);
        trade.setStopPrice(stopLeg.getStopPrice());
        trade.setLimitPrice(limitLeg.getPrice());
        trade.setFillPrice(triggerLeg.getPrice());
        trade.setOpenDate(LocalDateTime.ofInstant(Instant.parse(order.getCreateDate()), ZoneId.of("America/New_York")));
        trade.setQuantity(triggerLeg.getQuantity());
    }

//    public void finalizeOtocoTrade(Order order, OtocoTrade trade) {
//        List<OtocoTrade> trades = otocoTradeRepository.findLastFinalizedTrade();
//        OtocoTrade lastFinalizedTrade = trades.isEmpty() ? null : trades.get(0);
//
//        Leg limitLeg = order.getLeg().get(1);
//        Leg stopLeg = order.getLeg().get(2);
//
//        boolean isWin = "filled".equals(limitLeg.getStatus());
//        Leg resultLeg = isWin ? limitLeg : stopLeg;
//        trade.setStatus(order.getStatus());
//
//        if (isWin) {
//            double pl = (resultLeg.getPrice() * 100) * resultLeg.getQuantity();
//            trade.setPl(pl);
//            trade.setLossStreak(0);
//
//
//        } else {
//            double pl = (resultLeg.getStopPrice() * 100) * resultLeg.getQuantity();
//            trade.setPl(-pl);
//
//            if (lastFinalizedTrade != null) {
//                trade.setLossStreak(lastFinalizedTrade.getLossStreak() + 1);
//            } else {
//                trade.setLossStreak(1);
//            }
//        }
//
//        trade.setCloseDate(LocalDateTime.ofInstant(Instant.parse(order.getTransactionDate()), ZoneId.of("America/New_York")));
//        trade.setFinalized(true);
//    }
}
//
//    public void handleCanceledOrders(OtocoTrade trade) {
//        if (trade.getRecoveryId() != null) {
//            OtocoTrade lossIdTrade = tradeRepository.findTradeByLossId(trade.getRecoveryId());
//            if (lossIdTrade != null) {
//                lossIdTrade.setLossId(null);
//                tradeRepository.save(lossIdTrade);
//            }
//            trade.setRecoveryId(null);
//        }
//        trade.setFinalized(true);
//    }