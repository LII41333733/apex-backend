package com.project.apex.data.orders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.data.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OrderSummary {

    private static final Logger logger = LoggerFactory.getLogger(OrderSummary.class);
    private final List<Order> allOrders = new ArrayList<>();
    private final List<Order> pendingOrders = new ArrayList<>();
    private final List<Order> openOrders = new ArrayList<>();
    private final List<Order> filledOrders = new ArrayList<>();
    private final List<Order> otherOrders = new ArrayList<>();

    public void update(JsonNode ordersNode) throws JsonProcessingException {
        if (ordersNode.isArray()) {
            for (JsonNode orderNode : ordersNode) {
                assignMap(orderNode);
            }
        } else if (ordersNode.isObject()) {
            assignMap(ordersNode);
        }
    }

    private void assignMap (JsonNode orderNode) throws JsonProcessingException {
        boolean isValidOrder = orderNode.get("class").asText().equals("otoco");

       if (isValidOrder) {
           logger.info("Assigning JsonNode to Order: {}", orderNode);

           try {
               Order order = new ObjectMapper().treeToValue(orderNode, Order.class);
               String orderStatus = order.getStatus();

               allOrders.add(order);

               switch (orderStatus) {
                   case "open" -> openOrders.add(order);
                   case "pending" -> pendingOrders.add(order);
                   case "filled" -> filledOrders.add(order);
                   default -> otherOrders.add(order);
               }
           } catch (JsonProcessingException e) {
               logger.error(e.getMessage());
           }
       } else {
           logger.warn("Non-option: {}", orderNode);
       }
    }

    public List<Order> getAllOrders() {
        return allOrders;
    }

    public List<Order> getPendingOrders() {
        return pendingOrders;
    }

    public List<Order> getOpenOrders() {
        return openOrders;
    }

    public List<Order> getOtherOrders() {
        return otherOrders;
    }

    public List<Order> getFilledOrders() {
        return filledOrders;
    }
}
