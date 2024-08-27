package com.project.apex.component;

import com.project.apex.service.AccountService;
import com.project.apex.service.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AppStartupListener {

    private static final Logger logger = LoggerFactory.getLogger(AppStartupListener.class);

    private final AccountService accountService;
    private final TradeService tradeService;

    @Autowired
    public AppStartupListener(AccountService accountService, TradeService tradeService) {
        this.accountService = accountService;
        this.tradeService = tradeService;
    }

    @EventListener(ClientWebSocket.class)
    public void onApplicationReady() throws IOException {
        System.out.println("Application started successfully.");

//        tradeService.init();

        // Confirm (1) Account (2) Trade (3) WS are healthy.
    }
}