package com.stockpicker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.stockpicker.config.StockPickerProperties;
import com.stockpicker.model.AuctionSnapshot;
import com.stockpicker.model.DashboardResponse;
import com.stockpicker.model.DragonTigerRecord;
import com.stockpicker.model.LimitUpRecord;
import com.stockpicker.model.StockCandidate;
import com.stockpicker.provider.MarketDataProvider;

import org.springframework.stereotype.Service;

@Service
public class StockPickerService {

    private final MarketDataProvider provider;
    private final StockPickerProperties properties;
    private final AuctionCaptureService auctionCaptureService;
    private final YaoStockScorer scorer;

    public StockPickerService(MarketDataProvider provider,
                              StockPickerProperties properties,
                              AuctionCaptureService auctionCaptureService,
                              YaoStockScorer scorer) {
        this.provider = provider;
        this.properties = properties;
        this.auctionCaptureService = auctionCaptureService;
        this.scorer = scorer;
    }

    public DashboardResponse dashboard(LocalDate date) {
        List<AuctionSnapshot> auctions = auctionCaptureService.snapshots(date);
        List<LimitUpRecord> limitUps = safeLimitUps(date);
        List<DragonTigerRecord> dragonTiger = latestDragonTiger(date);

        Map<String, LimitUpRecord> limitMap = new LinkedHashMap<String, LimitUpRecord>();
        for (LimitUpRecord item : limitUps) {
            limitMap.put(item.getCode(), item);
        }

        Map<String, DragonTigerRecord> dragonMap = aggregateDragonTiger(dragonTiger);
        List<StockCandidate> candidates = new ArrayList<StockCandidate>();

        for (AuctionSnapshot auction : auctions) {
            if (!passes(auction)) {
                continue;
            }
            candidates.add(scorer.score(auction, dragonMap.get(auction.getCode()), limitMap.get(auction.getCode())));
        }

        candidates.sort(new Comparator<StockCandidate>() {
            @Override
            public int compare(StockCandidate a, StockCandidate b) {
                return Integer.compare(b.getYaoProbability(), a.getYaoProbability());
            }
        });

        DashboardResponse response = new DashboardResponse();
        response.setTradeDate(date);
        response.setProvider(provider.name());
        response.setCandidates(candidates);
        response.setDragonTiger(dragonTiger);
        response.setLimitUps(limitUps);

        if (!auctions.isEmpty()) {
            response.setAuctionSnapshotTime(auctions.get(0).getCapturedAt());
            response.setExactAuctionSnapshot(auctions.get(0).isExactAuctionSnapshot());
        }
        if (!dragonTiger.isEmpty()) {
            response.setDragonTigerTradeDate(dragonTiger.get(0).getTradeDate());
        }
        return response;
    }

    public List<AuctionSnapshot> captureAuctionNow() {
        return auctionCaptureService.captureNow();
    }

    private boolean passes(AuctionSnapshot s) {
        return s.getTurnoverRate() > properties.getAuctionTurnoverRate()
                && s.getPctChange() > properties.getAuctionPctChange()
                && s.getAskVolume() > s.getBidVolume();
    }

    private List<LimitUpRecord> safeLimitUps(LocalDate date) {
        try {
            return provider.fetchLimitUps(date);
        } catch (RuntimeException e) {
            return new ArrayList<LimitUpRecord>();
        }
    }

    private List<DragonTigerRecord> latestDragonTiger(LocalDate date) {
        for (int i = 0; i <= 7; i++) {
            LocalDate candidate = date.minusDays(i);
            try {
                List<DragonTigerRecord> rows = provider.fetchDragonTiger(candidate);
                if (rows != null && !rows.isEmpty()) {
                    return rows;
                }
            } catch (RuntimeException ignored) {
                // 免费网页接口偶发限流时继续回退。
            }
        }
        return new ArrayList<DragonTigerRecord>();
    }

    private Map<String, DragonTigerRecord> aggregateDragonTiger(List<DragonTigerRecord> rows) {
        Map<String, DragonTigerRecord> map = new LinkedHashMap<String, DragonTigerRecord>();
        for (DragonTigerRecord row : rows) {
            DragonTigerRecord existing = map.get(row.getCode());
            if (existing == null) {
                map.put(row.getCode(), copy(row));
            } else {
                existing.setBuyAmount(existing.getBuyAmount() + row.getBuyAmount());
                existing.setSellAmount(existing.getSellAmount() + row.getSellAmount());
                existing.setNetAmount(existing.getNetAmount() + row.getNetAmount());
                existing.setDealAmountRatio(Math.max(existing.getDealAmountRatio(), row.getDealAmountRatio()));
                if (row.getReason() != null && row.getReason().length() > 0
                        && (existing.getReason() == null || !existing.getReason().contains(row.getReason()))) {
                    existing.setReason((existing.getReason() == null ? "" : existing.getReason() + "；") + row.getReason());
                }
            }
        }
        return map;
    }

    private DragonTigerRecord copy(DragonTigerRecord row) {
        DragonTigerRecord r = new DragonTigerRecord();
        r.setCode(row.getCode());
        r.setName(row.getName());
        r.setPctChange(row.getPctChange());
        r.setNetAmount(row.getNetAmount());
        r.setBuyAmount(row.getBuyAmount());
        r.setSellAmount(row.getSellAmount());
        r.setDealAmountRatio(row.getDealAmountRatio());
        r.setReason(row.getReason());
        r.setTradeDate(row.getTradeDate());
        return r;
    }
}
