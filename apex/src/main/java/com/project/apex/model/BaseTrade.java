package com.project.apex.model;

import com.project.apex.data.trades.TradeProfile;
import com.project.apex.data.trades.Trim2Tradeable;
import jakarta.persistence.*;

@Entity
@Table(name = "base_trade")
public class BaseTrade extends Trim2Tradeable {

    public BaseTrade() {
        super();
    }

    public BaseTrade(TradeProfile tradeProfile) {
        super(tradeProfile);
    }


}