package com.example.financemanager;

public class LegendItem {
    private final String label;
    private final int color;

    private final String amount;

    public LegendItem(String label, int color, String amount) {
        this.label = label;
        this.color = color;
        this.amount = amount;
    }

    public String getAmount() {
        return amount;
    }

    public String getLabel() {
        return label;
    }

    public int getColor() {
        return color;
    }
}

