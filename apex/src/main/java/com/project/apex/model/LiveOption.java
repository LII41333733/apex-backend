package com.project.apex.model;

public class LiveOption {
    private String type;
    private String symbol;
    private double bid;
    private double ask;

    // Constructor
    public LiveOption(String type, String symbol, double bid, double ask) {
        this.type = type;
        this.symbol = symbol;
        this.bid = bid;
        this.ask = ask;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    @Override
    public String toString() {
        return "LiveOption{" +
                "type='" + type + '\'' +
                ", symbol='" + symbol + '\'' +
                ", bid=" + bid +
                ", ask=" + ask +
                '}';
    }
}