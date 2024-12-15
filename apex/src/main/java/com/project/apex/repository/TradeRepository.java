package com.project.apex.repository;

import com.project.apex.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository<T extends Trade> extends JpaRepository<T, Long> {}

