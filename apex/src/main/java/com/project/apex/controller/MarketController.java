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
    public ResponseEntity<ApiResponse<Object>> startOptionChainStream() throws IOException {
        ApiResponse<Object> response;

        try {
            marketStream.connectAndStartStream();
            response = new ApiResponse<>("Stream started successfully", HttpStatus.OK.value());

        } catch (Exception e) {
            throw new RuntimeException("Error starting options chain stream");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//    @GetMapping("/startOptionChainStream")
//    public ResponseEntity<ApiResponse<Object>> startOptionChainStream() throws IOException {
//        marketStream.startOptionChainStream();
//
//        ApiResponse<Object> response;
//
//        if (marketStream.isOpen()) {
//            response = new ApiResponse<>("Stream already running", HttpStatus.OK.value());
//        } else {
//            if (marketStream.getInitConfig().isMock()) {
//                marketService.updateSymbolData("");
//            } else {
//                try {
//                    marketStream.connectAndStartStream();
//                } catch (Exception e) {
//                    throw new RuntimeException("Error starting options chain stream");
//                }
//            }
//            response = new ApiResponse<>("Stream started successfully", HttpStatus.OK.value());
//        }
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

    @GetMapping("/getOptionChainTemplate")
    public ResponseEntity<ApiResponse<Object>> getOptionChainTemplate(String symbol, String optionType) {
        ApiResponse<Object> response;

        try {
            List<String> list = marketService.setOptionsChainSymbols(symbol, optionType);
            response = new ApiResponse<>("Options template received successfully", HttpStatus.OK.value(), list);
        } catch (Exception e) {
//            response = new ApiResponse<>("Error retrieving options template", HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            throw new RuntimeException("Error starting options chain stream");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}