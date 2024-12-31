package com.project.apex.model;

import com.project.apex.data.trades.TradeProfile;
import com.project.apex.data.trades.Trim1Tradeable;
import jakarta.persistence.*;

@Entity
@Table(name = "lotto_trade")
public class LottoTrade extends Trim1Tradeable {

    public LottoTrade(TradeProfile tradeProfile) {
        super(tradeProfile);
    }

    public LottoTrade() {
        super();
    }

}