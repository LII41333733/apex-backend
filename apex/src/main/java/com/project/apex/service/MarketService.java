package com.project.apex.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.config.InitConfig;
import com.project.apex.model.LiveOption;
import com.project.apex.model.OptionChainBean;
import com.project.apex.utils.ApiRequest;
import com.project.apex.websocket.ClientWebSocket;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarketService {

    private static final Logger logger = LogManager.getLogger(MarketService.class);

    private final String BASE_API = "https://api.tradier.com/v1/markets/";
    private final String GET_PRICE = BASE_API + "/quotes";
    private final String GET_OPTIONS_CHAIN = BASE_API + "/options/chains";
    private final LinkedHashMap<String, LiveOption> symbolData;
    private final InitConfig initConfig;
    private List<String> symbols;
    private final ClientWebSocket clientWebSocket;

    @Autowired
    public MarketService(InitConfig initConfig, ClientWebSocket clientWebSocket) {
        this.symbolData = new LinkedHashMap<>();
        this.initConfig = initConfig;
        this.symbols = new ArrayList<>();
        this.clientWebSocket = clientWebSocket;
    }

    public List<String> setOptionsChainSymbols(String symbol, String optionType) throws IOException, URISyntaxException {
        BigDecimal price = getPrice(symbol);
        boolean is0Dte = symbol.equals("SPY") || symbol.equals("QQQ");
        String nextExpiration = getNextExpiration(is0Dte);
        symbols = getOptionsChain(symbol, optionType, price, nextExpiration);
        initializeSymbols(symbols);
        return symbols;
    }

    public BigDecimal getPrice(String symbol) throws IOException, URISyntaxException {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("symbols", symbol);
        String response = ApiRequest.get(GET_PRICE, queryParams);
        JsonNode jsonNode = new ObjectMapper().readTree(response).path("quotes").path("quote");
        String price = jsonNode.get("last").asText();
        return BigDecimal.valueOf(Double.parseDouble(price));
    }

    private String getNextExpiration(boolean is0Dte) {
        LocalDate currentDate = LocalDate.now();
        LocalDate nextFriday = currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        LocalDate useDate = is0Dte ? currentDate : nextFriday;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return useDate.format(formatter);
    }

    public List<String> getOptionsChain(String symbol, String optionType, BigDecimal price, String nextExpiration) throws IOException, URISyntaxException {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("symbol", symbol);
        queryParams.put("expiration", nextExpiration);
        String response = ApiRequest.get(GET_OPTIONS_CHAIN, queryParams);
        JsonNode optionsNode = new ObjectMapper().readTree(response).path("options");
        JsonNode options = Optional.ofNullable(optionsNode).filter(node -> !node.isNull()).orElseThrow();
        JsonNode jsonNode = Optional.ofNullable(options.path("option")).filter(node -> !node.isNull()).orElseThrow();
        return getOptionsSymbolList(jsonNode, optionType, price);
    }

    public void initializeSymbols(List<String> symbols) {
        for (String symbol : symbols) {
            symbolData.put(symbol, null);
        }
    }

    public List<String> getOptionsSymbolList(JsonNode jsonNode, String optionType, BigDecimal price) throws URISyntaxException, IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<OptionChainBean> options = objectMapper.readValue(jsonNode.toString(), new TypeReference<>() {
            });

            // Filter the options based on criteria and map to symbol
            List<String> filteredSymbols = options.stream()
                    .filter(option -> optionType.equalsIgnoreCase(option.getOptionType()))
                    .filter(option -> {
                        if ("put".equalsIgnoreCase(option.getOptionType())) {
                            return option.getStrike().compareTo(price) < 0;
                        } else {
                            return option.getStrike().compareTo(price) > 0;
                        }
                    })
                    .sorted(Comparator.comparing(OptionChainBean::getStrike))
                    .map(OptionChainBean::getSymbol)
                    .toList();

            // Reverse order if the option type is "put"
            if ("put".equalsIgnoreCase(optionType)) {
                // Reverse the list
                filteredSymbols = filteredSymbols.stream()
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());
            }

            // Print the filtered symbols
            System.out.println("Filtered Symbols: " + filteredSymbols);
            return filteredSymbols;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR");
        }
        return List.of();
    }

    public String buildOptionsStreamCall() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api.tradier.com/v1/markets/events/session");
            post.setHeader("Accept", "application/json");
            post.setHeader("Authorization", "Bearer " + initConfig.getClientSecret());

            String jsonInputString = "{\"name\":\"John Doe\", \"age\":30}";
            StringEntity entity = new StringEntity(jsonInputString);
            post.setEntity(entity);

            HttpResponse response = httpClient.execute(post);
            final String jsonString = EntityUtils.toString(response.getEntity());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);

            String sessionId = rootNode.path("stream").path("sessionid").asText();
            String symbolsJsonArray = symbols.stream()
                    .map(symbol -> "\"" + symbol + "\"")
                    .collect(Collectors.joining(", ", "[", "]"));
            return "{" +
                    "\"symbols\": " + symbolsJsonArray + ", " +
                    "\"sessionid\": \"" + sessionId + "\", " +
                    "\"linebreak\": false, " +
                    "\"filter\": [\"quote\"]" +
                    "}";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Update symbol data with incoming JSON message
    public void updateSymbolData(String jsonMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonMessage);

            String type = jsonNode.path("type").asText();
            String symbol = jsonNode.path("symbol").asText();
            double bid = jsonNode.path("bid").asDouble();
            double ask = jsonNode.path("ask").asDouble();

            LiveOption liveOption = new LiveOption(type, symbol, bid, ask);

            // Update the dataset only if the symbol exists in the map
            if (symbolData.containsKey(symbol)) {
                symbolData.put(symbol, liveOption);
                System.out.println("Updated " + symbol + " with data: " + liveOption);
            } else {
                System.out.println("Symbol " + symbol + " not found in dataset.");
            }

            clientWebSocket.sendMessageToAll(objectMapper.writeValueAsString(liveOption));

//            final Random random = new Random();
//            Timer timer = new Timer();
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    for (Map.Entry<String, LiveOption> entry : symbolData.entrySet()) {
//                        LiveOption quote = entry.getValue();
//
//                        // Generate random bid and ask prices
//                        double bid = Math.round(random.nextDouble() * 200.0) / 100.0;
//                        double ask = Math.round(random.nextDouble() * 200.0) / 100.0;
//
//                        // Update bid and ask
//                        quote.setBid(bid);
//                        quote.setAsk(ask);
//
//                        // Print the updated quote
//                        System.out.println("Updated Quote: " + quote);
//
//                        try {
//                            clientWebSocket.sendMessageToAll(objectMapper.writeValueAsString(quote));
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//            }, 0, 2000); // Runs every 5 seconds
        } catch (Exception e) {
            System.err.println("Failed to parse message: " + e.getMessage());
        }
    }
}