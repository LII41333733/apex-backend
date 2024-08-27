package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.component.ClientWebSocket;
import com.project.apex.data.Order;
import com.project.apex.data.orders.OrderSummary;
import com.project.apex.util.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrdersService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersService.class);
    private boolean isFetchingOrders = false;
    private final AccountService accountService;
    private final ClientWebSocket clientWebSocket;
    private final List<Order> pendingOrders = new ArrayList<>();
    private final List<Order> openOrders = new ArrayList<>();

    @Autowired
    public OrdersService(AccountService accountService, @Lazy ClientWebSocket clientWebSocket) {
        this.accountService = accountService;
        this.clientWebSocket = clientWebSocket;
    }

    // create 3 maps - 1) pending 2) open 3) closed
    // if 1 or 2 is not empty
    // Timer shouldnt have started but too tired... only start timer if open or pending exists. Only needs 1 fetch on load and like 1 other condition. Check notes.
    // 1, on ui load
    // 2. refresh btn
    // 3. after a trade
    public void fetchOrders() throws IOException {
        processOrders(new ObjectMapper().readTree(accountService.get("/orders")).get("orders").get("order"));
    }

    @Scheduled(fixedRate = 10000)
    public void fetchOrdersSchedule() {
        if (isFetchingOrders) {
            try {
                fetchOrders();
            } catch (IOException e) {
                stopOrderFetching();
                logger.error(e.getMessage());
            }
        }
    }

    public void processOrders(JsonNode ordersNode) throws IOException {
        OrderSummary orderSummary = new OrderSummary();

        if (ordersNode != null) {
            orderSummary.update(ordersNode);
        }

        clientWebSocket.sendData(new Record<>("orderSummary", orderSummary));
//        if (!isFetchingOrders) isFetchingOrders = true;
    }

    public void startOrderFetching() {
        isFetchingOrders = true;
    }

    public void stopOrderFetching() {
        isFetchingOrders = false;
    }
}