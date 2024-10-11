package com.project.apex.model;

import com.project.apex.data.trades.RiskType;
import com.project.apex.util.Record;
import jakarta.persistence.*;

import static com.project.apex.util.Convert.roundedDouble;

@Entity
@Table(name = "vision_trade")
public class VisionTrade extends Trade {

    @Enumerated(EnumType.STRING)
    @Column(name = "riskType")
    private RiskType riskType = RiskType.VISION;
    @Transient
    public final double tradePercentModifier = 0.042;
    @Transient
    public final double stopLossPercentage = 0;
    @Transient
    public final double initialRunnersFloorModifier = 1.20;
    @Column(name = "loss_id")
    private Long lossId;
    @Column(name = "recovery_id")
    private Integer recoveryId;
    @Column(name = "loss_streak")
    private Integer lossStreak;
    @Column(name = "win")
    private Integer win;
    @Column(name = "loss")
    private Integer loss;

    public VisionTrade() {}

//    @Override
//    public void calculateStopsAndTrims() {
//        double ask = this.getFillPrice();
//        double stopPrice = roundedDouble(ask * (1 - stopLossPercentage));
//        double trim1Price = roundedDouble(ask * (1 + trim1Percentage));
//        double initialRunnersFloorPrice = roundedDouble(trim1Price / initialRunnersFloorModifier);
//        this.setStopPrice(stopPrice);
//        this.setTrim1Price(trim1Price);
//        this.setRunnersFloorPrice(initialRunnersFloorPrice);
//        this.setRunnersDelta(roundedDouble(this.getTrim1Price() - initialRunnersFloorPrice));
//
//        this.setFillPrice(ask);
//        this.setTrim1Quantity(trim1Quantity);
//        this.setRunnersQuantity(runnersQuantity);
//        this.setTradeAmount(ask * 100 * this.getQuantity());
//        new Record<>("LottoTrade.calculateStopsAndTrims", this);
//    }

    @Override
    public RiskType getRiskType() {
        return riskType;
    }

    public void setRiskType(RiskType riskType) {
        this.riskType = riskType;
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
    public double getInitialRunnersFloorModifier() {
        return initialRunnersFloorModifier;
    }

    public Long getLossId() {
        return lossId;
    }

    public void setLossId(Long lossId) {
        this.lossId = lossId;
    }

    public Integer getRecoveryId() {
        return recoveryId;
    }

    public void setRecoveryId(Integer recoveryId) {
        this.recoveryId = recoveryId;
    }

    public Integer getLossStreak() {
        return lossStreak;
    }

    public void setLossStreak(Integer lossStreak) {
        this.lossStreak = lossStreak;
    }

    public Integer getWin() {
        return win;
    }

    public void setWin(Integer win) {
        this.win = win;
    }

    public Integer getLoss() {
        return loss;
    }

    public void setLoss(Integer loss) {
        this.loss = loss;
    }
}
