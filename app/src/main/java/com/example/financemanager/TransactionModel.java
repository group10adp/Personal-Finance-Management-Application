package com.example.financemanager;

public class TransactionModel {

    private double amount;
    private String category;
    private String date;
    private String time;
    private String type;
    private String note;
    private String docId;
    private String paymentMode;

    public TransactionModel() {
    }

    public TransactionModel(double amount, String category, String date, String time,String type,String note,String docId,String paymentMode) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.time = time;
        this.type = type;
        this.note=note;
        this.docId=docId;
        this.paymentMode=paymentMode;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    @Override
    public String toString() {
        return "IncomeModel{" +
                "price='" + amount + '\'' +
                ", category='" + category + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

}
