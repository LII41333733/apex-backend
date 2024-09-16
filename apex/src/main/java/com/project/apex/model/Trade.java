package com.project.apex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass  // This class has no table but its properties will be inherited
public abstract class Trade {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "pre_trade_balance")
    private Double preTradeBalance;
    @Column(name = "post_trade_balance")
    private Double postTradeBalance;
    @Column(name = "option_symbol", length = 25)
    private String optionSymbol;
    @Column(name = "symbol", length = 3)
    private String symbol;
    @Column(name = "fill_price")
    private Double fillPrice;
    @Column(name = "open_date")
    private LocalDateTime openDate;
    @Column(name = "close_date")
    private LocalDateTime closeDate;
    @Column(name = "max_price")
    private Double maxPrice;
    @Column(name = "quantity")
    private Integer quantity;
    @Column(name = "pl")
    private Double pl;
    @Column(name = "trade_amount")
    private Integer tradeAmount;
    @Column(name = "last_price")
    private Double lastPrice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPreTradeBalance() {
        return preTradeBalance;
    }

    public void setPreTradeBalance(Double preTradeBalance) {
        this.preTradeBalance = preTradeBalance;
    }

    public Double getPostTradeBalance() {
        return postTradeBalance;
    }

    public void setPostTradeBalance(Double postTradeBalance) {
        this.postTradeBalance = postTradeBalance;
    }

    public String getOptionSymbol() {
        return optionSymbol;
    }

    public void setOptionSymbol(String optionSymbol) {
        this.optionSymbol = optionSymbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getFillPrice() {
        return fillPrice;
    }

    public void setFillPrice(Double fillPrice) {
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

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPl() {
        return pl;
    }

    public void setPl(Double pl) {
        this.pl = pl;
    }

    public Integer getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(Integer tradeAmount) {
        this.tradeAmount = tradeAmount;
    }

    public Double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(Double lastPrice) {
        this.lastPrice = lastPrice;
    }
}
