package com.project.apex.data.trades;

public class BuyData {

    Double price;
    String option;
    String riskType;

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getRiskType() {
        return riskType.toUpperCase();
    }

    public void setRiskType(String riskType) {
        this.riskType = riskType;
    }

    @Override
    public String toString() {
        return "BuyData{" +
                "price=" + price +
                ", option='" + option + '\'' +
                ", riskType='" + riskType.toUpperCase() + '\'' +
                '}';
    }
}
