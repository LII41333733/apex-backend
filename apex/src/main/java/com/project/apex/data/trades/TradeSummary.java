package com.project.apex.data.trades;

import com.project.apex.model.BaseTrade;
import com.project.apex.model.LottoTrade;
import com.project.apex.model.VisionTrade;

public record TradeSummary(TradeRecord<BaseTrade> baseTrades, TradeRecord<LottoTrade> lottoTrades, TradeRecord<VisionTrade> visionTrades) {}
