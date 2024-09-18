package com.project.apex.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.BaseTrade.BaseTradeStatus;
import com.project.apex.data.trades.RiskType;
import com.project.apex.util.BaseTradeOrder;
import com.project.apex.util.Quantities;
import com.project.apex.util.Record;
import jakarta.persistence.*;

import java.util.List;

import static com.project.apex.data.trades.BaseTrade.BaseTradeStatus.NEW;
import static com.project.apex.data.trades.RiskType.BASE;
import static com.project.apex.util.BaseTradeOrder.*;
import static com.project.apex.util.BaseTradeOrder.getSymbol;
import static com.project.apex.util.Convert.roundedDouble;

@Entity
@Table(name = "base_trade")  // Root class declares the table
public class BaseTrade extends Trade {

    public static final double tradePercentModifier = 0.042;
    public static final double stopLossPercentage = 0.40;
    public static final double trim1Percentage = 0.25;
    public static final double trim2Percentage = 0.50;
    public static final double trim3Percentage = 0.75;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "risk_type")
    private RiskType riskType = BASE;
    @Column(name = "initial_ask")
    private Double initialAsk;
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
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    private BaseTradeStatus status = NEW;

    public BaseTrade() {}

    public BaseTrade(Long id, double totalEquity, double initialAsk, int quantity) {
        this.setId(id);
        this.setPreTradeBalance(totalEquity);
        this.setInitialAsk(initialAsk);
        this.setFillPrice(initialAsk);
        this.setQuantity(quantity);
        calculateStopsAndTrims();
    }

    public void initializeTrade(JsonNode fillOrder) {
        this.setFillPrice(BaseTradeOrder.getPrice(fillOrder));
        this.setOpenDate(BaseTradeOrder.getCreateDate(fillOrder));
        this.setOptionSymbol(BaseTradeOrder.getOptionSymbol(fillOrder));
        this.setSymbol(BaseTradeOrder.getSymbol(fillOrder));
        this.calculateStopsAndTrims();
    }

    public void calculateStopsAndTrims() {
        List<Integer> quantities = Quantities.divideIntoThreeGroups(this.getQuantity());
        Integer trim1Quantity = quantities.get(0);
        Integer trim2Quantity = quantities.get(1);
        Integer runnersQuantity = quantities.get(2);
        double ask = this.getFillPrice();

        if (this.getFillPrice() <= .1) {
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
            double initialRunnersFloorPrice = roundedDouble(trim2Price / 2);
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

    public Double getInitialAsk() {
        return initialAsk;
    }

    public void setInitialAsk(Double initialAsk) {
        this.initialAsk = initialAsk;
    }
}