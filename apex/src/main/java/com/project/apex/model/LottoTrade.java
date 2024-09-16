package com.project.apex.model;

import com.project.apex.data.trades.RiskType;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("LOTTO_TRADE")
public class LottoTrade extends Trade {

    @Id
    @Column(name = "order_id")
    private Integer orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "riskType")
    private final RiskType riskType = RiskType.LOTTO;

    @Column(name = "limit_price")
    private Double limitPrice;

    public RiskType getRiskType() {
        return riskType;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public Double getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(Double limitPrice) {
        this.limitPrice = limitPrice;
    }

    @Override
    public String toString() {
        return "LottoTrade{" +
                "orderId=" + orderId +
                ", riskType=" + riskType +
                ", limitPrice=" + limitPrice +
                "} " + super.toString();
    }
}
