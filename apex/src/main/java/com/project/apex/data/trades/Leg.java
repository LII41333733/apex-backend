package com.project.apex.data.trades;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Leg {
    @JsonAlias("avg_fill_price")
    @JsonProperty("avgFillPrice")
    private double avgFillPrice;
    @JsonProperty("class")
    private String className;
    @JsonAlias("create_date")
    @JsonProperty("createDate")
    private String createDate;
    private String duration;
    @JsonAlias("exec_quantity")
    @JsonProperty("execQuantity")
    private double execQuantity;
    private long id;
    @JsonAlias("last_fill_price")
    @JsonProperty("lastFillPrice")
    private double lastFillPrice;
    @JsonAlias("last_fill_quantity")
    @JsonProperty("lastFillQuantity")
    private double lastFillQuantity;
    @JsonAlias("option_symbol")
    @JsonProperty("optionSymbol")
    private String optionSymbol;
    private double price;
    private Integer quantity;
    @JsonAlias("reason_description")
    @JsonProperty("reasonDescription")
    private String reasonDescription;
    @JsonAlias("remaining_quantity")
    @JsonProperty("remainingQuantity")
    private double remainingQuantity;
    private String side;
    private String status;
    private String symbol;
    @JsonAlias("transaction_date")
    @JsonProperty("transactionDate")
    private String transactionDate;
    private String type;
    @JsonAlias("stop_price")
    @JsonProperty("stopPrice")
    private double stopPrice;

    // Getters and Setters
    public double getAvgFillPrice() {
        return avgFillPrice;
    }

    public void setAvgFillPrice(double avgFillPrice) {
        this.avgFillPrice = avgFillPrice;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getExecQuantity() {
        return execQuantity;
    }

    public void setExecQuantity(double execQuantity) {
        this.execQuantity = execQuantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLastFillPrice() {
        return lastFillPrice;
    }

    public void setLastFillPrice(double lastFillPrice) {
        this.lastFillPrice = lastFillPrice;
    }

    public double getLastFillQuantity() {
        return lastFillQuantity;
    }

    public void setLastFillQuantity(double lastFillQuantity) {
        this.lastFillQuantity = lastFillQuantity;
    }

    public String getOptionSymbol() {
        return optionSymbol;
    }

    public void setOptionSymbol(String optionSymbol) {
        this.optionSymbol = optionSymbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }

    public double getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(double remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(double stopPrice) {
        this.stopPrice = stopPrice;
    }
}
