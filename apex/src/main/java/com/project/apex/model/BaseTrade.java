package com.project.apex.model;

import com.project.apex.data.trades.RiskType;
import com.project.apex.util.Quantities;
import com.project.apex.util.Record;
import jakarta.persistence.*;

import java.util.List;

import static com.project.apex.data.trades.RiskType.BASE;
import static com.project.apex.util.Convert.roundedDouble;

@Entity
@Table(name = "base_trade")
public class BaseTrade extends Trade {

    @Transient
    public final double tradePercentModifier = 0.042;
    @Transient
    public final double stopLossPercentage = 0.40;
    @Transient
    public final double trim1Percentage = 0.25;
    @Transient
    public final double trim2Percentage = 0.50;
    @Transient
    public final double initialRunnersFloorModifier = 1.25;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "risk_type")
    private final RiskType riskType = BASE;

    public BaseTrade() {}

    @Override
    public void calculateStopsAndTrims() {
        List<Integer> quantities = Quantities.divideIntoThreeGroups(this.getQuantity());
        int trim1Quantity = quantities.get(0);
        int trim2Quantity = quantities.get(1);
        int runnersQuantity = quantities.get(2);
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

    @Override
    public double getTradePercentModifier() {
        return tradePercentModifier;
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
    public double getInitialRunnersFloorModifier() {
        return initialRunnersFloorModifier;
    }
}