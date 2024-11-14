package com.project.apex.data.trades;

import java.util.List;

public record TradeRecord<T>(
        List<T> allTrades,
        List<Long> pendingTrades,
        List<Long> openTrades,
        List<Long> runnerTrades,
        List<Long> filledTrades,
        List<Long> canceledTrades,
        List<Long> rejectedTrades
) {}
