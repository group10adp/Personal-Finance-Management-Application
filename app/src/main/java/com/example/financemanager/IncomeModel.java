package com.example.financemanager;

public class IncomeModel {

    private String price;
    private String category;
    private String date;
    private String time;

    public IncomeModel(String price, String category, String date, String time) {
        this.price = price;
        this.category = category;
        this.date = date;
        this.time = time;
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
}
