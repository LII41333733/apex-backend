package com.project.apex.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.apex.data.trades.*;
import com.project.apex.model.*;
import com.project.apex.service.TradeService;
import com.project.apex.util.Constants;
import com.project.apex.util.Convert;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.project.apex.data.trades.RiskType.*;
import static com.project.apex.util.Convert.roundedDouble;

@Component
public class TradeSimulator {

    private final List<Trade> trades = new LinkedList<>();
    private final TradeFactory tradeFactory;
    private TradeService<Trade> tradeService;
    LocalDateTime currentDate = LocalDateTime.now(ZoneId.of("America/New_York"));

    @Autowired
    public TradeSimulator(TradeFactory tradeFactory) {
        this.tradeFactory = tradeFactory;
    }

    @PostConstruct
    public void init() {
        while (trades.isEmpty() || trades.get(trades.size() - 1).getPostTradeBalance() < 600000) {
            try {
                Trade trade = createRandomTrade();
                prepareTrade(trade);
                simulateTrade(trade, false);
                trade = createRandomTrade();
                prepareTrade(trade);
                simulateTrade(trade, false);
                trade = createRandomTrade();
                prepareTrade(trade);
                simulateTrade(trade, true);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            mapper.writeValue(new File("demoTrades.json"), trades);
            System.out.println("LinkedList written to JSON file successfully.");
        } catch (IOException e) {
            System.out.println("Error writing LinkedList to JSON file: " + e.getMessage());
        }
    }

    public Trade createRandomTrade() {
        List<RiskType> choices = List.of(BASE, BASE, BASE, BASE, BASE, VISION, VISION, VISION, LOTTO, LOTTO, LOTTO, HERO, HERO);
        int randomNumber = (int) (Math.random() * choices.size());
        RiskType riskType = choices.get(randomNumber);
        tradeService = tradeFactory.getTradeService(riskType);
        return tradeFactory.getTradeInstance(riskType);
    }

    public void prepareTrade(Trade trade) throws Exception {
        Long id = Convert.getMomentAsCode() + (trades.size() + 1);
        boolean isVisionTrade = trade instanceof VisionTrade;
        double ask = isVisionTrade ? getRandomVisionFillPrice() : getRandomFillPrice();
        int askPrice = (int) (ask * 100);
        double balance = trades.isEmpty() ? 10000 : trades.get(trades.size() - 1).getPostTradeBalance();
        int tradeAllotment = isVisionTrade ? 100 : (int) Math.floor(balance * trade.getTradeAmountPercentage());
        if (askPrice > tradeAllotment) {
            System.out.println("ask: " + askPrice);
            System.out.println("allotment: " + tradeAllotment);
            throw new Exception("Cannot afford");
        }
        int quantity = (int) Math.floor(tradeAllotment / (ask * 100));
        String symbol = Constants.SYMBOLS[(int) getRandomValue(0, Constants.SYMBOLS.length - 1)];
        String strike = strikes[(int) getRandomValue(0, strikes.length - 1)];
        trade.setId(id);
        trade.setPreTradeBalance(balance);
        trade.setInitialAsk(ask);
        trade.setFillPrice(ask);
        trade.setQuantity(quantity);
        trade.setSymbol(symbol);
        trade.setOptionSymbol(symbol + strike);
        trade.setStatus(TradeStatus.FILLED);
        trade.setMaxPrice(getRandomHeroPrice(ask));
        tradeService.calculateStopsAndTrims(trade);
    }

    public int updateTradeValue(int tradeValue, double initialPrice, double currentPrice, int quantity) {
        int initialValue = (int) (quantity * initialPrice * 100);
        int currentValue = (int) (quantity * currentPrice * 100);
        return tradeValue + (currentValue - initialValue);
    }

    public void simulateTrade(Trade trade, boolean changeDay) {
        int[] trimResultPercentages = trade.getDemoOutcomePercentages();
        Integer tradeValue = trade.getTradeAmount();
        int tradeStage = 0;
        int quantity = trade.getQuantity();
        while (tradeStage > -1 && tradeStage < trimResultPercentages.length) {
            if (tradeStage == 0) {
                if (simulateWinResult(trimResultPercentages[0])) {
                    if (trade instanceof Trim1Tradeable t1) {
                        trade.setTrimStatus(1);
                        tradeValue = updateTradeValue(tradeValue, trade.getFillPrice(), t1.getTrim1Price(), t1.getTrim1Quantity());
                        quantity = quantity - t1.getTrim1Quantity();
                        if (quantity < 0) {
                            trade.setLastPrice(t1.getTrim1Price());
                            tradeStage = -1;
                        } else {
                            tradeStage++;
                        }
                    } else {
                        double runnerPrice = getRandomRunnerPrice(trade.getFillPrice());
                        trade.setLastPrice(runnerPrice);
                        trade.setTrimStatus(1);
                        tradeValue = updateTradeValue(tradeValue, trade.getFillPrice(), runnerPrice, trade.getQuantity());
                        tradeStage = -1;
                    }
                } else {
                    trade.setLastPrice(trade.getStopPrice());
                    tradeValue = updateTradeValue(tradeValue, trade.getFillPrice(), trade.getStopPrice(), quantity);
                    tradeStage = -1;
                }
            } else if (tradeStage == 1) {
                if (simulateWinResult(trimResultPercentages[1])) {
                    if (trade instanceof Trim2Tradeable t2) {
                        trade.setTrimStatus(2);
                        tradeValue = updateTradeValue(tradeValue, trade.getFillPrice(), t2.getTrim2Price(), t2.getTrim2Quantity());
                        quantity = quantity - t2.getTrim2Quantity();
                        if (quantity < 0) {
                            trade.setLastPrice(t2.getTrim2Price());
                            tradeStage = -1;
                        } else {
                            tradeStage++;
                        }
                    } else {
                        double runnerPrice = getRandomRunnerPrice(trade.getFillPrice());
                        trade.setLastPrice(runnerPrice);
                        tradeValue = updateTradeValue(tradeValue, trade.getFillPrice(), runnerPrice, quantity);
                        tradeStage = -1;
                    }
                } else {
                    trade.setLastPrice(((Trim1Tradeable) trade).getTrim1Price());
                    tradeStage = -1;
                }
            } else {
                if (simulateWinResult(trimResultPercentages[2])) {
                    double runnerPrice = getRandomRunnerPrice(trade.getFillPrice());
                    trade.setLastPrice(runnerPrice);
                    tradeValue = updateTradeValue(tradeValue, trade.getFillPrice(), runnerPrice, quantity);
                } else {
                    trade.setLastPrice(((Trim2Tradeable) trade).getTrim2Price());
                }
                tradeStage = -1;
            }
        }

        trade.setFinalAmount(tradeValue);
        int pl = tradeValue - trade.getTradeAmount();
        trade.setPl(pl);
        trade.setPostTradeBalance(trade.getPreTradeBalance() + pl);

        if (trade instanceof Trim2Tradeable t2 && trade.getTrimStatus() == 2) {
            t2.setTrim2PriceFinal(t2.getTrim2Price());
        }

        if (trade instanceof Trim1Tradeable t1 && trade.getTrimStatus() >= 1) {
            t1.setTrim1PriceFinal(t1.getTrim1Price());
        }

        trade.setStopPriceFinal(trade.getLastPrice());

        if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            currentDate = currentDate.with(DayOfWeek.FRIDAY);
        }
        trade.setOpenDate(currentDate.withHour(9).withMinute(30).withSecond(0).withNano(0));
        trade.setCloseDate(currentDate.withHour(16).withMinute(0).withSecond(0).withNano(0));

        if (changeDay) {
            currentDate = switch (currentDate.getDayOfWeek()) {
                case MONDAY -> currentDate.minusDays(3);
                case SUNDAY -> currentDate.minusDays(2);
                default -> currentDate.minusDays(1);
            };
        }

        trades.add(trade);
    }

