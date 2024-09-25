//package com.project.apex.component;
//
//import com.project.apex.data.trades.RiskType;
//import com.project.apex.model.BaseTrade;
//import com.project.apex.repository.BaseTradeRepository;
//import com.project.apex.service.BaseTradeService;
//import com.project.apex.service.MarketService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import static com.project.apex.data.trades.TradeLeg.*;
//import static com.project.apex.data.trades.TradeStatus.*;
//import static com.project.apex.util.Convert.roundedDouble;
//
//@Component
//public class BaseTradeManager extends TradeManager<BaseTrade, BaseTradeRepository, BaseTradeService> {
//
//    private static final Logger logger = LoggerFactory.getLogger(BaseTradeManager.class);
//
//    @Autowired
//    public BaseTradeManager(
//            BaseTradeService baseTradeService
//    ) {
//        super(baseTradeService);
//    }
//}