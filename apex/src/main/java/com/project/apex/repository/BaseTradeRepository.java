package com.project.apex.repository;

import com.project.apex.model.BaseTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseTradeRepository extends JpaRepository<BaseTrade, Long> {}