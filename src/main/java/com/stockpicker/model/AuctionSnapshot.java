package com.stockpicker.model;

import java.time.LocalDateTime;

public class AuctionSnapshot {
    private String code;
    private String name;
    private double price;
    private double pctChange;
    private double turnoverRate;
    private long bidVolume;
    private long askVolume;
    private LocalDateTime capturedAt;
    private boolean exactAuctionSnapshot;

    public AuctionSnapshot() {
    }

    public double askBidRatio() {
        return bidVolume <= 0L ? (askVolume > 0L ? 999D : 0D) : (double) askVolume / (double) bidVolume;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getPctChange() { return pctChange; }
    public void setPctChange(double pctChange) { this.pctChange = pctChange; }
    public double getTurnoverRate() { return turnoverRate; }
    public void setTurnoverRate(double turnoverRate) { this.turnoverRate = turnoverRate; }
    public long getBidVolume() { return bidVolume; }
    public void setBidVolume(long bidVolume) { this.bidVolume = bidVolume; }
    public long getAskVolume() { return askVolume; }
    public void setAskVolume(long askVolume) { this.askVolume = askVolume; }
    public LocalDateTime getCapturedAt() { return capturedAt; }
    public void setCapturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; }
    public boolean isExactAuctionSnapshot() { return exactAuctionSnapshot; }
    public void setExactAuctionSnapshot(boolean exactAuctionSnapshot) { this.exactAuctionSnapshot = exactAuctionSnapshot; }
}
