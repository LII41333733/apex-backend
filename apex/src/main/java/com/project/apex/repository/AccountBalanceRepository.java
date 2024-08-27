package com.project.apex.repository;

import com.project.apex.data.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {
}