package com.stockpicker.model;

import java.time.LocalDate;

public class DragonTigerRecord {
    private String code;
    private String name;
    private double pctChange;
    private double netAmount;
    private double buyAmount;
    private double sellAmount;
    private double dealAmountRatio;
    private String reason;
    private LocalDate tradeDate;

    public DragonTigerRecord() {
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPctChange() { return pctChange; }
    public void setPctChange(double pctChange) { this.pctChange = pctChange; }
    public double getNetAmount() { return netAmount; }
    public void setNetAmount(double netAmount) { this.netAmount = netAmount; }
    public double getBuyAmount() { return buyAmount; }
    public void setBuyAmount(double buyAmount) { this.buyAmount = buyAmount; }
    public double getSellAmount() { return sellAmount; }
    public void setSellAmount(double sellAmount) { this.sellAmount = sellAmount; }
    public double getDealAmountRatio() { return dealAmountRatio; }
    public void setDealAmountRatio(double dealAmountRatio) { this.dealAmountRatio = dealAmountRatio; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }
}
