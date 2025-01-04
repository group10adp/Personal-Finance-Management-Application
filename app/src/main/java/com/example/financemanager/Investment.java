package com.example.financemanager;

public class Investment {
    private String mutualFundName;
    private String returnRate;
    private String amount;

    private String date;
    private String time;

    public Investment(String mutualFundName, String returnRate, String amount,String date,String time) {
        this.mutualFundName = mutualFundName;
        this.returnRate = returnRate;
        this.amount = amount;
        this.date = date;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getMutualFundName() {
        return mutualFundName;
    }

    public String getReturnRate() {
        return returnRate;
    }

    public String getAmount() {
        return amount;
    }
}
