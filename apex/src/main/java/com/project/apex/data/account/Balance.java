package com.project.apex.data.account;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Balance {

    @JsonProperty("unsettledFunds")
    @JsonAlias("balances.cash.unsettled_funds")
    private Double unsettledFunds;

    @JsonProperty("cashAvailable")
    @JsonAlias("balances.cash.cash_available")
    private Double cashAvailable;

    @JsonProperty("marketValue")
    @JsonAlias("balances.market_value")
    private Double marketValue;

    @JsonProperty("openPl")
    @JsonAlias("balances.open_pl")
    private Double openPl;

    @JsonProperty("closePl")
    @JsonAlias("balances.close_pl")
    private Double closePl;

    // Amount of cash being held for open orders
    @JsonProperty("pendingCash")
    @JsonAlias("balances.pending_cash")
    private Double pendingCash;

    // Cash unavailable for trading in the account
    @JsonProperty("unclearedFunds")
    @JsonAlias("balances.uncleared_funds")
    private Double unclearedFunds;

    // Margin account: Sandbox
    @JsonProperty("totalEquity")
    @JsonAlias("balances.total_equity")
    private Double totalEquity;

    @JsonProperty("totalCash")
    @JsonAlias("balances.total_cash")
    private Double totalCash;

    public Double getUnsettledFunds() {
        return unsettledFunds;
    }

    public void setUnsettledFunds(Double unsettledFunds) {
        this.unsettledFunds = unsettledFunds;
    }

    public Double getCashAvailable() {
        return cashAvailable;
    }

    public void setCashAvailable(Double cashAvailable) {
        this.cashAvailable = cashAvailable;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(Double marketValue) {
        this.marketValue = marketValue;
    }

    public Double getOpenPl() {
        return openPl;
    }

    public void setOpenPl(Double openPl) {
        this.openPl = openPl;
    }

    public Double getClosePl() {
        return closePl;
    }

    public void setClosePl(Double closePl) {
        this.closePl = closePl;
    }

    public Double getPendingCash() {
        return pendingCash;
    }

    public void setPendingCash(Double pendingCash) {
        this.pendingCash = pendingCash;
    }

    public Double getUnclearedFunds() {
        return unclearedFunds;
    }

    public void setUnclearedFunds(Double unclearedFunds) {
        this.unclearedFunds = unclearedFunds;
    }

    public Double getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(Double totalCash) {
        this.totalCash = totalCash;
    }

    public Double getTotalEquity() {
        return totalEquity;
    }

    public void setTotalEquity(Double totalEquity) {
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
