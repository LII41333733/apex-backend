package com.project.apex.data.trades;

import java.io.Serializable;

public record SellTradeRecord(Long id, RiskType riskType) implements Serializable {}