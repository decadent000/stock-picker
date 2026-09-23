package com.stockpicker.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.stockpicker.model.AuctionSnapshot;
import com.stockpicker.provider.MarketDataProvider;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AuctionCaptureService {

    private static final ZoneId CHINA = ZoneId.of("Asia/Shanghai");

    private final MarketDataProvider provider;
    private final AuctionSnapshotStore store;

    public AuctionCaptureService(MarketDataProvider provider, AuctionSnapshotStore store) {
        this.provider = provider;
        this.store = store;
    }

    @Scheduled(cron = "${stock-picker.auction-capture-cron:5 25 9 * * MON-FRI}", zone = "Asia/Shanghai")
    public void scheduledCapture() {
        captureNow();
    }

    public List<AuctionSnapshot> captureNow() {
        List<AuctionSnapshot> snapshots = provider.fetchAuctionCandidates();
        store.save(LocalDate.now(CHINA), snapshots);
        return snapshots;
    }

    public List<AuctionSnapshot> snapshots(LocalDate date) {
        List<AuctionSnapshot> saved = store.load(date);
        if (!saved.isEmpty()) {
            return saved;
        }
        if (LocalDate.now(CHINA).equals(date)) {
            return provider.fetchAuctionCandidates();
        }
        return saved;
    }
}
