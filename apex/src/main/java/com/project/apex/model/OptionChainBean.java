package com.project.apex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignore properties not mapped
public class OptionChainBean {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("option_type")
    private String optionType;

    @JsonProperty("strike")
    private BigDecimal strike;

    // Getters and Setters

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public BigDecimal getStrike() {
        return strike;
    }

    public void setStrike(BigDecimal strike) {
        this.strike = strike;
    }
}
