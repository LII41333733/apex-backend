package com.project.apex.config;

import com.project.apex.component.BaseTradeManager;
import com.project.apex.component.LottoTradeManager;
import com.project.apex.component.TradeManager;
import com.project.apex.model.BaseTrade;
import com.project.apex.repository.BaseTradeRepository;
import com.project.apex.repository.LottoTradeRepository;
import com.project.apex.service.BaseTradeService;
import com.project.apex.service.LottoTradeService;
import com.project.apex.service.MarketService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradeManagerConfig {

    @Bean
    public BaseTradeManager baseTradeManager(BaseTradeRepository baseTradeRepository, BaseTradeService baseTradeService) {
        return new BaseTradeManager(baseTradeRepository, baseTradeService);
    }

    @Bean
    public LottoTradeManager lottoTradeManager(LottoTradeRepository lottoTradeRepository, LottoTradeService lottoTradeService) {
        return new LottoTradeManager(lottoTradeService, lottoTradeRepository);
    }
}
