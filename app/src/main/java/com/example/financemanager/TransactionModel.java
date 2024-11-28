package com.example.financemanager;

public class TransactionModel {

    private String price;
    private String category;
    private String date;
    private String time;
    private String type;

    public TransactionModel(String price, String category, String date, String time,String type) {
        this.price = price;
        this.category = category;
        this.date = date;
        this.time = time;
        this.type = type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
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

    @Override
    public String toString() {
        return "IncomeModel{" +
                "price='" + price + '\'' +
                ", category='" + category + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

}
