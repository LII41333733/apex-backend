package com.project.apex.data.trades;

import jakarta.persistence.Column;

public class Trim2Tradeable extends Trim1Tradeable implements Trim2TradeableImpl {

    @Column(name = "trim2_price")
    private Double trim2Price;
    @Column(name = "trim2_price_final")
    private Double trim2PriceFinal = 0.0;
    @Column(name = "trim2_quantity")
    private Integer trim2Quantity = 0;
    @Column(name = "trim2_percentage")
    private Double trim2Percentage = (double) 0;

    public Trim2Tradeable(TradeProfile tradeProfile) {
        super(tradeProfile);
    }

    public Trim2Tradeable() {
    }

    public Double getTrim2Price() {
        return trim2Price;
    }

    public void setTrim2Price(Double trim2Price) {
        this.trim2Price = trim2Price;
    }

    public Double getTrim2PriceFinal() {
        return trim2PriceFinal;
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

    public Double getTrim2Percentage() {
        return trim2Percentage;
    }
}
