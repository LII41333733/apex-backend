package com.project.apex.data.trades;

public class TradeProfile {
    private RiskType riskType;
    private Double tradeAmountAllotment = null;
    private Double tradeAmountPercentage = null;
    private Double stopPercentage;
    private Double trim1Percentage;
    private Double trim2Percentage;
    private int[] demoOutcomePercentages;

    public TradeProfile() {}

    public TradeProfile(
            RiskType riskType,
            Double tradeAmountAllotment,
            Double tradeAmountPercentage,
            Double stopPercentage,
            Double trim1Percentage,
            Double trim2Percentage,
            int[] demoOutcomePercentages
    ) {
        this.riskType = riskType;
        this.tradeAmountAllotment = tradeAmountAllotment;
        this.tradeAmountPercentage = tradeAmountPercentage;
        this.stopPercentage = stopPercentage;
        this.trim1Percentage = trim1Percentage;
        this.trim2Percentage = trim2Percentage;
        this.demoOutcomePercentages = demoOutcomePercentages;
    }

    public RiskType getRiskType() {
        return riskType;
    }

    public Double getTradeAmountPercentage() {
        return tradeAmountPercentage;
    }

    public Double getTradeAmountAllotment() {
        return tradeAmountAllotment;
    }

    public Double getStopPercentage() {
        return stopPercentage;
    }

    public Double getTrim1Percentage() {
        return trim1Percentage;
    }

    public Double getTrim2Percentage() {
        return trim2Percentage;
    }

    public int[] getDemoOutcomePercentages() {
        return demoOutcomePercentages;
    }
}
