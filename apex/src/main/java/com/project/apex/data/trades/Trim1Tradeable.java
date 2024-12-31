package com.project.apex.data.trades;

import com.project.apex.model.Trade;
import jakarta.persistence.Column;

public class Trim1Tradeable extends Trade implements Trim1TradeableImpl {

    @Column(name = "trim1_price")
    private Double trim1Price;
    @Column(name = "trim1_price_final")
    private Double trim1PriceFinal = 0.0;
    @Column(name = "trim1_quantity")
    private Integer trim1Quantity = 0;
    @Column(name = "trim1_percentage")
    private Double trim1Percentage = (double) 0;

    public Trim1Tradeable(TradeProfile tradeProfile) {
        super(tradeProfile);
    }

    public Trim1Tradeable() {
        super();
    }

    public Double getTrim1Price() {
        return trim1Price;
    }

    public void setTrim1Price(Double trim1Price) {
        this.trim1Price = trim1Price;
    }

    public Double getTrim1PriceFinal() {
        return trim1PriceFinal;
    }

    public void setTrim1PriceFinal(Double trim1PriceFinal) {
        this.trim1PriceFinal = trim1PriceFinal;
    }

    public Integer getTrim1Quantity() {
        return trim1Quantity;
    }

    public void setTrim1Quantity(Integer trim1Quantity) {
        this.trim1Quantity = trim1Quantity;
    }

    public Double getTrim1Percentage() {
        return trim1Percentage;
    }

}
