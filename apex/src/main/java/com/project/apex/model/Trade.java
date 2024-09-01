package com.project.apex.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade")
public class Trade {

    @Id
    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "`option`", length = 25)
    private String option;

    @Column(name = "symbol", length = 3)
    private String symbol;

    @Column(name = "status", length = 25)
    private String status;

    @Column(name = "loss_id", length = 45)
    private Integer lossId;

    @Column(name = "loss_streak")
    private Integer lossStreak;

    @Column(name = "stop_price", precision = 10, scale = 2)
    private BigDecimal stopPrice;

    @Column(name = "limit_price", precision = 10, scale = 2)
    private BigDecimal limitPrice;

    @Column(name = "fill_price", precision = 10, scale = 2)
    private BigDecimal fillPrice;

    @Column(name = "open_date")
    private LocalDateTime openDate;

    @Column(name = "close_date")
    private LocalDateTime closeDate;

    @Column(name = "trade_result", length = 1)
    private String tradeResult;

    @Column(name = "max_price")
    private Integer maxPrice;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "recovery_id", length = 45)
    private Integer recoveryId;

    @Column(name = "pl", precision = 10, scale = 2)
    private BigDecimal pl;

    @Column(name = "wins")
    private Integer wins;

    @Column(name = "losses")
    private Integer losses;

    @Column(name = "trade_amount", precision = 10, scale = 2)
    private BigDecimal tradeAmount;

    @Column(name = "is_finalized")
    private boolean isFinalized;

    // Getters and Setters
    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getLossId() {
        return lossId;
    }

    public void setLossId(Integer lossId) {
        this.lossId = lossId;
    }

    public Integer getLossStreak() {
        return lossStreak;
    }

    public void setLossStreak(Integer lossStreak) {
        this.lossStreak = lossStreak;
    }

    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(BigDecimal limitPrice) {
        this.limitPrice = limitPrice;
    }

    public BigDecimal getFillPrice() {
        return fillPrice;
    }

    public void setFillPrice(BigDecimal fillPrice) {
        this.fillPrice = fillPrice;
    }

    public LocalDateTime getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDateTime openDate) {
        this.openDate = openDate;
    }

    public LocalDateTime getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDateTime closeDate) {
        this.closeDate = closeDate;
    }

    public String getTradeResult() {
        return tradeResult;
    }

    public void setTradeResult(String tradeResult) {
        this.tradeResult = tradeResult;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getRecoveryId() {
        return recoveryId;
    }

    public void setRecoveryId(Integer recoveryId) {
        this.recoveryId = recoveryId;
    }

    public BigDecimal getPl() {
        return pl;
    }

    public void setPl(BigDecimal pl) {
        this.pl = pl;
    }

    public Integer getLosses() {
        return losses;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
    }

    public BigDecimal getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(BigDecimal tradeAmount) {
        this.tradeAmount = tradeAmount;
    }

    public boolean isFinalized() {
        return isFinalized;
    }

    public void setFinalized(boolean finalized) {
        isFinalized = finalized;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "orderId=" + orderId +
                ", balance=" + balance +
                ", option='" + option + '\'' +
                ", symbol='" + symbol + '\'' +
                ", status='" + status + '\'' +
                ", lossId=" + lossId +
                ", lossStreak=" + lossStreak +
                ", stopPrice=" + stopPrice +
                ", limitPrice=" + limitPrice +
                ", fillPrice=" + fillPrice +
                ", openDate=" + openDate +
                ", closeDate=" + closeDate +
                ", tradeResult='" + tradeResult + '\'' +
                ", maxPrice=" + maxPrice +
                ", quantity=" + quantity +
                ", recoveryId=" + recoveryId +
                ", pl=" + pl +
                ", wins=" + wins +
                ", losses=" + losses +
                ", tradeAmount=" + tradeAmount +
                ", isFinalized=" + isFinalized +
                '}';
    }
}
