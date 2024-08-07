package com.project.apex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Balance {

    @JsonProperty("unsettled_funds")
    private BigDecimal unsettledFunds;

    @JsonProperty("cash_available")
    private BigDecimal cashAvailable;

    public BigDecimal getUnsettledFunds() {
        return unsettledFunds;
    }

    public void setUnsettledFunds(BigDecimal unsettledFunds) {
        this.unsettledFunds = unsettledFunds;
    }

    public BigDecimal getCashAvailable() {
        return cashAvailable;
    }

    public void setCashAvailable(BigDecimal cashAvailable) {
        this.cashAvailable = cashAvailable;
    }

}
