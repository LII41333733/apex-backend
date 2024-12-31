package com.project.apex.config;

import com.project.apex.data.trades.RiskType;
import com.project.apex.data.trades.TradeProfile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.project.apex.data.trades.RiskType.*;

@Configuration
public class TradeProfilesConfig {

    @Bean
    public Map<RiskType, TradeProfile> tradeProfiles() {
        Map<RiskType, TradeProfile> map = new HashMap<>();
        map.put(Base, new TradeProfile(Base, null, 0.08, .40, .40, .80, new int[]{ 80, 75, 10 }));
        map.put(Vision, new TradeProfile(Vision, 333.00, null, .40, .40, .80,  new int[]{ 80, 75, 10 }));
        map.put(Lotto, new TradeProfile(Lotto, null, 0.04, .50, .50,  null, new int[]{ 75, 40 }));
        map.put(Hero, new TradeProfile(Hero, null, 0.04, .90, null, null, new int[]{ 60 }));
        return map;
    }
}
