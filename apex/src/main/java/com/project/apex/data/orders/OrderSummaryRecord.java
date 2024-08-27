package com.project.apex.data.orders;

public record OrderSummaryRecord(String type, OrderSummary data) {
    public OrderSummaryRecord(OrderSummary data) {
        this("orderSummary", data);
    }
}