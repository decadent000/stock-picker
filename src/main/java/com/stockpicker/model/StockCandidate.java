package com.stockpicker.model;

import java.util.ArrayList;
import java.util.List;

public class StockCandidate {
    private String code;
    private String name;
    private double auctionTurnoverRate;
    private double auctionPctChange;
    private long auctionBidVolume;
    private long auctionAskVolume;
    private double askBidRatio;
    private int boardCount;
    private double dragonTigerNetAmount;
    private int yaoProbability;
    private String probabilityLevel;
    private boolean exactAuctionSnapshot;
    private List<String> reasons = new ArrayList<String>();

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getAuctionTurnoverRate() { return auctionTurnoverRate; }
    public void setAuctionTurnoverRate(double auctionTurnoverRate) { this.auctionTurnoverRate = auctionTurnoverRate; }
    public double getAuctionPctChange() { return auctionPctChange; }
    public void setAuctionPctChange(double auctionPctChange) { this.auctionPctChange = auctionPctChange; }
    public long getAuctionBidVolume() { return auctionBidVolume; }
    public void setAuctionBidVolume(long auctionBidVolume) { this.auctionBidVolume = auctionBidVolume; }
    public long getAuctionAskVolume() { return auctionAskVolume; }
    public void setAuctionAskVolume(long auctionAskVolume) { this.auctionAskVolume = auctionAskVolume; }
    public double getAskBidRatio() { return askBidRatio; }
    public void setAskBidRatio(double askBidRatio) { this.askBidRatio = askBidRatio; }
    public int getBoardCount() { return boardCount; }
    public void setBoardCount(int boardCount) { this.boardCount = boardCount; }
    public double getDragonTigerNetAmount() { return dragonTigerNetAmount; }
    public void setDragonTigerNetAmount(double dragonTigerNetAmount) { this.dragonTigerNetAmount = dragonTigerNetAmount; }
    public int getYaoProbability() { return yaoProbability; }
    public void setYaoProbability(int yaoProbability) { this.yaoProbability = yaoProbability; }
    public String getProbabilityLevel() { return probabilityLevel; }
    public void setProbabilityLevel(String probabilityLevel) { this.probabilityLevel = probabilityLevel; }
    public boolean isExactAuctionSnapshot() { return exactAuctionSnapshot; }
    public void setExactAuctionSnapshot(boolean exactAuctionSnapshot) { this.exactAuctionSnapshot = exactAuctionSnapshot; }
    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }
}
