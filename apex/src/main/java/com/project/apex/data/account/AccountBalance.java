package com.project.apex.data.account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AccountBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double totalBalance;
    private double buyingPower;
    private double unsettledFunds;
    private int buyLimit;
    private int currentLosses;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public double getBuyingPower() {
        return buyingPower;
    }

    public void setBuyingPower(double buyingPower) {
        this.buyingPower = buyingPower;
    }

    public double getUnsettledFunds() {
        return unsettledFunds;
    }

    public void setUnsettledFunds(double unsettledFunds) {
        this.unsettledFunds = unsettledFunds;
    }

    public int getBuyLimit() {
        return buyLimit;
    }

    public void setBuyLimit(int buyLimit) {
        this.buyLimit = buyLimit;
    }

    public int getCurrentLosses() {
        return currentLosses;
    }

    public void setCurrentLosses(int currentLosses) {
        this.currentLosses = currentLosses;
    }
}