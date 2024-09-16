package com.project.apex.data.trades.BaseTrade;

import com.project.apex.model.BaseTrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BaseTradeSummary {

    private static final Logger logger = LoggerFactory.getLogger(BaseTradeSummary.class);
    private List<BaseTrade> allTrades;
    private List<BaseTrade> canceledTrades;
    private List<BaseTrade> pendingTrades;
    private List<BaseTrade> preOpenTrades;
    private List<BaseTrade> openTrades;
    private List<BaseTrade> filledTrades;
    private List<BaseTrade> otherTrades;


    public void clearTrades() {
        allTrades.clear();
        pendingTrades.clear();
        preOpenTrades.clear();
        openTrades.clear();
        filledTrades.clear();
        otherTrades.clear();
    }

    public void compile(List<BaseTrade> trades) {
        clearTrades();

        allTrades = trades;
        // sort trades and return a summary to be sent to UI.

        for (BaseTrade trade : trades) {
            switch (trade.getStatus()) {
//                case OPEN -> openTrades.add(trade);
//                case PENDING -> pendingTrades.add(trade);
//                case FILLED -> filledTrades.add(trade);
                default -> otherTrades.add(trade);
            }
        }
    }

    public List<BaseTrade> getAllTrades() {
        return allTrades;
    }

    public void setAllTrades(List<BaseTrade> allTrades) {
        this.allTrades = allTrades;
    }

    public List<BaseTrade> getCanceledTrades() {
        return canceledTrades;
    }

    public void setCanceledTrades(List<BaseTrade> canceledTrades) {
        this.canceledTrades = canceledTrades;
    }

    public List<BaseTrade> getPendingTrades() {
        return pendingTrades;
    }

    public void setPendingTrades(List<BaseTrade> pendingTrades) {
        this.pendingTrades = pendingTrades;
    }

    public List<BaseTrade> getPreOpenTrades() {
        return preOpenTrades;
    }

    public void setPreOpenTrades(List<BaseTrade> preOpenTrades) {
        this.preOpenTrades = preOpenTrades;
    }

    public List<BaseTrade> getOpenTrades() {
        return openTrades;
    }

    public void setOpenTrades(List<BaseTrade> openTrades) {
        this.openTrades = openTrades;
    }

    public List<BaseTrade> getFilledTrades() {
        return filledTrades;
    }

    public void setFilledTrades(List<BaseTrade> filledTrades) {
        this.filledTrades = filledTrades;
    }

    public List<BaseTrade> getOtherTrades() {
        return otherTrades;
    }

    public void setOtherTrades(List<BaseTrade> otherTrades) {
        this.otherTrades = otherTrades;
    }
}
