package com.project.apex.util;

import java.util.List;

public class Calculate {
    public static double getPercentValue(double value, double percent) {
        return Convert.roundedDouble(value - (value * percent));
    }
    public static int getValueByQuantity(int quantity, double price) {
        int contractCost = (int) (price * 100);
        return quantity * contractCost;
    }
    public static int addQuantitiesByLimit(List<Integer> quantities, int limit, boolean isExactIndex) {
        if (isExactIndex) {
            return quantities.get(limit);
        }
        return quantities.stream().limit(limit).mapToInt(Integer::intValue).sum();
    }
}
