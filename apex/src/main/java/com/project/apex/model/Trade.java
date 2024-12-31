package com.project.apex.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.project.apex.data.trades.RiskType;
import com.project.apex.data.trades.TradeProfile;
import com.project.apex.data.trades.TradeStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import static com.project.apex.data.trades.TradeStatus.*;
import static com.project.apex.data.trades.TradeStatus.FINALIZED;

@MappedSuperclass
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "riskType", visible = true)  // "type" should match the JSON field identifying the type
@JsonSubTypes({
        @JsonSubTypes.Type(value = BaseTrade.class, name = "Base"),
        @JsonSubTypes.Type(value = LottoTrade.class, name = "Lotto"),
        @JsonSubTypes.Type(value = VisionTrade.class, name = "Vision"),
        @JsonSubTypes.Type(value = HeroTrade.class, name = "Hero")
})
public abstract class Trade {

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "fill_order_id")
    private Long fillOrderId;
    @Column(name = "risk_type")
    @Enumerated(EnumType.STRING)
    @JsonProperty("riskType")
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
    private Double maxPrice = 0.0;
    @Column(name = "pl")
    private Integer pl = 0;
    @Column(name = "trade_amount")
    private Integer tradeAmount;
    @Column(name = "last_price")
    private Double lastPrice = 0.0;
    @Column(name = "final_amount")
    private Integer finalAmount = 0;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    private TradeStatus status = NEW;
    @Column(name = "trim_status")
    private int trimStatus = 0;
    @Column(name = "stop_price")
    private Double stopPrice;
    @Column(name = "stop_price_final")
    private Double stopPriceFinal = 0.0;
    @Column(name = "quantity")
    private Integer quantity = 0;
    @Column(name = "runners_quantity")
    private Integer runnersQuantity = 0;
    @Column(name = "stop_percentage")
    private Double stopPercentage = (double) 0;
    @Column(name = "runners_floor_active")
    private Boolean runnersFloorIsActive = false;
    @JsonIgnore
    @Transient
    @Column(name = "tradeProfile")
    private TradeProfile tradeProfile;
    @JsonIgnore
    @Transient
    private int[] demoOutcomePercentages = {};
    private double tradeAmountPercentage = 0;


    public Trade(TradeProfile tradeProfile) {
        this.tradeProfile = tradeProfile;
        this.riskType = tradeProfile.getRiskType();
    }

    public Trade() {
    }

    @JsonIgnore
    @Transient
    public boolean isPending() {
        return this.getStatus() == PENDING;
    }
    @JsonIgnore
    @Transient
    public boolean isNew() {
        return this.getStatus() == NEW;
    }
    @JsonIgnore
    @Transient
    public boolean isOpen() {
        return this.getStatus() == OPEN;
    }
    @JsonIgnore
    @Transient
    public boolean hasRunners() {
        return this.getStatus() == RUNNERS;
    }
    @JsonIgnore
    @Transient
    public boolean isFilled() {
        return this.getStatus() == FILLED;
    }
    @JsonIgnore
    @Transient
    public boolean isFinalized() {
        return this.getStatus() == FINALIZED;
    }
    @JsonIgnore
    @Transient
    public double getTradeAmountPercentage() {
        return tradeAmountPercentage;
    }
    @JsonIgnore
    @Transient
    public int[] getDemoOutcomePercentages() {
        return demoOutcomePercentages;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getFillOrderId() { return fillOrderId; }
    public void setFillOrderId(Long fillOrderId) { this.fillOrderId = fillOrderId; }
    public Double getPreTradeBalance() { return preTradeBalance; }
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
    public int getTrimStatus() {
        return trimStatus;
    }
    public void setTrimStatus(int trimStatus) {
        this.trimStatus = trimStatus;
    }
    public Double getStopPrice() {
        return stopPrice;
    }
    public void setStopPrice(Double stopPrice) {
        this.stopPrice = stopPrice;
    }
    public Integer getRunnersQuantity() {
        return runnersQuantity;
    }
    public void setRunnersQuantity(Integer runnersQuantity) {
        this.runnersQuantity = runnersQuantity;
    }
    public void setStopPriceFinal(Double stopPriceFinal) {
        this.stopPriceFinal = stopPriceFinal;
    }
    public Double getStopPriceFinal() {
        return stopPriceFinal;
    }
    public RiskType getRiskType() {
        return riskType;
    }
    public Double getStopPercentage() {
        return stopPercentage;
    }
    public Boolean getRunnersFloorIsActive() {
        return runnersFloorIsActive;
    }
    public void setRunnersFloorIsActive(Boolean runnersFloorIsActive) {
        this.runnersFloorIsActive = runnersFloorIsActive;
    }
    public TradeProfile getTradeProfile() {
        return tradeProfile;
    }

    public void setRiskType(RiskType riskType) {
        this.riskType = riskType;
    }
}
