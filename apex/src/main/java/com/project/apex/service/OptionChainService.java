//package com.project.apex.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.project.apex.config.EnvConfig;
//import com.project.apex.data.market.OptionChainBean;
//import com.project.apex.component.MarketStream;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.HttpUriRequest;
//import org.apache.http.client.methods.RequestBuilder;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.time.temporal.TemporalAdjusters;
//import java.util.List;
//import java.net.URI;
//
//@Service
//public class OptionChainService {
//
//    private static final Logger logger = LoggerFactory.getLogger(OptionChainService.class);
//
//    private final EnvConfig initConfig;
//
//    private final MarketService marketService;
//
//    @Autowired
//    public OptionChainService(EnvConfig initConfig, MarketService marketService) {
//        this.initConfig = initConfig;
//        this.marketService = marketService;
//    }
//
//    public void startOptionChainStream(String symbol, String optionType) throws IOException, URISyntaxException {
//        System.out.println(symbol);
//        System.out.println(optionType);
//
//        boolean is0Dte = symbol.equals("SPY") || symbol.equals("QQQ");
//
//        LocalDate currentDate = LocalDate.now();
//        LocalDate nextFriday = currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
//        LocalDate useDate = is0Dte ? currentDate : nextFriday;
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        String formattedDate = useDate.format(formatter);
//
//        marketService.getPrice(symbol);
//
//        final MarketStream client = new MarketStream(new URI("wss://ws.tradier.com/v1/markets/events"), initConfig);
//        client.connect();
//
//
//        final HttpUriRequest request = RequestBuilder
//                .get("https://api.tradier.com/v1/markets/op")
//                .addHeader("Authorization", "Bearer " + initConfig.getClientSecret())
//                .addHeader("Accept", "application/json")
//                .addParameter("symbol", symbol)
//                .addParameter("expiration", formattedDate)
//                .build();
//
//        final HttpResponse response = HttpClientBuilder.create().build().execute(request);
//        logger.info(response.getEntity());
//        final String jsonString = EntityUtils.toString(response.getEntity());
//        ObjectMapper objectMapper = new ObjectMapper();
//        final JsonNode optionsNode = new ObjectMapper().readTree(jsonString).path("options").path("option");
//        System.out.println(optionsNode);
//        System.out.println(optionsNode.asText());
//        return objectMapper.convertValue(optionsNode, objectMapper.getTypeFactory().constructCollectionType(List.class, OptionChainBean.class));
//    }
//
////    private int price
//
//    public List<OptionChainBean> getOptionsChain(String symbol, String optionType) throws IOException {
//        System.out.println(symbol);
//        System.out.println(optionType);
//
//        boolean is0Dte = symbol.equals("SPY") || symbol.equals("QQQ");
//
//        LocalDate currentDate = LocalDate.now();
//        LocalDate nextFriday = currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
//        LocalDate useDate = is0Dte ? currentDate : nextFriday;
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        String formattedDate = useDate.format(formatter);
//
//        final HttpUriRequest request = RequestBuilder
//                .get("https://api.tradier.com/v1/markets/options/chains")
//                .addHeader("Authorization", "Bearer " + initConfig.getClientSecret())
//                .addHeader("Accept", "application/json")
//                .addParameter("symbol", symbol)
//                .addParameter("expiration", formattedDate)
//                .build();
//
//        final HttpResponse response = HttpClientBuilder.create().build().execute(request);
//        logger.info(response.getEntity());
//        final String jsonString = EntityUtils.toString(response.getEntity());
//        ObjectMapper objectMapper = new ObjectMapper();
//        final JsonNode optionsNode = new ObjectMapper().readTree(jsonString).path("options").path("option");
//        System.out.println(optionsNode);
//        System.out.println(optionsNode.asText());
//        return objectMapper.convertValue(optionsNode, objectMapper.getTypeFactory().constructCollectionType(List.class, OptionChainBean.class));
//    }
//}
