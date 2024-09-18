package com.project.apex.data.orders;

import java.util.Map;

public record OrderFillRecord(
        Long id,
        double totalEquity,
        double totalCash,
        int tradeAllotment,
        double ask,
        double contractCost,
        int contractQuantity,
        Map<String, String> parameters
) {}
