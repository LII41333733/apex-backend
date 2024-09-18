package com.project.apex.controller;

import com.project.apex.component.MarketStream;
import com.project.apex.data.trades.BuyData;
import com.project.apex.data.trades.RiskType;
import com.project.apex.service.BaseTradeService;
import com.project.apex.service.OrdersService;
import com.project.apex.service.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/api/trade")
public class TradeController {

    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);
    private final TradeService tradeService;
    private final MarketStream marketStream;
    private final OrdersService ordersService;
    private final BaseTradeService baseTradeService;

    @Autowired
    public TradeController(TradeService tradeService,
                           MarketStream marketStream,
                           OrdersService ordersService,
                           BaseTradeService baseTradeService) {
        this.tradeService = tradeService;
        this.marketStream = marketStream;
        this.ordersService = ordersService;
        this.baseTradeService = baseTradeService;
    }

    public record CancelTradeRequest(String orderId) {}

    @PostMapping("/placeTrade")
    public ResponseEntity<?> placeTrade(@RequestBody BuyData buyData) {
        logger.info("TradeController.placeTrade: Start");
        logger.info("TradeController.placeTrade: BuyData: {}", buyData);
        RiskType riskType = RiskType.valueOf(buyData.getRiskType().toUpperCase());

        try {
            switch (riskType) {
                case BASE -> baseTradeService.placeFill(buyData);
            }

            marketStream.stopAllStreams();

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("placeTrade", e);
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cancelTrade")
    public ResponseEntity<?> cancelTrade(@RequestBody CancelTradeRequest request) throws IOException {
        try {
            tradeService.cancelTrade(request.orderId());
            ordersService.fetchOrders();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Cancel Trade Exception", e);
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}