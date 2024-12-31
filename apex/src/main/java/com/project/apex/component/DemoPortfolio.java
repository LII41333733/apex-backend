package com.project.apex.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.apex.data.trades.TradeFactory;
import com.project.apex.data.trades.Trim1Tradeable;
import com.project.apex.data.trades.Trim2Tradeable;
import com.project.apex.model.*;
import com.project.apex.util.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@DependsOn("tradeSimulator")
public class DemoPortfolio {
    TradeFactory tradeFactory;
    ClientWebSocket clientWebSocket;

    List<Trade> allTrades;

    @Autowired
    public DemoPortfolio(TradeFactory tradeFactory, ClientWebSocket clientWebSocket) {
        this.tradeFactory = tradeFactory;
        this.clientWebSocket = clientWebSocket;
    }

    public List<Trade> fetchAllTrades() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        objectMapper.registerModule(new JavaTimeModule());

        try {
            // Define the list type you want to read
            allTrades = objectMapper.readValue(
                    new File("demoTrades.json"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Trade.class)
            );

        } catch (IOException e) {
            e.printStackTrace();
        }

        return allTrades;
    }

    public List<Trade> getAllTrades() {
        return allTrades;
    }

    public void setAllTrades(List<Trade> allTrades) {
        this.allTrades = allTrades;
    }


}
