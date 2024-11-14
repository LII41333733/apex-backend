package com.project.apex.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.data.orders.OrderStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TradeOrder {

    public static Integer getId(JsonNode order) {
        return order.get("id").asInt();
    }

    public static OrderStatus getStatus(JsonNode order) {
        return OrderStatus.valueOf(order.get("status").asText());
    }

    public static boolean isFilled(JsonNode order) {
        return getStatus(order) == OrderStatus.filled;
    }

    public static boolean isOpen(JsonNode order) {
        return getStatus(order) == OrderStatus.open;
    }

    public static boolean isCanceled(JsonNode order) {
        return getStatus(order) == OrderStatus.canceled;
    }

    public static boolean isRejected(JsonNode order) {
        return getStatus(order) == OrderStatus.rejected;
    }

    public static boolean isOk(JsonNode order) {
        return order != null && getStatus(order) == OrderStatus.ok;
    }

    public static String getSymbol(JsonNode order) {
        return order.get("symbol").asText();
    }

    public static LocalDateTime getCreateDate(JsonNode order) {
        return LocalDateTime.ofInstant(Instant.parse(order.get("create_date").asText()), ZoneId.of("America/New_York"));
    }

    public static LocalDateTime getCloseDate(JsonNode order) {
        return LocalDateTime.ofInstant(Instant.parse(order.get("transaction_date").asText()), ZoneId.of("America/New_York"));
    }

    public static String getOptionSymbol(JsonNode order) {
        return order.get("option_symbol").asText();
    }

    public static double getPrice(JsonNode order) {
        JsonNode avg = order.get("avg_fill_price");
        JsonNode price = order.get("price");
        if (avg != null) {
            return avg.asDouble();
        } else if (price != null) {
            return price.asDouble();
        } else {
            return 0;
        }
    }

    public static int getQuantity(JsonNode order) {
        return order.get("quantity").asInt();
    }
}
