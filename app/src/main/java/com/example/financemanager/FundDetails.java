package com.example.financemanager;

public class FundDetails {
    private double current_price;
    private String name;
    private String symbol;
    private GrowthRates growth_rates;

    public double getCurrentPrice() {
        return current_price;
    }

    public void setCurrentPrice(double current_price) {
        this.current_price = current_price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public GrowthRates getGrowthRates() {
        return growth_rates;
    }

    public void setGrowthRates(GrowthRates growth_rates) {
        this.growth_rates = growth_rates;
    }

    public static class GrowthRates {
        private String _1_year;

        public String get1Year() {
            return _1_year;
        }

        public void set1Year(String _1_year) {
            this._1_year = _1_year;
        }
    }
}
