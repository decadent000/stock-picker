package com.stockpicker.service;

import com.stockpicker.model.AuctionSnapshot;
import com.stockpicker.model.DragonTigerRecord;
import com.stockpicker.model.LimitUpRecord;
import com.stockpicker.model.StockCandidate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YaoStockScorerTest {

    private final YaoStockScorer scorer = new YaoStockScorer();

    @Test
    void strongSignalsShouldProduceHighScore() {
        AuctionSnapshot a = auction(4.8, 8.2, 60000, 85000);
        DragonTigerRecord d = new DragonTigerRecord();
        d.setNetAmount(150000000D);
        LimitUpRecord l = new LimitUpRecord();
        l.setBoardCount(4);
        l.setSealFund(120000000D);
        l.setFloatMarketCap(5000000000D);

        StockCandidate result = scorer.score(a, d, l);

        assertThat(result.getYaoProbability()).isBetween(75, 95);
        assertThat(result.getProbabilityLevel()).isEqualTo("高");
    }

    @Test
    void extremeSellPressureShouldBePenalized() {
        AuctionSnapshot normal = auction(3.5, 7.0, 60000, 80000);
        AuctionSnapshot heavySell = auction(3.5, 7.0, 20000, 90000);

        int normalScore = scorer.score(normal, null, null).getYaoProbability();
        int heavyScore = scorer.score(heavySell, null, null).getYaoProbability();

        assertThat(heavyScore).isLessThan(normalScore);
    }

    private AuctionSnapshot auction(double turnover, double pct, long bid, long ask) {
        AuctionSnapshot a = new AuctionSnapshot();
        a.setCode("000001");
        a.setName("测试股");
        a.setTurnoverRate(turnover);
        a.setPctChange(pct);
        a.setBidVolume(bid);
        a.setAskVolume(ask);
        a.setExactAuctionSnapshot(true);
        return a;
    }
}
