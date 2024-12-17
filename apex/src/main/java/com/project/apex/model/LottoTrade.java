package com.project.apex.model;

import com.project.apex.data.trades.Trim1Tradeable;
import jakarta.persistence.*;
import static com.project.apex.data.trades.RiskType.LOTTO;

@Entity
@Table(name = "lotto_trade")
public class LottoTrade extends Trim1Tradeable {

    public LottoTrade() {
        super(LOTTO, 0.04, .50, .50,  new int[]{ 75, 40 });
    }

}