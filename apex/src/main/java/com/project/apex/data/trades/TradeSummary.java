package com.project.apex.data.trades;

import com.project.apex.model.Trade;

public record TradeSummary(TradeRecord<Trade> baseTrades, TradeRecord<Trade> lottoTrades) {}
