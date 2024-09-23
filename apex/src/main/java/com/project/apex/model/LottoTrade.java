package com.project.apex.model;

import com.project.apex.data.trades.RiskType;
import com.project.apex.util.Record;
import jakarta.persistence.*;

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

    public LottoTrade() {}

    public LottoTrade(Long id, double totalEquity, double initialAsk, int quantity) {
        super(id, totalEquity, initialAsk, quantity);
        calculateStopsAndTrims();
    }

    @Override
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

}
