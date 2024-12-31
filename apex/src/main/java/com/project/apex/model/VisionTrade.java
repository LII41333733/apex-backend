package com.project.apex.model;

import com.project.apex.data.trades.TradeProfile;
import com.project.apex.data.trades.Trim2Tradeable;
import jakarta.persistence.*;

@Entity
@Table(name = "vision_trade")
public class VisionTrade extends Trim2Tradeable {

    public VisionTrade(TradeProfile tradeProfile) {
        super(tradeProfile);
    }

    public VisionTrade() {
        super();
    }
}