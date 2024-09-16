package com.project.apex.data.trades.BaseTrade;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.TradeLeg;
import com.project.apex.data.trades.TradeLegMap;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BaseTradeSnapshot {
    List<Map<Long, TradeLegMap>> snapshots = new ArrayList<>();

//    public Map<Long, TradeLegMap
}
