package com.project.apex.data.trades;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TradeMap extends HashMap<Long, TradeLegMap> {
//    // A method to add a trade leg to a specific moment code (with no overwrite)
//    public void addTradeLeg(Long momentCode, TradeLeg tradeLeg, JsonNode jsonNode) {
//        TradeLegMap tradeLegMap = this.computeIfAbsent(momentCode, k -> new HashMap<>());
//        tradeLegMap.put(tradeLeg, jsonNode);
//    }

    // A method to overwrite all trade legs for a specific Long
    public void setTradeMap(Long momentCode, TradeLegMap newTradeLegMap) {
        // Simply replace the entire map associated with the Long
        this.put(momentCode, newTradeLegMap);
    }

    // Method to set or update a specific TradeLeg for a Long
    public void setTradeLeg(Long momentCode, TradeLeg tradeLeg, JsonNode jsonNode) {
        // Get the map for the Long or create a new one if it doesn't exist
        TradeLegMap tradeLegMap = this.computeIfAbsent(momentCode, k -> new TradeLegMap());

        // Set or update the specific TradeLeg with the JsonNode
        tradeLegMap.put(tradeLeg, jsonNode);
    }

    // A method to retrieve the JsonNode for a specific Long and TradeLeg
    public JsonNode getTradeLeg(Long momentCode, TradeLeg tradeLeg) {
        return this.get(momentCode).get(tradeLeg);
    }

    @Override
    public String toString() {
        return "TradeMap{} " + super.toString();
    }
}