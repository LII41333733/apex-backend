package com.project.apex.model;

import com.project.apex.data.trades.BaseTrade.BaseTradeStatus;
import com.project.apex.data.trades.RiskType;
import jakarta.persistence.*;

@Entity
@Table(name = "base_trade")  // Root class declares the table
public class BaseTrade extends Trade {

    public static final double tradePercentModifier = 0.042;
    public static final double stopLossPercentage = 0.42;
    public static final double trim1Percentage = 0.25;
    public static final double trim2Percentage = 0.50;
    public static final double trim3Percentage = 0.75;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "risk_type")
    private RiskType riskType = RiskType.BASE;
    @Column(name = "trim1_price")
    private Double trim1Price;
    @Column(name = "trim1_quantity")
    private Integer trim1Quantity;
    @Column(name = "trim2_price")
    private Double trim2Price;
    @Column(name = "trim2_quantity")
    private Integer trim2Quantity;
    @Column(name = "runners_quantity")
    private Integer runnersQuantity;
    @Column(name = "runners_floor_price")
    private Double runnersFloorPrice;
    @Column(name = "runners_delta")
    private Double runnersDelta;
    @Column(name = "stop_price")
    private Double stopPrice;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "trim_status")
    private byte trimStatus = 0;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    private BaseTradeStatus status = BaseTradeStatus.PENDING;



    public RiskType getRiskType() {
        return riskType;
    }

    public void setRiskType(RiskType riskType) {
        this.riskType = riskType;
    }

    public Double getTrim1Price() {
        return trim1Price;
    }

    public void setTrim1Price(Double trim1Price) {
        this.trim1Price = trim1Price;
    }

    public Integer getTrim1Quantity() {
        return trim1Quantity;
    }

    public void setTrim1Quantity(Integer trim1Quantity) {
        this.trim1Quantity = trim1Quantity;
    }

    public Double getTrim2Price() {
        return trim2Price;
    }

    public void setTrim2Price(Double trim2Price) {
        this.trim2Price = trim2Price;
    }

    public Integer getTrim2Quantity() {
        return trim2Quantity;
    }

    public void setTrim2Quantity(Integer trim2Quantity) {
        this.trim2Quantity = trim2Quantity;
    }

    public Integer getRunnersQuantity() {
        return runnersQuantity;
    }

    public void setRunnersQuantity(Integer runnersQuantity) {
        this.runnersQuantity = runnersQuantity;
    }

    public Double getRunnersFloorPrice() {
        return runnersFloorPrice;
    }

    public void setRunnersFloorPrice(Double runnersFloorPrice) {
        this.runnersFloorPrice = runnersFloorPrice;
    }

    public Double getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(Double stopPrice) {
        this.stopPrice = stopPrice;
    }

    public byte getTrimStatus() {
        return trimStatus;
    }

    public void setTrimStatus(byte trimStatus) {
        this.trimStatus = trimStatus;
    }

    public BaseTradeStatus getStatus() {
        return status;
    }

    public void setStatus(BaseTradeStatus status) {
        this.status = status;
    }

    public Double getRunnersDelta() {
        return runnersDelta;
    }

    public void setRunnersDelta(Double runnersDelta) {
        this.runnersDelta = runnersDelta;
    }
}