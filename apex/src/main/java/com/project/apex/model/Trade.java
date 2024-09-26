package com.project.apex.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.RiskType;
import com.project.apex.data.trades.TradeStatus;
import com.project.apex.util.TradeOrder;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import static com.project.apex.data.trades.TradeStatus.*;
import static com.project.apex.data.trades.TradeStatus.FINALIZED;

@MappedSuperclass
public abstract class Trade {

    public final double tradePercentModifier = 0.042;
    public final double stopLossPercentage = 0.40;
    public final double trim1Percentage = 0.25;
    public final double trim2Percentage = 0.50;
    public final double initialRunnersFloorModifier = 1.25;

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "fill_order_id")
    private Long fillOrderId;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "riskType")
    private RiskType riskType;
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
    @Column(name = "trim1_quantity")
    private Integer trim1Quantity;
    @Column(name = "runners_quantity")
    private Integer runnersQuantity;
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
    @Column(name = "trim_status")
    private byte trimStatus = 0;
    @Column(name = "stop_price")
    private Double stopPrice;
    @Column(name = "trim1_price")
    private Double trim1Price;
    @Column(name = "trim2_price")
    private Double trim2Price;
    @Column(name = "runners_floor_price")
    private Double runnersFloorPrice;
    @Column(name = "runners_delta")
    private Double runnersDelta;
    @Column(name = "trim1_price_final")
    private Double trim1PriceFinal = 0.0;
    @Column(name = "stop_price_final")
    private Double stopPriceFinal = 0.0;
    @Column(name = "trim2_quantity")
    private Integer trim2Quantity;
    @Column(name = "trim2_price_final")
    private Double trim2PriceFinal = 0.0;

    public Trade() {}

    public Trade(Long id, double totalEquity, double initialAsk, int quantity, Long fillOrderId) {
        this.setId(id);
        this.setPreTradeBalance(totalEquity);
        this.setInitialAsk(initialAsk);
        this.setFillPrice(initialAsk);
        this.setQuantity(quantity);
        this.setFillOrderId(fillOrderId);
    }

    public void initializeTrade(JsonNode fillOrder) {
        this.setFillPrice(
                TradeOrder.isFilled(fillOrder)
                    ? TradeOrder.getAverageFillPrice(fillOrder)
                    : TradeOrder.getPrice(fillOrder)
        );
        this.setOpenDate(TradeOrder.getCreateDate(fillOrder));
        this.setOptionSymbol(TradeOrder.getOptionSymbol(fillOrder));
        this.setSymbol(TradeOrder.getSymbol(fillOrder));
        this.calculateStopsAndTrims();
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

    public RiskType getRiskType() {
        return riskType;
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

    public byte getTrimStatus() {
        return trimStatus;
    }

    public void setTrimStatus(byte trimStatus) {
        this.trimStatus = trimStatus;
    }

    public Double getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(Double stopPrice) {
        this.stopPrice = stopPrice;
    }

    public Double getRunnersFloorPrice() {
        return runnersFloorPrice;
    }

    public void setRunnersFloorPrice(Double runnersFloorPrice) {
        this.runnersFloorPrice = runnersFloorPrice;
    }

    public Double getTrim1Price() {
        return trim1Price;
    }

    public void setTrim1Price(Double trim1Price) {
        this.trim1Price = trim1Price;
    }

    public Double getTrim2Price() {
        return trim2Price;
    }

    public void setTrim2Price(Double trim2Price) {
        this.trim2Price = trim2Price;
    }

    public Double getRunnersDelta() {
        return runnersDelta;
    }

    public void setRunnersDelta(Double runnersDelta) {
        this.runnersDelta = runnersDelta;
    }

    public Integer getTrim1Quantity() {
        return trim1Quantity;
    }

    public void setTrim1Quantity(Integer trim1Quantity) {
        this.trim1Quantity = trim1Quantity;
    }

    public Integer getRunnersQuantity() {
        return runnersQuantity;
    }

    public void setRunnersQuantity(Integer runnersQuantity) {
        this.runnersQuantity = runnersQuantity;
    }

    public Double getTrim1PriceFinal() {
        return trim1PriceFinal;
    }

    public void setTrim1PriceFinal(Double trim1PriceFinal) {
        this.trim1PriceFinal = trim1PriceFinal;
    }

    public Double getStopPriceFinal() {
        return stopPriceFinal;
    }

    public void setStopPriceFinal(Double stopPriceFinal) {
        this.stopPriceFinal = stopPriceFinal;
    }

    public Long getFillOrderId() {
        return fillOrderId;
    }

    public void setFillOrderId(Long fillOrderId) {
        this.fillOrderId = fillOrderId;
    }

    public double getTradePercentModifier() {
        return tradePercentModifier;
    }

    public double getStopLossPercentage() {
        return stopLossPercentage;
    }

    public double getTrim1Percentage() {
        return trim1Percentage;
    }

    public double getTrim2Percentage() {
        return trim2Percentage;
    }

    public double getInitialRunnersFloorModifier() {
        return initialRunnersFloorModifier;
    }

    public Integer getTrim2Quantity() {
        return trim2Quantity;
    }

    public void setTrim2Quantity(Integer trim2Quantity) {
        this.trim2Quantity = trim2Quantity;
    }

    public Double getTrim2PriceFinal() {
        return trim2PriceFinal;
    }

    public void setTrim2PriceFinal(Double trim2PriceFinal) {
        this.trim2PriceFinal = trim2PriceFinal;
    }

    public void calculateStopsAndTrims() {}

}