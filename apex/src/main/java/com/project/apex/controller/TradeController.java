package com.project.apex.controller;

import com.project.apex.model.BuyData;
import com.project.apex.model.OptionChainBean;
import com.project.apex.service.AccountService;
import com.project.apex.service.TradeService;
import com.project.apex.utils.ApiResponse;
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
@RequestMapping("/api/trade")
public class TradeController {

    private static final Logger logger = LogManager.getLogger(TradeController.class);

    private final TradeService tradeService;
    private final AccountService accountService;

    @Autowired
    public TradeController(TradeService tradeService, AccountService accountService) {
        Assert.notNull(tradeService, "tradeService must not be null");
        Assert.notNull(accountService, "accountService must not be null");
        this.tradeService = tradeService;
        this.accountService = accountService;
    }

    @PostMapping("/placeTrade")
    public ResponseEntity<ApiResponse<List<OptionChainBean>>> placeTrade(@RequestBody BuyData buyData) throws IOException {
        System.out.println(buyData.toString());

        try {
            tradeService.placeTrade(buyData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }





        ApiResponse<List<OptionChainBean>> response = new ApiResponse<>("Started Trade Placeing...", HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


//    @GetMapping("/getOptionsChain")
//    public JsonNode getOptionsChain(@RequestParam String symbol, @RequestParam String optionType) throws IOException, URISyntaxException {
//        final ClientExample client = new ClientExample(new URI("wss://ws.tradier.com/v1/markets/events"));
//        client.connect();
//    }
}

//package com.example.demo.controller;
//
//import com.example.demo.model.Item;
//import com.example.demo.response.ApiResponse;
//import com.example.demo.service.ItemService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//        import java.util.List;
//
//@RestController
//@RequestMapping("/items")
//public class ItemController {
//
//    @Autowired
//    private ItemService itemService;
//
//    // Create
//    @PostMapping
//    public ResponseEntity<ApiResponse<Item>> createItem(@RequestBody Item item) {
//        Item createdItem = itemService.createItem(item);
//        ApiResponse<Item> response = new ApiResponse<>("Item created successfully", HttpStatus.CREATED.value(), createdItem);
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }
//
//    // Read All
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<Item>>> getAllItems() {
//        List<Item> items = itemService.getAllItems();
//        ApiResponse<List<Item>> response = new ApiResponse<>("Items retrieved successfully", HttpStatus.OK.value(), items);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//    // Read One
//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<Item>> getItemById(@PathVariable Long id) {
//        Item item = itemService.getItemById(id);
//        ApiResponse<Item> response = new ApiResponse<>("Item retrieved successfully", HttpStatus.OK.value(), item);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//    // Update
//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse<Item>> updateItem(@PathVariable Long id, @RequestBody Item itemDetails) {
//        Item updatedItem = itemService.updateItem(id, itemDetails);
//        ApiResponse<Item> response = new ApiResponse<>("Item updated successfully", HttpStatus.OK.value(), updatedItem);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//    // Delete
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
//        itemService.deleteItem(id);
//        ApiResponse<Void> response = new ApiResponse<>("Item deleted successfully", HttpStatus.NO_CONTENT.value(), null);
//        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
//    }