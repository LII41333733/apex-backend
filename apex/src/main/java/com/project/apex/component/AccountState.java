package com.project.apex.component;

import com.project.apex.data.account.Balance;
import org.springframework.stereotype.Component;

@Component
public class AccountState {

    private Balance balance;

    public Balance getBalanceData() {
        return balance;
    }

    public void setBalanceData(Balance balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "AccountState{" +
                "balance=" + balance +
                '}';
    }
}
