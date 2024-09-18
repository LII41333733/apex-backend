//package com.project.apex.controller;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.project.apex.config.EnvConfig;
//import com.project.apex.data.account.Balance;
//import com.project.apex.data.market.OptionChainBean;
//import com.project.apex.service.OptionChainService;
////import com.project.apex.service.TradierService;
//import com.project.apex.utils.ApiResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.util.Assert;
//import org.springframework.web.bind.annotation.*;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.HttpUriRequest;
//import org.apache.http.client.methods.RequestBuilder;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.util.EntityUtils;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.time.temporal.TemporalAdjusters;
//import java.util.Arrays;
//import java.util.List;
//
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//
//@RestController
//@RequestMapping("/api/data")
//public class DataController {
//
//    private static final Logger logger = LoggerFactory.getLogger(DataController.class);
//
//    private final OptionChainService optionChainService;
//
//    @Autowired
//    public DataController( OptionChainService optionChainService) {
//        Assert.notNull(optionChainService, "optionChainService must not be null");
//        this.optionChainService = optionChainService;
//    }
//
//    @GetMapping("/getOptionsChain")
//    public ResponseEntity<ApiResponse<List<OptionChainBean>>> getFullOptionsChain(@RequestParam String symbol, @RequestParam String optionType) throws IOException, URISyntaxException {
//        List<OptionChainBean> optionChainBeanList = optionChainService.getFullOptionsChain(symbol, optionType);
//        ApiResponse<List<OptionChainBean>> response = new ApiResponse<>("Items retrieved successfully", HttpStatus.OK.value(), optionChainBeanList);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//
//
////    @GetMapping("/getOptionsChain")
////    public JsonNode getOptionsChain(@RequestParam String symbol, @RequestParam String optionType) throws IOException, URISyntaxException {
////        final ClientExample client = new ClientExample(new URI("wss://ws.tradier.com/v1/markets/events"));
////        client.connect();
////    }
//}
//
////package com.example.demo.controller;
////
////import com.example.demo.model.Item;
////import com.example.demo.response.ApiResponse;
////import com.example.demo.service.ItemService;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.http.HttpStatus;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////        import java.util.List;
////
////@RestController
////@RequestMapping("/items")
////public class ItemController {
////
////    @Autowired
////    private ItemService itemService;
////
////    // Create
////    @PostMapping
////    public ResponseEntity<ApiResponse<Item>> createItem(@RequestBody Item item) {
////        Item createdItem = itemService.createItem(item);
////        ApiResponse<Item> response = new ApiResponse<>("Item created successfully", HttpStatus.CREATED.value(), createdItem);
////        return new ResponseEntity<>(response, HttpStatus.CREATED);
////    }
////
////    // Read All
////    @GetMapping
////    public ResponseEntity<ApiResponse<List<Item>>> getAllItems() {
////        List<Item> items = itemService.getAllItems();
////        ApiResponse<List<Item>> response = new ApiResponse<>("Items retrieved successfully", HttpStatus.OK.value(), items);
////        return new ResponseEntity<>(response, HttpStatus.OK);
////    }
////
////    // Read One
////    @GetMapping("/{id}")
////    public ResponseEntity<ApiResponse<Item>> getItemById(@PathVariable Long id) {
////        Item item = itemService.getItemById(id);
////        ApiResponse<Item> response = new ApiResponse<>("Item retrieved successfully", HttpStatus.OK.value(), item);
////        return new ResponseEntity<>(response, HttpStatus.OK);
////    }
////
////    // Update
////    @PutMapping("/{id}")
////    public ResponseEntity<ApiResponse<Item>> updateItem(@PathVariable Long id, @RequestBody Item itemDetails) {
////        Item updatedItem = itemService.updateItem(id, itemDetails);
////        ApiResponse<Item> response = new ApiResponse<>("Item updated successfully", HttpStatus.OK.value(), updatedItem);
////        return new ResponseEntity<>(response, HttpStatus.OK);
////    }
////
////    // Delete
////    @DeleteMapping("/{id}")
////    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
////        itemService.deleteItem(id);
////        ApiResponse<Void> response = new ApiResponse<>("Item deleted successfully", HttpStatus.NO_CONTENT.value(), null);
////        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
////    }