package com.project.apex.data.market;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuoteData {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("bid")
    private Double bid;

    @JsonProperty("ask")
    private Double ask;

    @JsonProperty("strike")
    private Double strike;

    @JsonAlias("option_type")
    @JsonProperty("optionType")
    private String optionType;

    @JsonAlias("expiration_date")
    @JsonProperty("expirationDate")
    private String expirationDate;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getBid() {
        return bid;
    }

    public void setBid(Double bid) {
        this.bid = bid;
    }

    public Double getAsk() {
        return ask;
    }

    public void setAsk(Double ask) {
        this.ask = ask;
    }

    public Double getStrike() {
        return strike;
    }

    public void setStrike(Double strike) {
        this.strike = strike;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
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