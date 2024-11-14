package com.project.apex.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import static com.project.apex.data.trades.RiskType.HERO;

@Entity
@Table(name = "hero_trade")
public class HeroTrade extends Trade {

    @Column(name = "loss_id")
    private Long lossId = 0L;
    @Column(name = "recovery_id")
    private Integer recoveryId = 0;
    @Column(name = "loss_streak")
    private Integer lossStreak = 0;
    @Column(name = "win")
    private Integer win = 0;
    @Column(name = "loss")
    private Integer loss = 0;

    @JsonIgnore
    @Transient
    private final double stopLossPercentage = 0;
    @JsonIgnore
    @Transient
    private final double targetPercentage = 0.30;
    @JsonIgnore
    @Transient
    private final double runnersFloorDeltaPercentage = 0.25;

    public HeroTrade() {
        super(HERO, 0.04, new int[]{ 60 });
    }

    @JsonIgnore
    @Transient
    public double getStopLossPercentage() {
        return stopLossPercentage;
    }
    @JsonIgnore
    @Transient
    public double getTargetPercentage() {
        return targetPercentage;
    }
    @JsonIgnore
    @Transient
    public double getRunnersFloorDeltaPercentage() {
        return runnersFloorDeltaPercentage;
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
