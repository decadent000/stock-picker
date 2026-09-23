package com.stockpicker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stock-picker")
public class StockPickerProperties {

    private String provider = "eastmoney";
    private double auctionTurnoverRate = 2.0D;
    private double auctionPctChange = 5.0D;
    private String dataDir = "./data";
    private String auctionCaptureCron = "5 25 9 * * MON-FRI";

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public double getAuctionTurnoverRate() {
        return auctionTurnoverRate;
    }

    public void setAuctionTurnoverRate(double auctionTurnoverRate) {
        this.auctionTurnoverRate = auctionTurnoverRate;
    }

    public double getAuctionPctChange() {
        return auctionPctChange;
    }

    public void setAuctionPctChange(double auctionPctChange) {
        this.auctionPctChange = auctionPctChange;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getAuctionCaptureCron() {
        return auctionCaptureCron;
    }

    public void setAuctionCaptureCron(String auctionCaptureCron) {
        this.auctionCaptureCron = auctionCaptureCron;
    }
}