    public boolean simulateWinResult(int chancePercent) {
        Random random = new Random();
        int randomNumber = random.nextInt(100);
        return (randomNumber < chancePercent);
    }

    public double getRandomFillPrice() {
        return roundedDouble(getRandomValue(.20, 2.00));
    }

    public double getRandomVisionFillPrice() {
        return roundedDouble(getRandomValue(.05, 1.00));
    }

    public double getRandomRunnerPrice(double price) {
        return roundedDouble(price * getRandomValue(2.00, 2.50));
    }

    public double getRandomHeroPrice(double price) {
        return roundedDouble(price * getRandomValue(2.50, 3.00));
    }

    public double getRandomValue(double min, double max) {
        return roundedDouble(min + (max - min) * Math.random());
    }

    public double getRandomValue(int min, int max) {
        return roundedDouble(min + (max - min) * Math.random());
    }

    private static final String[] strikes = {"241001C00571000", "241001P00567000", "241018C00232500", "240919C00572000", "240919P00564000", "241018C00051000", "240919C00574000", "241018C00051500", "241018C00139000", "241018P00182500", "241018P00028000", "241018P00222500", "241018C00192500", "241018C00082500", "241018C00232500", "240920P00565000", "240920C00232500", "240927C00165000", "241025C00230000", "241025C00048000", "241025C00160000", "241025C00148000", "241025C00083000", "240927C00260000", "240927C00255000", "240927C00405000", "241004C00041000", "241004C00270000", "241004C00122000", "241004C00580000", "241004C00039500", "241004C00262500", "241004C00043250", "241004C00165000", "241011C00043000", "241011C00195000", "241011C00252500", "241011C00047000", "241011C00257500", "241011C00595000"};
    private static final RiskType[] riskTypes = {BASE, LOTTO, VISION, HERO};

}
