package com.project.apex.component;

import com.project.apex.data.trades.RiskType;
import com.project.apex.model.LottoTrade;
import com.project.apex.repository.LottoTradeRepository;
import com.project.apex.service.LottoTradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.project.apex.data.trades.TradeLeg.*;
import static com.project.apex.data.trades.TradeStatus.*;
import static com.project.apex.util.Convert.roundedDouble;

public class LottoTradeManager extends TradeManager<LottoTrade, LottoTradeRepository, LottoTradeService> {

    private static final Logger logger = LoggerFactory.getLogger(LottoTradeManager.class);

    public LottoTradeManager(
            LottoTradeService lottoTradeService,
            LottoTradeRepository lottoTradeRepository
    ) {
        super(lottoTradeRepository, lottoTradeService);
    }


}