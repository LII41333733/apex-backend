package com.project.apex.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Balance {

    @JsonProperty("unsettledFunds")
    @JsonAlias("balances.cash.unsettled_funds")
    private BigDecimal unsettledFunds;

    @JsonProperty("cashAvailable")
    @JsonAlias("balances.cash.cash_available")
    private BigDecimal cashAvailable;

    @JsonProperty("marketValue")
    @JsonAlias("balances.market_value")
    private BigDecimal marketValue;

    @JsonProperty("openPl")
    @JsonAlias("balances.open_pl")
    private BigDecimal openPl;

    @JsonProperty("closePl")
    @JsonAlias("balances.close_pl")
    private BigDecimal closePl;

    // Amount of cash being held for open orders
    @JsonProperty("pendingCash")
    @JsonAlias("balances.pending_cash")
    private BigDecimal pendingCash;

    // Cash unavailable for trading in the account
    @JsonProperty("unclearedFunds")
    @JsonAlias("balances.uncleared_funds")
    private BigDecimal unclearedFunds;

    // Margin account: Sandbox
    @JsonProperty("totalEquity")
    @JsonAlias("balances.total_equity")
    private BigDecimal totalEquity;

    @JsonProperty("totalCash")
    @JsonAlias("balances.total_cash")
    private BigDecimal totalCash;

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

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public BigDecimal getOpenPl() {
        return openPl;
    }

    public void setOpenPl(BigDecimal openPl) {
        this.openPl = openPl;
    }

    public BigDecimal getClosePl() {
        return closePl;
    }

    public void setClosePl(BigDecimal closePl) {
        this.closePl = closePl;
    }

    public BigDecimal getPendingCash() {
        return pendingCash;
    }

    public void setPendingCash(BigDecimal pendingCash) {
        this.pendingCash = pendingCash;
    }

    public BigDecimal getUnclearedFunds() {
        return unclearedFunds;
    }

    public void setUnclearedFunds(BigDecimal unclearedFunds) {
        this.unclearedFunds = unclearedFunds;
    }

    public BigDecimal getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(BigDecimal totalCash) {
        this.totalCash = totalCash;
    }

    public BigDecimal getTotalEquity() {
        return totalEquity;
    }

    public void setTotalEquity(BigDecimal totalEquity) {
        this.totalEquity = totalEquity;
    }

    @Override
    public String toString() {
        return "Balance{" +
                "unsettledFunds=" + unsettledFunds +
                ", cashAvailable=" + cashAvailable +
                ", marketValue=" + marketValue +
                ", openPl=" + openPl +
                ", closePl=" + closePl +
                ", pendingCash=" + pendingCash +
                ", unclearedFunds=" + unclearedFunds +
                ", totalEquity=" + totalEquity +
                ", totalCash=" + totalCash +
                '}';
    }
}
