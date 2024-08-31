package com.project.apex.controller;

import com.project.apex.component.MarketStream;
import com.project.apex.data.BuyData;
import com.project.apex.data.SandboxTradeRequest;
import com.project.apex.service.AccountService;
import com.project.apex.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/api/trade")
public class TradeController {

    private static final Logger logger = LogManager.getLogger(TradeController.class);
    private final TradeService tradeService;
    private final AccountService accountService;
    private final MarketStream marketStream;

    @Autowired
    public TradeController(TradeService tradeService, AccountService accountService, MarketStream marketStream) {
        this.tradeService = tradeService;
        this.accountService = accountService;
        this.marketStream = marketStream;
    }

//    @PostMapping("/placeSandboxTrade")
//    public ResponseEntity<?> placeSandboxTrade(@RequestBody SandboxTradeRequest sandboxTradeRequest) throws IOException {
//        try {
//            String response = tradeService.placeSandboxTrade(sandboxTradeRequest);
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        } catch (NoSuchElementException e) {
//            var err = "Tradier Options Chain is down. (Market Closed - Data Unavailable)";
//            logger.warn(err);
//            return new ResponseEntity<>(err, HttpStatus.SERVICE_UNAVAILABLE);
//        } catch (Exception e) {
//            logger.error(e);
//            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PostMapping("/placeTrade")
    public ResponseEntity<?> placeTrade(@RequestBody BuyData buyData) throws IOException {
        System.out.println(buyData.toString());

        try {
            String response = tradeService.placeTrade(buyData);
            marketStream.stopAllStreams();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e);
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // onclick od Bid/Ask update price
        // Send balance data every (s) seconds
        // get market value based on the last sent balance data in state
        // + Check price against the amount of cons to be bought based on 1% market value (try/catch)
        // + (feature) Add losses from last trade

        // Place trade OTOCO
        // save trade to trades table

        /*
        * Trade
        *
        * id
        * status
        * type
        * timePlaced
        * timeFille
        * kd
        * # cons
        * symbol
        * cost
        * optionType
        * Strike
        * pl
        * stop_loss
        * target_sell
        * target_hit
        * loss_id
        * */

        /*
        * SQL
        * - find last loss, see if any current order has a loss id associated. if not, use losses on current option.
        * - if so, then losses are accounted for and 1% should be used
        * */

        // Attempt to create account session onload (in init)
        // Create a state for orders that gets updated when each piece is found. When closed,
        // Track the status of the order by creating a log on BE and send thru stream to be saved on FE
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