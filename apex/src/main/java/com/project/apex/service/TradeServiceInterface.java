package com.project.apex.service;

import com.project.apex.data.trades.RiskType;
import com.project.apex.data.trades.TradeLegMap;
import com.project.apex.model.Trade;

import java.util.List;

public interface TradeServiceInterface<T extends Trade> {
    void finalizeTrade(T trade, TradeLegMap tradeLegMap);
    void handleOpenTrades(T trade, double lastPrice, Long id, RiskType riskType, List<Long> runnerTrades);
}
