package com.project.apex.model;

import jakarta.persistence.*;
import static com.project.apex.data.trades.RiskType.HERO;

@Entity
@Table(name = "hero_trade")
public class HeroTrade extends Trade {

    public HeroTrade() {
        super(HERO, 0.04, .90, new int[]{ 60 });
    }

}