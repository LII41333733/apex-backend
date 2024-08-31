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

import java.net.URISyntaxException;
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
    public ResponseEntity<?> getOptionsChain(@RequestParam String symbol, @RequestParam String optionType) {
        try {
            // Retrieve the options chain data
            List<QuoteData> list = marketService.getOptionsChain(symbol, optionType);

            // If data is available, handle the WebSocket communication
            if (!list.isEmpty()) {
                if (!marketStream.isConnected()) {
                    // Reconnect if the connection is not active
                    marketStream.reconnect();
                } else {
                    marketStream.stopAllStreams();
                    marketStream.reconnect();
                    String message = marketService.buildOptionsStreamCall();
                    marketStream.sendMessage(message);

                }

                         }

            return new ResponseEntity<>(list, HttpStatus.OK);

        } catch (NoSuchElementException e) {
            String err = "Tradier Options Chain is down. (Market Closed - Data Unavailable)";
            return new ResponseEntity<>(err, HttpStatus.SERVICE_UNAVAILABLE);

        } catch (URISyntaxException e) {
            String err = "WebSocket URI Error";
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            String err = "Error retrieving options template";
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}