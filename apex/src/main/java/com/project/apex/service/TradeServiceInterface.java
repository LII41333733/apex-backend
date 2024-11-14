package com.project.apex.service;

import com.project.apex.data.trades.RiskType;
import com.project.apex.data.trades.SellTradeRecord;
import com.project.apex.data.trades.TradeLeg;
import com.project.apex.data.trades.TradeLegMap;
import com.project.apex.model.Trade;

import java.util.List;

public interface TradeServiceInterface<T extends Trade> {

    void calculateStopsAndTrims(T trade);
    void handleOpenTrades(T trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades);
    void finalizeTrade(T trade, TradeLegMap tradeLegMap);
    void sellTrade(SellTradeRecord sellTradeRecord);
    boolean prepareMarketSell(T trade, TradeLeg tradeLeg);

}
