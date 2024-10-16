package com.project.apex.model;

import com.project.apex.data.trades.RiskType;
import com.project.apex.util.Record;
import jakarta.persistence.*;

import static com.project.apex.util.Convert.roundedDouble;

@Entity
@Table(name = "lotto_trade")
public class LottoTrade extends Trade {

    @Enumerated(EnumType.STRING)
    @Column(name = "riskType")
    private RiskType riskType = RiskType.LOTTO;
    @Transient
    private final double tradeAmountPercentage = 0.05;
    @Transient
    private final double stopLossPercentage = 0.75;
    @Transient
    private final double trim1Percentage = 0.75;
    @Transient
    private final double runnersFloorPercentage = 1.20;

    public LottoTrade() {}

    @Override
    public void calculateStopsAndTrims() {
        int trim1Quantity = (int) Math.round(this.getQuantity() * 0.7);
        int runnersQuantity = this.getQuantity() - trim1Quantity;
        double ask = this.getFillPrice();
        double stopPrice = roundedDouble(ask * (1 - stopLossPercentage));
        double trim1Price = roundedDouble(ask * (1 + trim1Percentage));
        double initialRunnersFloorPrice = roundedDouble(trim1Price / runnersFloorPercentage);
        this.setStopPrice(stopPrice);
        this.setTrim1Price(trim1Price);
        this.setRunnersFloorPrice(initialRunnersFloorPrice);
        this.setRunnersDelta(roundedDouble(this.getTrim1Price() - initialRunnersFloorPrice));

        this.setFillPrice(ask);
        this.setTrim1Quantity(trim1Quantity);
        this.setRunnersQuantity(runnersQuantity);
        this.setTradeAmount(ask * 100 * this.getQuantity());
        new Record<>("LottoTrade.calculateStopsAndTrims", this);
    }

    @Override
    public RiskType getRiskType() {
        return riskType;
    }

    @Override
    public double getTradeAmountPercentage() {
        return tradeAmountPercentage;
    }

    @Override
    public double getStopLossPercentage() {
        return stopLossPercentage;
    }

    @Override
    public double getTrim1Percentage() {
        return trim1Percentage;
    }

    @Override
    public double getTrim2Percentage() {
        return trim2Percentage;
    }

    @Override
    public double getRunnersFloorPercentage() {
        return runnersFloorPercentage;
    }
}
