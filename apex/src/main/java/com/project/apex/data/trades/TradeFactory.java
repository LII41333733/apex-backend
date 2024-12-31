package com.project.apex.data.trades;

import com.project.apex.model.*;
import com.project.apex.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.project.apex.data.trades.RiskType.*;

@Component
public class TradeFactory {

    private final BaseTradeService baseTradeService;
    private final LottoTradeService lottoTradeService;
    private final HeroTradeService heroTradeService;
    private final VisionTradeService visionTradeService;
    private final Map<RiskType, TradeProfile> tradeProfiles;

    @Autowired
    public TradeFactory(BaseTradeService baseTradeService,
                        LottoTradeService lottoTradeService,
                        HeroTradeService heroTradeService,
                        VisionTradeService visionTradeService,
                        Map<RiskType, TradeProfile> tradeProfiles) {
        this.baseTradeService = baseTradeService;
        this.lottoTradeService = lottoTradeService;
        this.heroTradeService = heroTradeService;
        this.visionTradeService = visionTradeService;
        this.tradeProfiles = tradeProfiles;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trade> TradeService<T> getTradeService(RiskType riskType) {
        return switch (riskType) {
            case Base -> (TradeService<T>) baseTradeService;
            case Lotto -> (TradeService<T>) lottoTradeService;
            case Hero -> (TradeService<T>) heroTradeService;
            case Vision -> (TradeService<T>) visionTradeService;
        };
    }

    @SuppressWarnings("unchecked")
    public <T extends Trade> T getTradeInstance(RiskType riskType) {
        return switch (riskType) {
            case Base -> (T) new BaseTrade(tradeProfiles.get(Base));
            case Lotto -> (T) new LottoTrade(tradeProfiles.get(Lotto));
            case Hero -> (T) new HeroTrade(tradeProfiles.get(Hero));
            case Vision -> (T) new VisionTrade(tradeProfiles.get(Vision));
        };
    }

    public List<Trade> fetchAllTrades() {
        List<Trade> trades = new ArrayList<>();
        trades.addAll(getTradeService(Base).fetchAllTrades());
        trades.addAll(getTradeService(Lotto).fetchAllTrades());
        trades.addAll(getTradeService(Hero).fetchAllTrades());
        trades.addAll(getTradeService(Vision).fetchAllTrades());
        trades.sort(Comparator.comparing(Trade::getOpenDate));
        return trades;
    }

    public <T extends Trade> void placeTrade(BuyDataRecord buyDataRecord) throws Exception {
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