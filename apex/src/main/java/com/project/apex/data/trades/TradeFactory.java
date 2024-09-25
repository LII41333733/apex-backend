package com.project.apex.data.trades;

import com.project.apex.model.BaseTrade;
import com.project.apex.model.LottoTrade;
import com.project.apex.model.Trade;
import com.project.apex.repository.BaseTradeRepository;
import com.project.apex.repository.LottoTradeRepository;
import com.project.apex.repository.TradeRepository;
import com.project.apex.service.BaseTradeService;
import com.project.apex.service.LottoTradeService;
import com.project.apex.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TradeFactory {

    private final BaseTradeRepository baseTradeRepository;
    private final BaseTradeService baseTradeService;
    private final LottoTradeRepository lottoTradeRepository;
    private final LottoTradeService lottoTradeService;

    public TradeRepository tradeRepository;
    public TradeService tradeService;
    public Trade trade;

    @Autowired
    public TradeFactory(BaseTradeRepository baseTradeRepository, BaseTradeService baseTradeService, LottoTradeRepository lottoTradeRepository, LottoTradeService lottoTradeService) {
        this.baseTradeRepository = baseTradeRepository;
        this.baseTradeService = baseTradeService;
        this.lottoTradeRepository = lottoTradeRepository;
        this.lottoTradeService = lottoTradeService;
    }

    public void execute(RiskType riskType) {
        switch (riskType) {
            case BASE:
                tradeService = baseTradeService;
                tradeRepository = baseTradeRepository;
                trade = new BaseTrade();
                break;
            case LOTTO:
                tradeService = lottoTradeService;
                tradeRepository = lottoTradeRepository;
                trade = new LottoTrade();
                break;
            default:
                throw new IllegalArgumentException("Invalid riskType");
        }
    }

    public void placeFill(BuyDataRecord buyDataRecord) {
        RiskType riskType = buyDataRecord.riskType();
        execute(riskType);
        tradeService.placeFill(trade, buyDataRecord);
        tradeRepository.save(trade);
    }

    public void modifyTrade(ModifyTradeRecord modifyTradeRecord) {
        RiskType riskType = modifyTradeRecord.riskType();
        execute(riskType);
        tradeService.modifyTrade(modifyTradeRecord, tradeRepository);
    }

    public void watch(RiskType riskType, TradeMap tradeMap) throws IOException, URISyntaxException {
        execute(riskType);
        tradeService.watch(riskType, tradeMap, tradeRepository);
    }

    public void finalizeTrade(Trade trade, TradeLegMap tradeLegMap) {
        execute(trade.getRiskType());
        tradeService.finalizeTrade(trade, tradeLegMap);
    }

    public void sellTrade(SellTradeRecord sellTradeRecord) {
        execute(sellTradeRecord.riskType());
        tradeService.sellTrade(sellTradeRecord, tradeRepository);
    }

    public List<Trade> fetchAllTrades() {
        List<Trade> trades = new ArrayList<>();
        List<Trade> baseTrades = baseTradeRepository.findAll();
        List<Trade> lottoTrades = lottoTradeRepository.findAll();
        trades.addAll(baseTrades);
        trades.addAll(lottoTrades);
        return trades;
    }
}