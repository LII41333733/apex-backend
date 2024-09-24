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

    public static final double tradePercentModifier = 0.042;
    public static final double stopLossPercentage = 0.40;
    public static final double trim1Percentage = 0.25;
    public static final double trim2Percentage = 0.50;
    final double initialRunnersFloorModifier = 1.25;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "risk_type")
    private final RiskType riskType = BASE;
    @Column(name = "trim2_quantity")
    private Integer trim2Quantity;
    @Column(name = "trim2_price_final")
    private Double trim2PriceFinal = 0.0;

    public BaseTrade() {}

    public BaseTrade(Long id, double totalEquity, double initialAsk, int quantity, Long fillOrderId) {
        super(id, totalEquity, initialAsk, quantity, fillOrderId);
        calculateStopsAndTrims();
    }

    @Override
    public void calculateStopsAndTrims() {
        System.out.println("hello");

        List<Integer> quantities = Quantities.divideIntoThreeGroups(this.getQuantity());
        int trim1Quantity = quantities.get(0);
        int trim2Quantity = quantities.get(1);
        int runnersQuantity = quantities.get(2);
        double ask = this.getFillPrice();

        if (this.getFillPrice() <= .1) {
            // LOTTOS ONLY!!!!! Hide on UI
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

}