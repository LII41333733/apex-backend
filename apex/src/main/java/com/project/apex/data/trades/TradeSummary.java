package com.project.apex.data.trades;

import com.project.apex.model.BaseTrade;
import com.project.apex.model.HeroTrade;
import com.project.apex.model.LottoTrade;

public record TradeSummary(TradeRecord<BaseTrade> baseTrades, TradeRecord<LottoTrade> lottoTrades, TradeRecord<HeroTrade> heroTrades) {}
