//package com.project.apex.service;
//
//import com.project.apex.component.ClientWebSocket;
//import com.project.apex.repository.TradeRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.event.EventListener;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//@Service
//public class OrderService {
//
//    @Autowired
//    private TradeRepository tradeRepository;
//
//    private boolean fetchPending = false;
//    private Map<String, Order> orders = new HashMap<>();
//
//    @EventListener(ClientWebSocket.class) // Replace with your custom event
//    public void onApplicationEvent() {
//        startFetching();
//    }
//
//    @Scheduled(fixedRate = 5000)
//    public void fetchOrders() {
//        if (fetchPending) {
//            Order order = fetchOrderData();
//            if (order != null && order.isCompleted()) {
//                updateOrderInDatabase(order);
//                fetchPending = false; // Stop fetching after completion
//            }
//        }
//    }
//
//    public void startFetching() {
//        fetchPending = true;
//    }
//
//    public Order fetchOrderData() {
//        return new Order(); // Replace with actual fetch logic
//    }
//
//    public void updateOrderInDatabase(Order order) {
//        orderRepository.save(order);
//    }
//}