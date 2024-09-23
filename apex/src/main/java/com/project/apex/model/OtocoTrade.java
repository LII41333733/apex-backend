//package com.project.apex.model;
//
//import com.project.apex.data.trades.RiskType;
//import jakarta.persistence.*;
//
//public class OtocoTrade extends Trade {
//
//    @Id
//    @Column(name = "id")
//    private Integer orderId;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "riskType")
//    private final RiskType riskType = RiskType.OTOCO;
//
//    @Column(name = "loss_id", length = 45, nullable = true)
//    private Integer lossId;
//
//    @Column(name = "loss_streak")
//    private Integer lossStreak;
//
//    @Column(name = "stop_price")
//    private double stopPrice;
//
//    @Column(name = "limit_price")
//    private double limitPrice;
//
//    @Column(name = "max_price")
//    private Integer maxPrice;
//
//    @Column(name = "recovery_id", length = 45)
//    private Integer recoveryId;
//
//    public OtocoTrade(Long id, double totalEquity, double initialAsk, int quantity) {
//        super(id, totalEquity, initialAsk, quantity);
//    }
//
//    public OtocoTrade() {
//
//    }
//
//    public double getLimitPrice() {
//        return limitPrice;
//    }
//
//    public void setLimitPrice(double limitPrice) {
//        this.limitPrice = limitPrice;
//    }
//
//    public Integer getOrderId() {
//        return orderId;
//    }
//
//    public void setOrderId(Integer orderId) {
//        this.orderId = orderId;
//    }
//
//    public RiskType getRiskType() {
//        return riskType;
//    }
//
//    public Integer getLossId() {
//        return lossId;
//    }
//
//    public void setLossId(Integer lossId) {
//        this.lossId = lossId;
//    }
//
//    public Integer getLossStreak() {
//        return lossStreak;
//    }
//
//    public void setLossStreak(Integer lossStreak) {
//        this.lossStreak = lossStreak;
//    }
//
//    public double getStopPrice() {
//        return stopPrice;
//    }
//
//    public void setStopPrice(double stopPrice) {
//        this.stopPrice = stopPrice;
//    }
//
//    public Integer getRecoveryId() {
//        return recoveryId;
//    }
//
//    public void setRecoveryId(Integer recoveryId) {
//        this.recoveryId = recoveryId;
//    }
//
//    @Override
//    public String toString() {
//        return "OtocoTrade{" +
//                "orderId=" + orderId +
//                ", riskType=" + riskType +
//                ", lossId=" + lossId +
//                ", lossStreak=" + lossStreak +
//                ", stopPrice=" + stopPrice +
//                ", limitPrice=" + limitPrice +
//                ", maxPrice=" + maxPrice +
//                ", recoveryId=" + recoveryId +
//                "} " + super.toString();
//    }
//}
