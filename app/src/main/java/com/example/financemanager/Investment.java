package com.example.financemanager;

public class Investment {
    private String mutualFundName;
    private double returnRate;
    private double amount;

    public Investment(String mutualFundName, double returnRate, double amount) {
        this.mutualFundName = mutualFundName;
        this.returnRate = returnRate;
        this.amount = amount;
    }

    public String getMutualFundName() {
        return mutualFundName;
    }

    public double getReturnRate() {
        return returnRate;
    }

    public double getAmount() {
        return amount;
    }
}
