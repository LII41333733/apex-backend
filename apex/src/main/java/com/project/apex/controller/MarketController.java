package com.project.apex.controller;

//import com.project.apex.service.TradierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.data.QuoteData;
import com.project.apex.service.MarketService;
import com.project.apex.component.MarketStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private static final Logger logger = LogManager.getLogger(AccountController.class);

    private final MarketStream marketStream;
    private final MarketService marketService;

    @Autowired
    public MarketController(MarketStream marketStream, MarketService marketService) {
        this.marketStream = marketStream;
        this.marketService = marketService;
    }

    @GetMapping("/getOptionsChain")
    public ResponseEntity<?> getOptionsChain(String symbol, String optionType) {
        try {
            List<QuoteData> list = marketService.getOptionsChain(symbol, optionType);

            if (!list.isEmpty()) {
                MarketStream response = marketStream.createAndConnectNewWebSocketClient();

                if (response == null) {
                    return new ResponseEntity<>("Error connecting to the Market Stream", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            var err = "Tradier Options Chain is down. (Market Closed - Data Unavailable)";
            logger.warn(err);
           return new ResponseEntity<>(err, HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            var err = "Error retrieving options template";
            logger.error(err);
           return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}