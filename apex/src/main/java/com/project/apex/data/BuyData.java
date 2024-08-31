package com.project.apex.data;

import java.math.BigDecimal;

public class BuyData {

    BigDecimal price;

    String option;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
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
