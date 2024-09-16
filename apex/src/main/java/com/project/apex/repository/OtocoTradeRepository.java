//package com.project.apex.repository;
//
//import com.project.apex.model.OtocoTrade;
//import com.project.apex.model.Trade;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
////@Repository
//public interface OtocoTradeRepository extends JpaRepository<OtocoTrade, Integer> {
//    @Query("SELECT t FROM OtocoTrade t WHERE t.pl < 0 AND t.lossId IS NULL ORDER BY t.openDate DESC LIMIT 1")
//    Optional<OtocoTrade> findLastLossTradeWithoutLossId();
//
//    @Query("SELECT t FROM OtocoTrade t WHERE t.lossId = :recoveryId")
//    OtocoTrade findTradeByLossId(@Param("recoveryId") int recoveryId);
//
//    @Query("SELECT t FROM OtocoTrade t WHERE t.isFinalized = true ORDER BY t.closeDate DESC")
//    List<OtocoTrade> findLastFinalizedTrade();
//}