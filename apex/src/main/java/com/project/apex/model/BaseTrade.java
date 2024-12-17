package com.project.apex.model;

import com.project.apex.data.trades.Trim2Tradeable;
import jakarta.persistence.*;
import static com.project.apex.data.trades.RiskType.BASE;

@Entity
@Table(name = "base_trade")
public class BaseTrade extends Trim2Tradeable {

    public BaseTrade() {
        super(BASE, 0.08, .30, .30, .60, new int[]{ 80, 75, 10 });
    }

}