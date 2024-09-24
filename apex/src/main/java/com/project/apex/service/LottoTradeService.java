package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.component.LottoTradeManager;
import com.project.apex.component.TradeManagerInterface;
import com.project.apex.config.EnvConfig;
import com.project.apex.data.account.Balance;
import com.project.apex.data.orders.OrderFillRecord;
import com.project.apex.data.trades.*;
import com.project.apex.model.LottoTrade;
import com.project.apex.model.Trade;
import com.project.apex.repository.LottoTradeRepository;
import com.project.apex.util.Convert;
import com.project.apex.util.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static com.project.apex.data.trades.TradeLeg.STOP;
import static com.project.apex.data.trades.TradeLeg.TRIM1;
import static com.project.apex.data.trades.TradeStatus.RUNNERS;
import static com.project.apex.util.Convert.roundedDouble;
import static com.project.apex.util.TradeOrder.*;

@Service
public class LottoTradeService implements TradeManagerInterface<LottoTrade> {

    /**
     * For base trades, on the UI, hide all options with asks less than .11
     * They should be saved for lottos
     */

    private static final Logger logger = LoggerFactory.getLogger(LottoTradeService.class);
    protected final EnvConfig envConfig;
    private final MarketService marketService;
    private final AccountService accountService;
    private final LottoTradeRepository lottoTradeRepository;

    @Autowired
    public LottoTradeService(
            EnvConfig envConfig,
            MarketService marketService,
            AccountService accountService,
            LottoTradeRepository lottoTradeRepository
    ) {
        this.envConfig = envConfig;
        this.marketService = marketService;
        this.accountService = accountService;
        this.lottoTradeRepository = lottoTradeRepository;
    }

    public void placeFill(BuyData buyData) {
        try {
            Long id = Convert.getMomentAsCode();
            logger.info("LottoTradeService.placeFill: Start: {}", id);
            Balance balance = accountService.getBalanceData();
            double totalEquity = balance.getTotalEquity();
            logger.info("Total Equity: {}", totalEquity);
            double totalCash = balance.getTotalCash();
            logger.info("Total Cash: {}", totalCash);
            int tradeAllotment = (int) Math.floor(totalEquity * LottoTrade.tradePercentModifier);
            logger.info("Trade Allotment: {}", tradeAllotment);
            logger.info("Trade Allotment < Total Cash: {}", tradeAllotment < totalCash);
            if (tradeAllotment < totalCash) {
                double ask = buyData.getPrice();
                logger.info("Ask: {}", ask);
                double contractCost = ask * 100;
                logger.info("Contract Cost: {}", contractCost);
                int quantity = (int) Math.floor(tradeAllotment /contractCost);
                logger.info("Quantity: {}", quantity);
                Map<String, String> parameters = new HashMap<>();
                parameters.put("class", "option");
                parameters.put("duration", "day");
                parameters.put("quantity", String.valueOf(quantity));
                parameters.put("side", "buy_to_open");
                parameters.put("option_symbol", buyData.getOption());
                parameters.put("price", String.valueOf(ask));
                parameters.put("type", "limit");
                parameters.put("tag", buyData.getRiskType().toUpperCase() + "-" + id + "-" +  TradeLeg.FILL);

                new Record<>("LottoTradeService.placeFill: Fill Parameters", new OrderFillRecord(
                        id,
                        totalEquity,
                        totalCash,
                        tradeAllotment,
                        buyData.getPrice(),
                        contractCost,
                        quantity,
                        parameters
                ));

                JsonNode json = accountService.post("/orders", parameters);
                JsonNode jsonNode = json.get("order");

                if (isOk(jsonNode)) {
                    logger.info("LottoTradeService.placeFill: Fill Successful: {}", id);
                    Long orderId = jsonNode.get("id").asLong();
                    LottoTrade trade = new LottoTrade(id, totalEquity, ask, quantity, orderId);
                    lottoTradeRepository.save(trade);
                } else {
                    logger.error("LottoTradeService.placeFill: Fill UnSuccessful: {}", id);
                }
            } else {
                logger.error("LottoTradeService.placeFill: Not enough cash available to make trade: {}", id);
            }
        } catch (Exception e) {
            logger.error("LottoTradeService.placeFill: ERROR: Exception", e);
        }
    }

