package com.project.apex.data.trades;

import com.project.apex.model.BaseTrade;
import com.project.apex.model.LottoTrade;
import com.project.apex.model.Trade;
import com.project.apex.service.BaseTradeService;
import com.project.apex.service.LottoTradeService;
import com.project.apex.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.project.apex.data.trades.RiskType.BASE;
import static com.project.apex.data.trades.RiskType.LOTTO;

@Component
public class TradeFactory {

    private final BaseTradeService baseTradeService;
    private final LottoTradeService lottoTradeService;

    @Autowired
    public TradeFactory(BaseTradeService baseTradeService, LottoTradeService lottoTradeService) {
        this.baseTradeService = baseTradeService;
        this.lottoTradeService = lottoTradeService;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trade> TradeService<T> getTradeService(RiskType riskType) {
        switch (riskType) {
            case BASE:
                return (TradeService<T>) baseTradeService;
            case LOTTO:
                return (TradeService<T>) lottoTradeService;
            default:
                throw new IllegalArgumentException("Invalid riskType");
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Trade> T getTradeInstance(RiskType riskType) {
        switch (riskType) {
            case BASE:
                return (T) new BaseTrade();
            case LOTTO:
                return (T) new LottoTrade();
            default:
                throw new IllegalArgumentException("Invalid riskType");
        }
    }

    public List<Trade> fetchAllTrades() {
        List<Trade> trades = new ArrayList<>();
        trades.addAll(getTradeService(BASE).fetchAllTrades());
        trades.addAll(getTradeService(LOTTO).fetchAllTrades());
        return trades;
    }

    public <T extends Trade> void placeTrade(BuyDataRecord buyDataRecord) {
        RiskType riskType = buyDataRecord.riskType();
        TradeService<T> tradeService = getTradeService(riskType);
        tradeService.placeTrade(getTradeInstance(riskType), buyDataRecord);
    }

    public <T extends Trade> void modifyTrade(ModifyTradeRecord modifyTradeRecord) {
        RiskType riskType = modifyTradeRecord.riskType();
        TradeService<T> tradeService = getTradeService(riskType);
        tradeService.modifyTrade(modifyTradeRecord);
    }

    public <T extends Trade> void sellTrade(SellTradeRecord sellTradeRecord) {
        TradeService<T> tradeService = getTradeService(sellTradeRecord.riskType());
        tradeService.sellTrade(sellTradeRecord);
    }

    public <T extends Trade> TradeRecord<T> watch(RiskType riskType, TradeMap tradeMap) throws IOException, URISyntaxException {
        TradeService<T> tradeService = getTradeService(riskType);
        return tradeService.watch(riskType, tradeMap);
    }

}