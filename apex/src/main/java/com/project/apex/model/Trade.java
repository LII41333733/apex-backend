package com.project.apex.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.TradeStatus;
import com.project.apex.util.TradeOrder;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import static com.project.apex.data.trades.TradeStatus.*;
import static com.project.apex.data.trades.TradeStatus.FINALIZED;

@MappedSuperclass
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
    @Column(name = "symbol", length = 25)
    private String symbol;
    @Column(name = "fill_price")
    private Double fillPrice;
    @Column(name = "initial_ask")
    private Double initialAsk;
    @Column(name = "open_date")
    private LocalDateTime openDate;
    @Column(name = "close_date")
    private LocalDateTime closeDate;
    @Column(name = "max_price")
    private Double maxPrice = (double) 0;
    @Column(name = "quantity")
    private Integer quantity;
    @Column(name = "pl")
    private Integer pl = 0;
    @Column(name = "trade_amount")
    private Integer tradeAmount;
    @Column(name = "last_price")
    private Double lastPrice;
    @Column(name = "final_amount")
    private Integer finalAmount = 0;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    private TradeStatus status = NEW;

    public Trade() {}

    public Trade(Long id, double totalEquity, double initialAsk, int quantity) {
        this.setId(id);
        this.setPreTradeBalance(totalEquity);
        this.setInitialAsk(initialAsk);
        this.setFillPrice(initialAsk);
        this.setQuantity(quantity);
    }

    public void initializeTrade(JsonNode fillOrder) {
        this.setFillPrice(
                TradeOrder.isOpen(fillOrder)
                    ? TradeOrder.getPrice(fillOrder)
                    : TradeOrder.getAverageFillPrice(fillOrder)
        );
        this.setOpenDate(TradeOrder.getCreateDate(fillOrder));
        this.setOptionSymbol(TradeOrder.getOptionSymbol(fillOrder));
        this.setSymbol(TradeOrder.getSymbol(fillOrder));
    }

    public boolean isPending() {
        return this.getStatus() == PENDING;
    }

    public boolean isNew() {
        return this.getStatus() == NEW;
    }

    public boolean isOpen() {
        return this.getStatus() == OPEN;
    }

    public boolean hasRunners() {
        return this.getStatus() == RUNNERS;
    }

    public boolean isFilled() {
        return this.getStatus() == FILLED;
    }

    public boolean isFinalized() {
        return this.getStatus() == FINALIZED;
    }

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

    public Integer getPl() {
        return pl;
    }

    public void setPl(Integer pl) {
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

    public Integer getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Integer finalAmount) {
        this.finalAmount = finalAmount;
    }

    public Double getInitialAsk() {
        return initialAsk;
    }

    public void setInitialAsk(Double initialAsk) {
        this.initialAsk = initialAsk;
    }

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
    }
}
