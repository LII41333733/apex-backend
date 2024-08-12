package com.project.apex.controller;

import com.project.apex.model.Balance;
//import com.project.apex.service.TradierService;
import com.project.apex.model.LiveOption;
import com.project.apex.service.MarketService;
import com.project.apex.utils.ApiResponse;
import com.project.apex.websocket.MarketStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
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
        Assert.notNull(marketStream, "marketStream must not be null");
        this.marketStream = marketStream;
        this.marketService = marketService;
    }

    @GetMapping("/startOptionChainStream")
    public ResponseEntity<ApiResponse<Object>> startOptionChainStream() {
        ApiResponse<Object> response;

        if (marketStream.isOpen()) {
            response = new ApiResponse<>("Stream already running", HttpStatus.OK.value());
            marketStream.startOptionChainStream();
        } else {
            try {
                marketStream.connectAndStartStream();
            } catch (Exception e) {
                throw new RuntimeException("Error starting options chain stream");
            }
            response = new ApiResponse<>("Stream started successfully", HttpStatus.OK.value());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getOptionChainTemplate")
    public ResponseEntity<ApiResponse<Object>> getOptionChainTemplate(String symbol, String optionType) {
        ApiResponse<Object> response;

        try {
            List<String> list = marketService.setOptionsChainSymbols(symbol, optionType);
            response = new ApiResponse<>("Options template received successfully", HttpStatus.OK.value(), list);
        } catch (NoSuchElementException e) {
            var err = "Tradier Options Chain is down. (Market Closed - Data Unavailable)";
            logger.warn(err);
            response = new ApiResponse<>(err, HttpStatus.SERVICE_UNAVAILABLE.value(), e.getMessage());
        } catch (Exception e) {
            var err = "Error retrieving options template";
            logger.error(err);
            response = new ApiResponse<>(err, HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}