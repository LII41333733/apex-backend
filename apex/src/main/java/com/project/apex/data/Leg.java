package com.project.apex.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Leg {
    @JsonAlias("avg_fill_price")
    @JsonProperty("avgFillPrice")
    private BigDecimal avgFillPrice;
    @JsonProperty("class")
    private String className;
    @JsonAlias("create_date")
    @JsonProperty("createDate")
    private String createDate;
    private String duration;
    @JsonAlias("exec_quantity")
    @JsonProperty("execQuantity")
    private BigDecimal execQuantity;
    private long id;
    @JsonAlias("last_fill_price")
    @JsonProperty("lastFillPrice")
    private BigDecimal lastFillPrice;
    @JsonAlias("last_fill_quantity")
    @JsonProperty("lastFillQuantity")
    private BigDecimal lastFillQuantity;
    @JsonAlias("option_symbol")
    @JsonProperty("optionSymbol")
    private String optionSymbol;
    private BigDecimal price;
    private Integer quantity;
    @JsonAlias("reason_description")
    @JsonProperty("reasonDescription")
    private String reasonDescription;
    @JsonAlias("remaining_quantity")
    @JsonProperty("remainingQuantity")
    private BigDecimal remainingQuantity;
    private String side;
    private String status;
    private String symbol;
    @JsonAlias("transaction_date")
    @JsonProperty("transactionDate")
    private String transactionDate;
    private String type;
    @JsonAlias("stop_price")
    @JsonProperty("stopPrice")
    private BigDecimal stopPrice;

    // Getters and Setters
    public BigDecimal getAvgFillPrice() {
        return avgFillPrice;
    }

    public void setAvgFillPrice(BigDecimal avgFillPrice) {
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

    public BigDecimal getExecQuantity() {
        return execQuantity;
    }

    public void setExecQuantity(BigDecimal execQuantity) {
        this.execQuantity = execQuantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getLastFillPrice() {
        return lastFillPrice;
    }

    public void setLastFillPrice(BigDecimal lastFillPrice) {
        this.lastFillPrice = lastFillPrice;
    }

    public BigDecimal getLastFillQuantity() {
        return lastFillQuantity;
    }

    public void setLastFillQuantity(BigDecimal lastFillQuantity) {
        this.lastFillQuantity = lastFillQuantity;
    }

    public String getOptionSymbol() {
        return optionSymbol;
    }

    public void setOptionSymbol(String optionSymbol) {
        this.optionSymbol = optionSymbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
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

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(BigDecimal remainingQuantity) {
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

    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }
}
