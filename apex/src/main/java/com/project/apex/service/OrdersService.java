package com.project.apex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.apex.component.ClientWebSocket;
import com.project.apex.data.trades.*;
import com.project.apex.data.trades.TradeRecord;
import com.project.apex.data.websocket.WebSocketData;
import com.project.apex.model.BaseTrade;
import com.project.apex.model.LottoTrade;
import com.project.apex.model.HeroTrade;
import com.project.apex.model.VisionTrade;
import com.project.apex.util.Record;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.function.Consumer;

import static com.project.apex.data.trades.RiskType.*;

@Service
public class OrdersService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersService.class);
    private final AccountService accountService;
    private final ClientWebSocket clientWebSocket;
    private final TradeFactory tradeFactory;

    @Autowired
    public OrdersService(ClientWebSocket clientWebSocket,
                         AccountService accountService,
                         TradeFactory tradeFactory) {
        this.accountService = accountService;
        this.clientWebSocket = clientWebSocket;
        this.tradeFactory = tradeFactory;
    }

    @PostConstruct
    public void fetchOrders() {
        logger.info("OrdersService.fetchOrders: Start: Fetching orders from Tradier");

        try {
            JsonNode response = accountService.get("/orders?includeTags=true");
            JsonNode orders = response.get("orders").get("order");
            new Record<>("OrdersService.fetchOrders: Orders Received: {}", orders);

            if (orders == null) {
                logger.info("OrdersService.fetchOrders: No orders found");
            } else {
                RiskMap ordersByRiskType = handleMapOrdersByRiskType(orders);
                TradeMap baseTradeMap = ordersByRiskType.get(Base);
                TradeMap visionTradeMap = ordersByRiskType.get(Vision);
                TradeMap lottoTradeMap = ordersByRiskType.get(Lotto);
                TradeMap heroTradeMap = ordersByRiskType.get(Hero);
                TradeRecord<BaseTrade> baseTradeRecord = tradeFactory.watch(Base, baseTradeMap);
                TradeRecord<VisionTrade> visionTradeRecord = tradeFactory.watch(Vision, visionTradeMap);
                TradeRecord<LottoTrade> lottoTradeRecord = tradeFactory.watch(Lotto, lottoTradeMap);
                TradeRecord<HeroTrade> heroTradeRecord = tradeFactory.watch(Hero, heroTradeMap);
                clientWebSocket.sendData(
                        new Record<>(
                                WebSocketData.POSITIONS.name(),
                                new TradeSummary(
                                        baseTradeRecord,
                                        visionTradeRecord,
                                        lottoTradeRecord,
                                        heroTradeRecord)));
            }
        } catch (Exception e) {
            logger.error("OrdersService.fetchOrders: ERROR: Exception: {}", e.getMessage(), e);
        }
    }

    public RiskMap handleMapOrdersByRiskType(JsonNode orders) {
        RiskMap ordersByRiskType = new RiskMap();
        Consumer<JsonNode> mapOrders = orderJson -> {
            JsonNode tag = orderJson.get("tag");
            logger.debug("OrdersService.handleMapOrdersByRiskType: Mapping Tag: {}", tag);

            if (tag != null) {
                String[] split = tag.asText().split("-");
                RiskType riskType = RiskType.valueOf(split[0]);
                Long id = Long.valueOf(split[1]);
                TradeLeg tradeLeg = TradeLeg.valueOf(split[2]);

                TradeMap tradeMap = ordersByRiskType.get(riskType);

                if (tradeMap == null) {
                    tradeMap = new TradeMap();
                }

                TradeLegMap tradeLegMap = tradeMap.get(id);

                if (tradeLegMap == null) {
                    tradeLegMap = new TradeLegMap();
                }

                if (!tradeLegMap.containsKey(tradeLeg)) {
                    tradeLegMap.put(tradeLeg, orderJson);
                }

                tradeMap.put(id, tradeLegMap);
                ordersByRiskType.put(riskType, tradeMap);
            }
        };

        if (orders.isObject()) {
            mapOrders.accept(orders);
        } else {
            for (JsonNode order : orders) {
                mapOrders.accept(order);
            }
        }

        return ordersByRiskType;
    }
}