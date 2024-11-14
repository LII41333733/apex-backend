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
    private final AccountService accountService;
    private final MarketService marketService;

    public Schedules(ClientWebSocket clientWebSocket, OrdersService ordersService, MarketService marketService, AccountService accountService) {
        this.clientWebSocket = clientWebSocket;
        this.ordersService = ordersService;
        this.marketService = marketService;
        this.accountService = accountService;
    }

    @Scheduled(fixedRate = 10000)
    public void fetchOrdersActiveClient() {
        clientWebSocket.fetchOrdersActiveClient();
    }

    @Scheduled(fixedRate = 5000)
    public void fetchOrdersInActiveClient() {
        clientWebSocket.fetchOrdersInActiveClient();
    }

}
