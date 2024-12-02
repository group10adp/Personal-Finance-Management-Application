package com.example.financemanager;

// Class for storing budget entry
public class BudgetEntry {
    private double amount;

    public BudgetEntry(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}

// Class for storing remaining budget entry
