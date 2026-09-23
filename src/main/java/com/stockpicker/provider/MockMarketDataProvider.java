package com.stockpicker.provider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.stockpicker.model.AuctionSnapshot;
import com.stockpicker.model.DragonTigerRecord;
import com.stockpicker.model.LimitUpRecord;

@Service
@ConditionalOnProperty(name = "stock-picker.provider", havingValue = "mock")
public class MockMarketDataProvider implements MarketDataProvider {

    private static final ZoneId CHINA = ZoneId.of("Asia/Shanghai");

    @Override
    public String name() {
        return "mock";
    }

    @Override
    public List<AuctionSnapshot> fetchAuctionCandidates() {
        LocalDateTime now = LocalDateTime.now(CHINA);
        boolean exact = isAuctionWindow(now.toLocalTime());
        List<AuctionSnapshot> list = new ArrayList<AuctionSnapshot>();
        list.add(snapshot("000001", "示例一", 11.20, 7.60, 3.40, 72000, 95000, now, exact));
        list.add(snapshot("600001", "示例二", 8.82, 6.20, 2.65, 51000, 66000, now, exact));
        list.add(snapshot("002001", "示例三", 16.75, 9.70, 5.10, 43000, 91000, now, exact));
        list.add(snapshot("300001", "未通过示例", 21.30, 4.20, 1.80, 80000, 60000, now, exact));
        return list;
    }

    @Override
    public List<DragonTigerRecord> fetchDragonTiger(LocalDate tradeDate) {
        DragonTigerRecord a = new DragonTigerRecord();
        a.setCode("000001");
        a.setName("示例一");
        a.setPctChange(7.60);
        a.setBuyAmount(188000000D);
        a.setSellAmount(83000000D);
        a.setNetAmount(105000000D);
        a.setDealAmountRatio(18.5D);
        a.setReason("日涨幅偏离值达到披露条件");
        a.setTradeDate(tradeDate);

        DragonTigerRecord b = new DragonTigerRecord();
        b.setCode("002001");
        b.setName("示例三");
        b.setPctChange(9.70);
        b.setBuyAmount(235000000D);
        b.setSellAmount(102000000D);
        b.setNetAmount(133000000D);
        b.setDealAmountRatio(22.1D);
        b.setReason("连续三个交易日涨幅偏离值累计达到披露条件");
        b.setTradeDate(tradeDate);
        return Arrays.asList(a, b);
    }

    @Override
    public List<LimitUpRecord> fetchLimitUps(LocalDate tradeDate) {
        LimitUpRecord a = limit("000001", "示例一", 9.98, 8.3, 72000000D, 5600000000D, 2, 0, "软件服务", "09:31:00", "09:31:00", tradeDate);
        LimitUpRecord b = limit("002001", "示例三", 10.02, 12.2, 126000000D, 4200000000D, 4, 1, "机器人", "09:25:00", "10:03:00", tradeDate);
        return Arrays.asList(a, b);
    }

    private AuctionSnapshot snapshot(String code, String name, double price, double pct, double hs,
                                     long bid, long ask, LocalDateTime now, boolean exact) {
        AuctionSnapshot s = new AuctionSnapshot();
        s.setCode(code);
        s.setName(name);
        s.setPrice(price);
        s.setPctChange(pct);
        s.setTurnoverRate(hs);
        s.setBidVolume(bid);
        s.setAskVolume(ask);
        s.setCapturedAt(now);
        s.setExactAuctionSnapshot(exact);
        return s;
    }

    private LimitUpRecord limit(String code, String name, double pct, double hs, double fund, double ltsz,
                                int boards, int opens, String industry, String first, String last, LocalDate date) {
        LimitUpRecord r = new LimitUpRecord();
        r.setCode(code);
        r.setName(name);
        r.setPctChange(pct);
        r.setTurnoverRate(hs);
        r.setSealFund(fund);
        r.setFloatMarketCap(ltsz);
        r.setBoardCount(boards);
        r.setOpenBoardCount(opens);
        r.setIndustry(industry);
        r.setFirstLimitTime(first);
        r.setLastLimitTime(last);
        r.setTradeDate(date);
        return r;
    }

    private boolean isAuctionWindow(LocalTime time) {
        return !time.isBefore(LocalTime.of(9, 24, 30)) && !time.isAfter(LocalTime.of(9, 26, 0));
    }
}
