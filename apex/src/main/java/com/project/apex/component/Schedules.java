package com.project.apex.component;

import com.project.apex.service.AccountService;
import com.project.apex.service.MarketService;
import com.project.apex.service.OrdersService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Schedules {

    private final ClientWebSocket clientWebSocket;
    private final OrdersService ordersService;

    public Schedules(ClientWebSocket clientWebSocket, OrdersService ordersService, MarketService marketService, AccountService accountService) {
        this.clientWebSocket = clientWebSocket;
        this.ordersService = ordersService;
    }

    @Scheduled(fixedRate = 20000)
    public void fetchOrdersActiveClient() throws Exception {
        if (clientWebSocket.isConnected()) {
            clientWebSocket.handleActiveClientWebSocketData();
        }
    }

    @Scheduled(fixedRate = 20000)
    public void fetchOrdersInActiveClient() {
        if (!clientWebSocket.isConnected()) {
            ordersService.fetchOrders();
        }
    }

}
