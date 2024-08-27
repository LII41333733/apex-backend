package com.project.apex.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuoteData {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("bid")
    private BigDecimal bid;

    @JsonProperty("ask")
    private BigDecimal ask;

    @JsonProperty("strike")
    private BigDecimal strike;

    @JsonAlias("option_type")
    @JsonProperty("optionType")
    private String optionType;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    public BigDecimal getStrike() {
        return strike;
    }

    public void setStrike(BigDecimal strike) {
        this.strike = strike;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    @Override
    public String toString() {
        return "QuoteData{" +
                "symbol='" + symbol + '\'' +
                ", bid=" + bid +
                ", ask=" + ask +
                ", strike=" + strike +
                ", optionType='" + optionType + '\'' +
                '}';
    }
}