    @Override
    public boolean placeMarketSell(LottoTrade trade, TradeLeg TradeLeg) {
        logger.info("LottoTradeService.placeMarketSell: Start");
        boolean result = true;

        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("class", "option");
            parameters.put("duration", "day");
            parameters.put("type", "market");
            parameters.put("option_symbol", trade.getOptionSymbol());
            parameters.put("side", "sell_to_close");
            parameters.put("tag", trade.getRiskType().name() + "-" + trade.getId() + "-" +  TradeLeg.name());

            switch (TradeLeg) {
                case TRIM1 -> parameters.put("quantity", String.valueOf(trade.getTrim1Quantity()));
                case STOP -> {
                    switch (trade.getTrimStatus()) {
                        case 0 -> parameters.put("quantity", String.valueOf(trade.getTrim1Quantity() + trade.getRunnersQuantity()));
                        case 1 -> parameters.put("quantity", String.valueOf(trade.getRunnersQuantity()));
                    }
                }
            }

            new Record<>("LottoTradeService.placeMarketSell: Parameters:", parameters);

            JsonNode response = accountService.post("/orders", parameters);
            JsonNode order = response.get("order");

            if (isOk(order)) {
                logger.info("LottoTradeService.placeMarketSell: Market Sell Successful: {}", trade.getId());
            } else {
                logger.error("LottoTradeService.placeMarketSell: Market Sell UnSuccessful: {}", trade.getId());
            }
        } catch (Exception e) {
            logger.error("LottoTradeService.placeMarketSell: ERROR: Exception: {}, ID: {}", e.getMessage(), trade.getId(), e);
            result = false;
        }

        return result;
    }

    @Override
    public void setLastAndMaxPrices(LottoTrade trade) throws IOException, URISyntaxException {
        logger.debug("LottoTradeService.setLastAndMaxPrices: Start: {}", trade.getId());
        JsonNode quote = marketService.getPrices(trade.getOptionSymbol());
        double bid = quote.get("bid").asDouble();
        double tradeMaxPrice = trade.getMaxPrice();
        logger.debug("Last Price: {}", bid);
        trade.setLastPrice(bid);
        logger.debug("Max Price: {}", Math.max(tradeMaxPrice, bid));
        trade.setMaxPrice(Math.max(tradeMaxPrice, bid));
    }

    @Override
    public void finalizeTrade(LottoTrade trade, TradeLegMap tradeLegMap) {
        logger.debug("LottoTradeService.finalizeTrade: Start: {}", trade.getId());
        int totalQuantity = trade.getQuantity();

        if (tradeLegMap.containsKey(TRIM1)) {
            JsonNode trim1 = tradeLegMap.get(TRIM1);
            logger.debug("LottoTradeService.finalizeTrade: Avg. Fill Price (TRIM1): {}", getAvgFillPrice(trim1));
            double finalTrim1Price = getAvgFillPrice(trim1);
            trade.setTrim1PriceFinal(finalTrim1Price);
            trade.setFinalAmount((int) (trade.getFinalAmount() + (trade.getTrim1Quantity() * (finalTrim1Price * 100))));
            totalQuantity = totalQuantity - trade.getTrim1Quantity();
        }

        JsonNode stop = tradeLegMap.get(STOP);
        logger.debug("LottoTradeService.finalizeTrade: Avg. Fill Price (STOP): {}", getAvgFillPrice(stop));
        double finalStopPrice = getAvgFillPrice(stop);
        trade.setStopPriceFinal(finalStopPrice);
        trade.setFinalAmount((int) (trade.getFinalAmount() + (totalQuantity * (finalStopPrice * 100))));
        trade.setCloseDate(getCloseDate(stop));
        trade.setPl(trade.getFinalAmount() - trade.getTradeAmount());
        trade.setPostTradeBalance(trade.getPreTradeBalance() + trade.getPl());
    }

    public void handleOpenTrades(LottoTrade trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades) {
        if (trade.getTrimStatus() < 1 && (lastPrice >= trade.getTrim1Price())) {
            trade.setTrimStatus((byte) 1);
            logger.info("LottoTradeManager.watch: {}: Trim 1 Hit!: {}", riskType, id);
            placeMarketSell(trade, TRIM1);
            trade.setStopPrice(trade.getRunnersFloorPrice());
            trade.setStatus(RUNNERS);
            logger.info("LottoTradeManager.watch: {}: (OPEN -> RUNNERS): {}", riskType, id);
        }

        if (trade.getTrimStatus() > 0) {
            runnerTrades.add(id);
            logger.info("LottoTradeManager.watch: {}: Last Price: {}", riskType, lastPrice);
            logger.info("LottoTradeManager.watch: {}: Last Price > Stop Price: {}", riskType, lastPrice > trade.getStopPrice());
            if (lastPrice > (trade.getStopPrice() + trade.getRunnersDelta())) {
                double newFloor = roundedDouble(lastPrice - trade.getRunnersDelta());
                logger.info("LottoTradeManager.watch: {}: New Floor: {}", riskType, newFloor);
                trade.setStopPrice(newFloor);
            }
        }
    }

}