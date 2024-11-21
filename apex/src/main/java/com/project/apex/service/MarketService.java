package com.project.apex.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.config.EnvConfig;
import com.project.apex.data.market.QuoteData;
import com.project.apex.component.ApiRequest;
import com.project.apex.component.ClientWebSocket;
import com.project.apex.util.Constants;
import com.project.apex.util.Convert;
import com.project.apex.util.Record;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarketService {

    private static final Logger logger = LoggerFactory.getLogger(MarketService.class);
    private LinkedHashMap<String, QuoteData> optionsMap;
    private List<String> symbolList;
    private final EnvConfig envConfig;
    private final ClientWebSocket clientWebSocket;
    ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    public MarketService(EnvConfig envConfig, @Lazy ClientWebSocket clientWebSocket) {
        this.envConfig = envConfig;
        this.clientWebSocket = clientWebSocket;
    }

    private String getBaseApi() {
        return envConfig.getApiEndpoint() + "/v1/markets";
    }

    public List<QuoteData> getOptionsChain(String symbol, String optionType) throws IOException, URISyntaxException {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("symbol", symbol);
        queryParams.put("expiration", getNextExpiration(symbol.equals("SPY") || symbol.equals("QQQ") || symbol.equals("IWM")));
        String response = ApiRequest.get(getBaseApi() + "/options/chains", queryParams);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode optionsNode = objectMapper.readTree(response).path("options");
        JsonNode options = Optional.ofNullable(optionsNode).filter(node -> !node.isNull()).orElseThrow();
        JsonNode quotes = Optional.ofNullable(options.path("option")).filter(node -> !node.isNull()).orElseThrow();
        Double price = getPrice(symbol);

        try {
            List<QuoteData> optionsList = objectMapper.readValue(quotes.toString(), new TypeReference<>() {});
            List<QuoteData> list = optionsList.stream()
                    .filter(option -> optionType.equalsIgnoreCase(option.getOptionType()))
                    .filter(option -> {
                        if ("put".equalsIgnoreCase(option.getOptionType())) {
                            return option.getStrike().compareTo(price) < 0;
                        } else {
                            return option.getStrike().compareTo(price) > 0;
                        }
                    })
                    .sorted("put".equalsIgnoreCase(optionType) ?
                            Comparator.comparing(QuoteData::getStrike).reversed() :
                            Comparator.comparing(QuoteData::getStrike))
                            .limit(20)
                                    .toList();

            setOptionsMap(list.stream()
                    .collect(Collectors.toMap(QuoteData::getSymbol, data -> data)));
            setSymbolList(list.stream().map(QuoteData::getSymbol).collect(Collectors.toList()));
            return list;
        } catch (IOException e) {
            logger.error("getOptionsChain", e);
            return List.of();
        }
    }

    public Double getPrice(String symbol) throws IOException, URISyntaxException {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("symbols", symbol);
        String response = ApiRequest.get(getBaseApi() + "/quotes", queryParams);
        JsonNode jsonNode = new ObjectMapper().readTree(response).path("quotes").path("quote");
        String price = jsonNode.get("last").asText();
        return Double.parseDouble(price);
    }

    public JsonNode getPrices(String symbols) throws IOException, URISyntaxException {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("symbols", symbols);
        String response = ApiRequest.get(getBaseApi() + "/quotes", queryParams);
        return new ObjectMapper().readTree(response).path("quotes").path("quote");
    }

    public String getPriceFull(String symbol) throws IOException, URISyntaxException {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("symbols", symbol);
        String response = ApiRequest.get(getBaseApi() + "/quotes", queryParams);
        JsonNode jsonNode = new ObjectMapper().readTree(response).path("quotes").path("quote");
        return jsonNode.toString();
    }

    private String getNextExpiration(boolean is0Dte) {
        LocalDate currentDate = LocalDate.now();
        LocalDate nextFriday = currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        LocalDate useDate = is0Dte ? currentDate : nextFriday;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return useDate.format(formatter);
    }

    public String buildOptionsStreamCall() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api.tradier.com/v1/markets/events/session");
            post.setHeader("Accept", "application/json");
            post.setHeader("Authorization", "Bearer " + envConfig.getProdClientSecret());
            String jsonInputString = "{\"name\":\"John Doe\", \"age\":30}";
            StringEntity entity = new StringEntity(jsonInputString);
            post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);
            final String jsonString = EntityUtils.toString(response.getEntity());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);
            String sessionId = rootNode.path("stream").path("sessionid").asText();
            String symbolsJsonArray = getSymbolList().stream()
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
    public void sendOptionsQuote(String jsonMessage) throws IOException {
        try {
            QuoteData quoteData = new QuoteData();
            JsonNode quote = objectMapper.readTree(jsonMessage);
            quoteData.setSymbol(quote.get("symbol").asText());
            quoteData.setBid(quote.get("bid").asDouble());
            quoteData.setAsk(quote.get("ask").asDouble());
            optionsMap.put(quoteData.getSymbol(), quoteData);
            Record<QuoteData> quoteRecord = new Record<>("quote", quoteData);
            clientWebSocket.sendMessageToAll(Convert.objectToString(quoteRecord));
        } catch (Exception e) {
            System.err.println("Failed to parse message: " + e.getMessage());
        }
    }

    public void setOptionsMap(Map<String, QuoteData> optionsMap) {
        this.optionsMap = new LinkedHashMap<>(optionsMap);
    }

    public List<String> getSymbolList() {
        return symbolList;
    }

    public void setSymbolList(List<String> symbolList) {
        this.symbolList = symbolList;
    }

    public Map<String, QuoteData> getOptionsMap() {
        return optionsMap;
    }

    public void setDemoOptionsChain() throws IOException {
        String str = objectMapper.writeValueAsString(Files.newInputStream(Paths.get("src/main/resources/json/optionsChainExampleListOnly.json")));

        final Random random = new Random();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (Map.Entry<String, QuoteData> entry : optionsMap.entrySet()) {
                    QuoteData quote = entry.getValue();

                    // Generate random bid and ask prices
                    Double bid = Math.round(random.nextDouble() * 200.0) / 100.0;
                    Double ask = Math.round(random.nextDouble() * 200.0) / 100.0;

                    // Update bid and ask
                    quote.setBid(bid);
                    quote.setAsk(ask);

                    // Print the updated quote
//                        System.out.println("Updated QuoteData: " + quote);

                    try {
                        clientWebSocket.sendMessageToAll(objectMapper.writeValueAsString(quote));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    timer.cancel();
                }
            }
        }, 0, 2000); // Runs every 5 seconds
    }

    public void fetchMarketPrices() {
        String concatStrings = Arrays.stream(Constants.SYMBOLS).reduce("", (a, b) -> a + "," + b);
        String stripped = concatStrings.substring(1, concatStrings.length() - 1);

        try {
            String symbolsData = getPriceFull(stripped);
            clientWebSocket.sendData(new Record<>("symbols", symbolsData));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
