package com.project.apex.data.trades;

import java.util.List;
import java.util.Map;

public record TradeRecord<T>(
        Map<Long, T> allTrades,
        List<Long> pendingTrades,
        List<Long> openTrades,
        List<Long> runnerTrades,
        List<Long> filledTrades,
        List<Long> canceledTrades,
        List<Long> rejectedTrades
) {}
