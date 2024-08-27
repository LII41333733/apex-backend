package com.project.apex.data.orders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apex.data.Leg;
import com.project.apex.data.Order;
import com.project.apex.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderSummary {

    private static final Logger logger = LoggerFactory.getLogger(OrderSummary.class);
    private final List<Order> pendingOrders = new ArrayList<>();
    private final List<Order> openOrders = new ArrayList<>();
    private final List<Order> otherOrders = new ArrayList<>();

    public void update(JsonNode ordersNode) throws JsonProcessingException {
        if (ordersNode.isArray()) { // [{"id":13872135,"stat
            for (JsonNode orderNode : ordersNode) {
                assignMap(orderNode);
            }
        } else if (ordersNode.isObject()) { // {"id":13872135,"status":"rejected",
            assignMap(ordersNode);
        }
    }

    private void assignMap (JsonNode orderNode) throws JsonProcessingException {
        boolean isValidOrder = orderNode.get("class").asText().equals("otoco");

       if (isValidOrder) {
           logger.info("Assigning JsonNode to Order: {}", orderNode);

           try {
               Order order = new ObjectMapper().treeToValue(orderNode, Order.class);
               Leg triggerLeg = order.getLeg().get(0);

               switch (triggerLeg.getStatus()) {
                   case "open", "partially_filled", "pending" -> pendingOrders.add(order);
                   case "filled" -> openOrders.add(order);
                   case "expired", "canceled", "rejected", "error" -> otherOrders.add(order);
               }
           } catch (JsonProcessingException e) {
               logger.error(e.getMessage());
           }
       } else {
           logger.warn("Non-option: {}", orderNode);
       }
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
}
