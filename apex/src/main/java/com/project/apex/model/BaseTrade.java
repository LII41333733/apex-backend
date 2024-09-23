package com.project.apex.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.RiskType;
import com.project.apex.util.Quantities;
import com.project.apex.util.Record;
import jakarta.persistence.*;

import java.util.List;

import static com.project.apex.data.trades.TradeStatus.*;
import static com.project.apex.data.trades.RiskType.BASE;
import static com.project.apex.util.Convert.roundedDouble;

@Entity
@Table(name = "base_trade")  // Root class declares the table
public class BaseTrade extends Trade {

    public static final double tradePercentModifier = 0.042;
    public static final double stopLossPercentage = 0.40;
    public static final double trim1Percentage = 0.25;
    public static final double trim2Percentage = 0.50;
    final double initialRunnersFloorModifier = 1.25;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "risk_type")
    private RiskType riskType = BASE;
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
    @Column(name = "trim_status")
    private byte trimStatus = 0;
    @Column(name = "trim1_price_final")
    private Double trim1PriceFinal = 0.0;
    @Column(name = "trim2_price_final")
    private Double trim2PriceFinal = 0.0;
    @Column(name = "stop_price_final")
    private Double stopPriceFinal = 0.0;

    public BaseTrade() {}

    public BaseTrade(Long id, double totalEquity, double initialAsk, int quantity) {
        super(id, totalEquity, initialAsk, quantity);
        calculateStopsAndTrims();
    }

    public void initializeTrade(JsonNode fillOrder) {
        super.initializeTrade(fillOrder);
        this.calculateStopsAndTrims();
    }

    public void calculateStopsAndTrims() {
        List<Integer> quantities = Quantities.divideIntoThreeGroups(this.getQuantity());
        int trim1Quantity = quantities.get(0);
        int trim2Quantity = quantities.get(1);
        int runnersQuantity = quantities.get(2);
        double ask = this.getFillPrice();

        if (this.getFillPrice() <= .1) {
            // LOTTOS ONLY!!!!!
            double initialRunnersFloorPrice = .13;
            this.setStopPrice(roundedDouble(ask / 2));
            this.setTrim1Price(this.getStopPrice() + .11);
            this.setTrim2Price(this.getStopPrice() + .16);
            this.setRunnersFloorPrice(initialRunnersFloorPrice);
            this.setRunnersDelta(roundedDouble(this.getTrim2Price() - initialRunnersFloorPrice));
        } else {
            double stopPrice = roundedDouble(ask * (1 - stopLossPercentage));
            double trim1Price = roundedDouble(ask * (1 + trim1Percentage));
            double trim2Price = roundedDouble(ask * (1 + trim2Percentage));
            double initialRunnersFloorPrice = roundedDouble(trim2Price / initialRunnersFloorModifier);
            this.setStopPrice(stopPrice);
            this.setTrim1Price(trim1Price);
            this.setTrim2Price(trim2Price);
            this.setRunnersFloorPrice(initialRunnersFloorPrice);
            this.setRunnersDelta(roundedDouble(this.getTrim2Price() - initialRunnersFloorPrice));
        }
        this.setFillPrice(ask);
        this.setTrim1Quantity(trim1Quantity);
        this.setTrim2Quantity(trim2Quantity);
        this.setRunnersQuantity(runnersQuantity);
        this.setTradeAmount((int) (ask * 100) * this.getQuantity());
        new Record<>("BaseTrade.calculateStopsAndTrims", this);
    }

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

    public Double getRunnersDelta() {
        return runnersDelta;
    }

    public void setRunnersDelta(Double runnersDelta) {
        this.runnersDelta = runnersDelta;
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

    public Double getTrim2PriceFinal() {
        return trim2PriceFinal;
    }

    public void setTrim2PriceFinal(Double trim2PriceFinal) {
        this.trim2PriceFinal = trim2PriceFinal;
    }
}