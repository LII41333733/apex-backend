package com.project.apex.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.trades.TradeStatus;
import com.project.apex.data.trades.RiskType;
import com.project.apex.util.Quantities;
import com.project.apex.util.Record;
import com.project.apex.util.TradeOrder;
import jakarta.persistence.*;

import java.util.List;

import static com.project.apex.data.trades.TradeStatus.NEW;
import static com.project.apex.util.Convert.roundedDouble;

@Entity
@Table(name = "lotto_trade")
public class LottoTrade extends Trade {

    public static final double tradePercentModifier = 0.02;
    public static final double stopLossPercentage = 0.75;
    public static final double trim1Percentage = 0.75;
    final double initialRunnersFloorModifier = 1.20;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "riskType")
    private final RiskType riskType = RiskType.LOTTO;
    @Column(name = "trim1_price")
    private Double trim1Price;
    @Column(name = "trim1_quantity")
    private Integer trim1Quantity;
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
    private TradeStatus status = NEW;
    @Column(name = "trim1_price_final")
    private Double trim1PriceFinal = 0.0;
    @Column(name = "stop_price_final")
    private Double stopPriceFinal = 0.0;

    public LottoTrade() {}

    public LottoTrade(Long id, double totalEquity, double initialAsk, int quantity) {
        super(id, totalEquity, initialAsk, quantity);
        calculateStopsAndTrims();
    }

    public void initializeTrade(JsonNode fillOrder) {
        super.initializeTrade(fillOrder);
        this.calculateStopsAndTrims();
    }

    public void calculateStopsAndTrims() {
        int quantity = this.getQuantity();  // Example number

        int trim1Quantity = (int) Math.round(quantity * 0.7);
        int runnersQuantity = quantity - trim1Quantity;
        double ask = this.getFillPrice();
        double stopPrice = roundedDouble(ask * (1 - stopLossPercentage));
        double trim1Price = roundedDouble(ask * (1 + trim1Percentage));
        double initialRunnersFloorPrice = roundedDouble(trim1Price / initialRunnersFloorModifier);
        this.setStopPrice(stopPrice);
        this.setTrim1Price(trim1Price);
        this.setRunnersFloorPrice(initialRunnersFloorPrice);
        this.setRunnersDelta(roundedDouble(this.getTrim1Price() - initialRunnersFloorPrice));

        this.setFillPrice(ask);
        this.setTrim1Quantity(trim1Quantity);
        this.setRunnersQuantity(runnersQuantity);
        this.setTradeAmount((int) (ask * 100) * this.getQuantity());
        new Record<>("BaseTrade.calculateStopsAndTrims", this);
    }

    public RiskType getRiskType() {
        return riskType;
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

    public Double getRunnersDelta() {
        return runnersDelta;
    }

    public void setRunnersDelta(Double runnersDelta) {
        this.runnersDelta = runnersDelta;
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

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
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
}
