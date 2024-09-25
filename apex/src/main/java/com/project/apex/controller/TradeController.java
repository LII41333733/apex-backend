package com.project.apex.controller;

import com.project.apex.component.MarketStream;
import com.project.apex.data.trades.*;
import com.project.apex.model.Trade;
import com.project.apex.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

import static com.project.apex.data.trades.TradeOperation.PLACE;

@RestController
@RequestMapping("/api/trade")
public class TradeController {

    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);
    private final TradeFactory tradeFactory;
    private final MarketStream marketStream;
    private final AccountService accountService;

    @Autowired
    public TradeController(TradeFactory tradeFactory, MarketStream marketStream, AccountService accountService) {
        this.tradeFactory = tradeFactory;
        this.marketStream = marketStream;
        this.accountService = accountService;
    }

    @PostMapping("/placeTrade")
    public ResponseEntity<?> placeTrade(@RequestBody BuyDataRecord buyDataRecord) {
        logger.info("TradeController.placeTrade: Start");
        logger.info("TradeController.placeTrade: BuyData: {}", buyDataRecord);

        try {
            tradeFactory.placeFill(buyDataRecord);
            marketStream.stopAllStreams();

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("placeTrade", e);
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cancelTrade")
    public ResponseEntity<?> cancelTrade(@RequestBody CancelTradeRecord request) {
        try {
            accountService.delete("/orders/" + request.id());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Cancel Trade Exception", e);
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/modifyTrade")
    public ResponseEntity<?> modifyTrade(@RequestBody ModifyTradeRecord sellTradeRecord) {
        try {
            tradeFactory.modifyTrade(sellTradeRecord);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Cancel Trade Exception", e);
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/sellTrade")
    public ResponseEntity<?> sellTrade(@RequestBody SellTradeRecord sellTradeRecord) {
        try {
            tradeFactory.sellTrade(sellTradeRecord);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Cancel Trade Exception", e);
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}