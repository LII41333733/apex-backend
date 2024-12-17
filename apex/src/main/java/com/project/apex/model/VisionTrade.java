package com.project.apex.model;

import com.project.apex.data.trades.Trim2Tradeable;
import jakarta.persistence.*;
import static com.project.apex.data.trades.RiskType.VISION;

@Entity
@Table(name = "vision_trade")
public class VisionTrade extends Trim2Tradeable {

    public VisionTrade() {
        super(VISION, 0, .30, .30, .60,  new int[]{ 80, 75, 10 });
    }

}