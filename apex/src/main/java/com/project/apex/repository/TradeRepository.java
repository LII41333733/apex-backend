package com.project.apex.repository;

import com.project.apex.model.Trade;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Integer> {
    @Query("SELECT t FROM Trade t WHERE t.tradeResult = 'L' AND t.lossId IS NULL ORDER BY t.openDate DESC LIMIT 1")
    Optional<Trade> findLastLossTradeWithoutLossId();

    @Query("SELECT t FROM Trade t WHERE t.lossId = :recoveryId")
    Trade findTradeByLossId(@Param("recoveryId") int recoveryId);

    @Query("SELECT t FROM Trade t WHERE t.isFinalized = true ORDER BY t.closeDate DESC")
    List<Trade> findLastFinalizedTrade();
}