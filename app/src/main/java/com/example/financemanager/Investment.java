package com.example.financemanager;

public class Investment {
    private String mutualFund;
    private String returnRate;
    private double amount;

    private String date;
    private String time;
    private String docId;

    public Investment(){

    }

    public Investment(String mutualFund, String returnRate, double amount,String date,String time,String docId) {
        this.mutualFund = mutualFund;
        this.returnRate = returnRate;
        this.amount = amount;
        this.date = date;
        this.time = time;
        this.docId=docId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getMutualFund() {
        return mutualFund;
    }

    public String getReturnRate() {
        return returnRate;
    }

    public double getAmount() {
        return amount;
    }

    public String getDocId() {
        return docId;
    }
}
