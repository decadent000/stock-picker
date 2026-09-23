package com.stockpicker.model;

import java.time.LocalDate;

public class LimitUpRecord {
    private String code;
    private String name;
    private double pctChange;
    private double turnoverRate;
    private double sealFund;
    private double floatMarketCap;
    private int boardCount;
    private int openBoardCount;
    private String industry;
    private String firstLimitTime;
    private String lastLimitTime;
    private LocalDate tradeDate;

    public LimitUpRecord() {
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPctChange() { return pctChange; }
    public void setPctChange(double pctChange) { this.pctChange = pctChange; }
    public double getTurnoverRate() { return turnoverRate; }
    public void setTurnoverRate(double turnoverRate) { this.turnoverRate = turnoverRate; }
    public double getSealFund() { return sealFund; }
    public void setSealFund(double sealFund) { this.sealFund = sealFund; }
    public double getFloatMarketCap() { return floatMarketCap; }
    public void setFloatMarketCap(double floatMarketCap) { this.floatMarketCap = floatMarketCap; }
    public int getBoardCount() { return boardCount; }
    public void setBoardCount(int boardCount) { this.boardCount = boardCount; }
    public int getOpenBoardCount() { return openBoardCount; }
    public void setOpenBoardCount(int openBoardCount) { this.openBoardCount = openBoardCount; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getFirstLimitTime() { return firstLimitTime; }
    public void setFirstLimitTime(String firstLimitTime) { this.firstLimitTime = firstLimitTime; }
    public String getLastLimitTime() { return lastLimitTime; }
    public void setLastLimitTime(String lastLimitTime) { this.lastLimitTime = lastLimitTime; }
    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }
}
