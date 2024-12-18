package com.project.apex.util;

public class Calculate {
    public static double getPercentValue(double value, double percent) {
        return Convert.roundedDouble(value - (value * percent));
    }
}
