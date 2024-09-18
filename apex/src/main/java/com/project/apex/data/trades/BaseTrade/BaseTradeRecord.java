package com.project.apex.data.trades.BaseTrade;

import com.project.apex.model.BaseTrade;

import java.util.List;
import java.util.Map;

public record BaseTradeRecord(
        Map<Long, BaseTrade> allTrades,
        List<Long> pendingTrades,
        List<Long> openTrades,
        List<Long> runnerTrades,
        List<Long> filledTrades,
        List<Long> canceledTrades,
        List<Long> rejectedTrades
) {
    @Override
    public Map<Long, BaseTrade> allTrades() {
        return allTrades;
    }
}
