package com.project.apex.data.trades;

import java.io.Serializable;

public record ModifyTradeRecord(Long id, TradeLeg tradeLeg, Double price, RiskType riskType) implements Serializable {
}
