package com.project.apex.model;

import com.project.apex.data.trades.RiskType;
import jakarta.persistence.*;

@Entity
@Table(name = "vision_trade")
public class VisionTrade extends Trade {

    @Enumerated(EnumType.STRING)
    @Column(name = "riskType")
    private RiskType riskType = RiskType.VISION;
    @Transient
    private final double tradeAmountPercentage = 0.042;
    @Transient
    private final double stopLossPercentage = 0;
    @Transient
    private final double runnersFloorPercentage = 1.20;
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
//        double initialRunnersFloorPrice = roundedDouble(trim1Price / runnersFloorPercentage);
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
