package com.stockpicker.provider;

import java.time.LocalDate;
import java.util.List;

import com.stockpicker.model.AuctionSnapshot;
import com.stockpicker.model.DragonTigerRecord;
import com.stockpicker.model.LimitUpRecord;

public interface MarketDataProvider {
    String name();

    List<AuctionSnapshot> fetchAuctionCandidates();

    List<DragonTigerRecord> fetchDragonTiger(LocalDate tradeDate);

    List<LimitUpRecord> fetchLimitUps(LocalDate tradeDate);
}
