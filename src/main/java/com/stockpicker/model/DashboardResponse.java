package com.stockpicker.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardResponse {
    private LocalDate tradeDate;
    private String provider;
    private LocalDateTime auctionSnapshotTime;
    private boolean exactAuctionSnapshot;
    private LocalDate dragonTigerTradeDate;
    private List<StockCandidate> candidates = new ArrayList<StockCandidate>();
    private List<DragonTigerRecord> dragonTiger = new ArrayList<DragonTigerRecord>();
    private List<LimitUpRecord> limitUps = new ArrayList<LimitUpRecord>();

    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public LocalDateTime getAuctionSnapshotTime() { return auctionSnapshotTime; }
    public void setAuctionSnapshotTime(LocalDateTime auctionSnapshotTime) { this.auctionSnapshotTime = auctionSnapshotTime; }
    public boolean isExactAuctionSnapshot() { return exactAuctionSnapshot; }
    public void setExactAuctionSnapshot(boolean exactAuctionSnapshot) { this.exactAuctionSnapshot = exactAuctionSnapshot; }
    public LocalDate getDragonTigerTradeDate() { return dragonTigerTradeDate; }
    public void setDragonTigerTradeDate(LocalDate dragonTigerTradeDate) { this.dragonTigerTradeDate = dragonTigerTradeDate; }
    public List<StockCandidate> getCandidates() { return candidates; }
    public void setCandidates(List<StockCandidate> candidates) { this.candidates = candidates; }
    public List<DragonTigerRecord> getDragonTiger() { return dragonTiger; }
    public void setDragonTiger(List<DragonTigerRecord> dragonTiger) { this.dragonTiger = dragonTiger; }
    public List<LimitUpRecord> getLimitUps() { return limitUps; }
    public void setLimitUps(List<LimitUpRecord> limitUps) { this.limitUps = limitUps; }
}
