package com.project.apex.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.apex.interfaces.Trim1Tradeable;
import com.project.apex.interfaces.Trim2Tradeable;
import jakarta.persistence.*;
import static com.project.apex.data.trades.RiskType.BASE;

@Entity
@Table(name = "base_trade")
public class BaseTrade extends Trade implements Trim1Tradeable, Trim2Tradeable {

    @Column(name = "trim1_price")
    private Double trim1Price;
    @Column(name = "trim1_price_final")
    private Double trim1PriceFinal = 0.0;
    @Column(name = "trim1_quantity")
    private Integer trim1Quantity = 0;
    @Column(name = "trim2_price")
    private Double trim2Price;
    @Column(name = "trim2_price_final")
    private Double trim2PriceFinal = 0.0;
    @Column(name = "trim2_quantity")
    private Integer trim2Quantity = 0;

    @JsonIgnore
    @Transient
    private final double stopLossPercentage = 0.40;
    @JsonIgnore
    @Transient
    private final double trim1Percentage = 0.25;
    @JsonIgnore
    @Transient
    private final double trim2Percentage = 0.50;
    @JsonIgnore
    @Transient
    private final double runnersFloorPercentage = 0.25;

    public BaseTrade() {
        super(BASE, 0.08, new int[]{ 80, 75, 10 });
    }

    @Override
    public Double getTrim1Price() {
        return trim1Price;
    }
    @Override
    public void setTrim1Price(Double trim1Price) {
        this.trim1Price = trim1Price;
    }
    @Override
    public Double getTrim2Price() {
        return trim2Price;
    }
    @Override
    public void setTrim2Price(Double trim2Price) {
        this.trim2Price = trim2Price;
    }
    public Double getTrim1PriceFinal() {
        return trim1PriceFinal;
    }
    public void setTrim1PriceFinal(Double trim1PriceFinal) {
        this.trim1PriceFinal = trim1PriceFinal;
    }
    @Override
    public Integer getTrim1Quantity() {
        return trim1Quantity;
    }
    @Override
    public void setTrim1Quantity(Integer trim1Quantity) {
        this.trim1Quantity = trim1Quantity;
    }
    public void setTrim2PriceFinal(Double trim2PriceFinal) {
        this.trim2PriceFinal = trim2PriceFinal;
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
    public double getStopLossPercentage() {
        return stopLossPercentage;
    }
    public double getTrim1Percentage() {
        return trim1Percentage;
    }
    public double getTrim2Percentage() {
        return trim2Percentage;
    }
    public double getRunnersFloorPercentage() {
        return runnersFloorPercentage;
    }

}
