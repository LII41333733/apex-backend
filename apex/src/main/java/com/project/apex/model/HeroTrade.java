package com.project.apex.model;

import com.project.apex.data.trades.TradeProfile;
import jakarta.persistence.*;

@Entity
@Table(name = "hero_trade")
public class HeroTrade extends Trade {


    public HeroTrade() {
        super();
    }

    public HeroTrade(TradeProfile tradeProfile) {
        super(tradeProfile);
    }

}