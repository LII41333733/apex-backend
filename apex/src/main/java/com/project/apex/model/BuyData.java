package com.project.apex.model;

public class BuyData {

    double price;

    String option;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    @Override
    public String toString() {
        return "BuyData{" +
                "price=" + price +
                ", option='" + option + '\'' +
                '}';
    }
}
