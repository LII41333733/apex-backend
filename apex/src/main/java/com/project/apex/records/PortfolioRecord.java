package com.project.apex.records;

import com.project.apex.model.Trade;
import java.util.List;

public record PortfolioRecord(List<Trade> allTrades) {}
