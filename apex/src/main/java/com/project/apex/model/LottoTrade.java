package com.project.apex.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.apex.interfaces.Trim1Tradeable;
import jakarta.persistence.*;
import static com.project.apex.data.trades.RiskType.LOTTO;

@Entity
@Table(name = "lotto_trade")
public class LottoTrade extends Trade implements Trim1Tradeable {

    @Column(name = "trim1_price")
    private Double trim1Price;
    @Column(name = "trim1_price_final")
    private Double trim1PriceFinal = 0.0;
    @Column(name = "trim1_quantity")
    private Integer trim1Quantity = 0;

    @Transient
    private final double stopLossPercentage = 0.50;
    @Transient
    private final double trim1Percentage = 0.50;
    @Transient
    private final double runnersFloorPercentage = 0.25;

    public LottoTrade() {
        super(LOTTO, 0.04, new int[]{ 75, 40 });
    }

    @Override
    public Double getTrim1Price() {
        return trim1Price;
    }
    @Override
    public void setTrim1Price(Double trim1Price) {
        this.trim1Price = trim1Price;
    }
    @JsonIgnore
    @Transient
    public double getStopLossPercentage() {
        return stopLossPercentage;
    }
    @JsonIgnore
    @Transient
    public double getTrim1Percentage() {
        return trim1Percentage;
    }
    @JsonIgnore
    @Transient
    public double getRunnersFloorPercentage() {
        return runnersFloorPercentage;
    }
    @Override
    public Integer getTrim1Quantity() {
        return trim1Quantity;
    }
    @Override
    public void setTrim1Quantity(Integer trim1Quantity) {
        this.trim1Quantity = trim1Quantity;
    }
    public Double getTrim1PriceFinal() {
        return trim1PriceFinal;
    }
    public void setTrim1PriceFinal(Double trim1PriceFinal) {
        this.trim1PriceFinal = trim1PriceFinal;
    }

}
