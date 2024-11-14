package com.project.apex.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.apex.data.trades.TradeFactory;
import com.project.apex.model.Trade;
import com.project.apex.records.PortfolioRecord;
import com.project.apex.util.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class Portfolio {
    TradeFactory tradeFactory;
    ClientWebSocket clientWebSocket;

    List<Trade> allTrades;
    List<Trade> positivePlTrades = new ArrayList<>();
    List<Trade> negativePlTrades = new ArrayList<>();

    @Autowired
    public Portfolio(TradeFactory tradeFactory, ClientWebSocket clientWebSocket) {
        this.tradeFactory = tradeFactory;
        this.clientWebSocket = clientWebSocket;
    }

    public void fetchAllTrades() throws IOException {
        List<Trade> allTrades = tradeFactory.fetchAllTrades();
        setAllTrades(allTrades);
        createVisionChartData();
    }

    public void createVisionChartData() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            // Define the list type you want to read
            List<Trade> dataList = objectMapper.readValue(
                    new File("demoTrades.json"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Trade.class)
            );

            for (Trade trade : dataList) {
                if (trade.getPl() >= 0) {
                    positivePlTrades.add(trade);
                } else {
                    negativePlTrades.add(trade);
                }
            }

            clientWebSocket.sendData(
                    new Record<>(
                            "portfolio",
                            new PortfolioRecord(allTrades)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<Trade> getAllTrades() {
        return allTrades;
    }

    public void setAllTrades(List<Trade> allTrades) {
        this.allTrades = allTrades;
    }


}
