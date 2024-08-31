package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.component.ClientWebSocket;
import com.project.apex.data.Leg;
import com.project.apex.data.Order;
import com.project.apex.data.orders.OrderSummary;
import com.project.apex.model.Trade;
import com.project.apex.repository.TradeRepository;
import com.project.apex.util.Record;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class OrdersService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersService.class);
    private final AccountService accountService;
    private final ClientWebSocket clientWebSocket;
    private final TradeRepository tradeRepository;
    private boolean ordersAreEmpty = false;

    @Autowired
    public OrdersService(AccountService accountService,
                         @Lazy ClientWebSocket clientWebSocket,
                         TradeRepository tradeRepository) {
        this.accountService = accountService;
        this.clientWebSocket = clientWebSocket;
        this.tradeRepository = tradeRepository;
    }

    @PostConstruct
    public void fetchOrders() {
       try {
           logger.info("Fetching orders");
           OrderSummary orderSummary = new OrderSummary();
           JsonNode orders = new ObjectMapper().readTree(accountService.get("/orders")).get("orders").get("order");

           if (orders == null) {
               logger.info("No orders found");
               ordersAreEmpty = true;
           } else {
               ordersAreEmpty = false;
               orderSummary.update(orders);
               syncTradeRepository(orderSummary);
           }

           if (clientWebSocket.isConnected()) {
               // Need to get price of all open contracts and send a separate record to update UI pl
               clientWebSocket.sendData(new Record<>("orderSummary", orderSummary));
           }
       } catch (Exception e) {
           logger.error(e.getMessage());
       }
    }

    @Transactional
    public void syncTradeRepository(OrderSummary orderSummary) {
        List<Order> orders = orderSummary.getAllOrders();

        orders.sort(Comparator.comparing(Order::getCreateDate));

        for (Order order : orders) {
            Optional<Trade> tradeEntity = tradeRepository.findById(order.getId());

            if (tradeEntity.isPresent()) {
                Trade trade = tradeEntity.get();

                if (!trade.isFinalized()) {
                    if (trade.getStatus() == null) {
                        convertOrderToTrade(order, trade);
                    }

                    if ("canceled".equals(order.getStatus())) {
                        handleCanceledOrders(trade);
                    }

                    if ("filled".equals(order.getStatus())) {
                        finalizeTrade(order, trade);
                    }

                    tradeRepository.save(trade);
                }
            }
        }
    }

    public void convertOrderToTrade(Order order, Trade trade) {
        Leg triggerLeg = order.getLeg().get(0);
        Leg limitLeg = order.getLeg().get(1);
        Leg stopLeg = order.getLeg().get(2);
        trade.setOrderId(order.getId());
        trade.setOption(triggerLeg.getOptionSymbol());
        trade.setSymbol(triggerLeg.getSymbol());
        trade.setStatus(order.getStatus());
        trade.setLossStreak(0);
        trade.setStopPrice(stopLeg.getStopPrice());
        trade.setLimitPrice(limitLeg.getPrice());
        trade.setFillPrice(triggerLeg.getPrice());
        trade.setOpenDate(Instant.parse(order.getCreateDate()));
        trade.setCloseDate(Instant.parse(order.getTransactionDate()));
        trade.setQuantity(triggerLeg.getQuantity());
    }

    public void finalizeTrade(Order order, Trade trade) {
        List<Trade> trades = tradeRepository.findLastFinalizedTrade();
        Trade lastFinalizedTrade = trades.isEmpty() ? null : trades.get(0);

        Leg limitLeg = order.getLeg().get(1);
        Leg stopLeg = order.getLeg().get(2);

        String tradeResult = "filled".equals(limitLeg.getStatus()) ? "W" : "L";
        boolean isWin = tradeResult.equals("W");
        Leg resultLeg = isWin ? limitLeg : stopLeg;
        trade.setTradeResult(tradeResult);
        trade.setStatus(order.getStatus());

        if (isWin) {
            BigDecimal pl = calculatePl(resultLeg.getPrice(), resultLeg.getQuantity());
            trade.setPl(pl);
            trade.setLossStreak(0);

            if (lastFinalizedTrade != null) {
                trade.setWins(lastFinalizedTrade.getWins() + 1);
                trade.setLosses(lastFinalizedTrade.getLosses());
            } else {
                trade.setWins(1);
            }
        } else {
            BigDecimal pl = calculatePl(resultLeg.getStopPrice(), resultLeg.getQuantity());
            trade.setPl(pl.negate());

            if (lastFinalizedTrade != null) {
                trade.setLosses(lastFinalizedTrade.getLosses() + 1);
                trade.setLossStreak(lastFinalizedTrade.getLossStreak() + 1);
                trade.setWins(lastFinalizedTrade.getWins());
            } else {
                trade.setLosses(1);
                trade.setLossStreak(1);
            }
        }

        trade.setFinalized(true);
    }

    public void handleCanceledOrders(Trade trade) {
        if (trade.getRecoveryId() != null) {
            Trade lossIdTrade = tradeRepository.findTradeByLossId(trade.getRecoveryId());
            lossIdTrade.setLossId(null);
            tradeRepository.save(lossIdTrade);
            trade.setRecoveryId(null);
        }
        trade.setFinalized(true);
    }

    public BigDecimal calculatePl(BigDecimal ask, Integer quantity) {
        BigDecimal contractCost = ask.multiply(BigDecimal.valueOf(100));
        return contractCost.multiply(BigDecimal.valueOf(quantity));
    }

//    @Scheduled(fixedRate = 8000)
//    public void fetchOrdersScheduleActive() {
//        if (clientWebSocket.isConnected() && !ordersAreEmpty) {
//            fetchOrders();
//        }
//    }
//
//    @Scheduled(fixedRate = 10000)
//    public void fetchOrdersSchedule() {
//        if (!clientWebSocket.isConnected() && !ordersAreEmpty) {
//            fetchOrders();
//        }
//    }
}