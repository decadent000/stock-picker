package com.stockpicker.service;

import java.util.ArrayList;
import java.util.List;

import com.stockpicker.model.AuctionSnapshot;
import com.stockpicker.model.DragonTigerRecord;
import com.stockpicker.model.LimitUpRecord;
import com.stockpicker.model.StockCandidate;

import org.springframework.stereotype.Component;

@Component
public class YaoStockScorer {

    public StockCandidate score(AuctionSnapshot auction, DragonTigerRecord dragonTiger, LimitUpRecord limitUp) {
        int score = 30;
        List<String> reasons = new ArrayList<String>();

        double turnoverExtra = Math.max(0D, auction.getTurnoverRate() - 2D);
        int turnoverPoints = (int) Math.min(15D, 5D + turnoverExtra * 3D);
        score += turnoverPoints;
        reasons.add(String.format("竞价换手 %.2f%%", auction.getTurnoverRate()));

        double pctExtra = Math.max(0D, auction.getPctChange() - 5D);
        int pctPoints = (int) Math.min(15D, 5D + pctExtra * 2.5D);
        score += pctPoints;
        reasons.add(String.format("09:25 涨幅 %.2f%%", auction.getPctChange()));

        double ratio = auction.askBidRatio();
        if (ratio >= 1D && ratio <= 1.8D) {
            score += 5;
            reasons.add(String.format("委卖/委买 %.2f，满足过滤且未极端失衡", ratio));
        } else if (ratio > 3D) {
            score -= 8;
            reasons.add(String.format("委卖/委买 %.2f，卖压偏高", ratio));
        } else {
            reasons.add(String.format("委卖/委买 %.2f", ratio));
        }

        int boards = limitUp == null ? 0 : limitUp.getBoardCount();
        if (boards > 0) {
            int boardPoints = Math.min(20, boards * 5);
            score += boardPoints;
            reasons.add(boards + " 连板");
        }

        if (dragonTiger != null) {
            double net = dragonTiger.getNetAmount();
            if (net > 0D) {
                int flowPoints = Math.min(15, 5 + (int) Math.floor(net / 50000000D) * 2);
                score += flowPoints;
                reasons.add(String.format("龙虎榜净流入 %.2f 亿", net / 100000000D));
            } else if (net < 0D) {
                score -= 8;
                reasons.add(String.format("龙虎榜净流出 %.2f 亿", -net / 100000000D));
            }
        }

        if (limitUp != null && limitUp.getFloatMarketCap() > 0D && limitUp.getSealFund() > 0D) {
            double sealRatio = limitUp.getSealFund() / limitUp.getFloatMarketCap() * 100D;
            int sealPoints = (int) Math.min(10D, sealRatio * 5D);
            score += sealPoints;
            reasons.add(String.format("封单/流通市值 %.2f%%", sealRatio));
            if (limitUp.getOpenBoardCount() >= 3) {
                score -= 5;
                reasons.add("炸板次数偏多");
            }
        }

        score = Math.max(0, Math.min(95, score));

        StockCandidate c = new StockCandidate();
        c.setCode(auction.getCode());
        c.setName(auction.getName());
        c.setAuctionTurnoverRate(auction.getTurnoverRate());
        c.setAuctionPctChange(auction.getPctChange());
        c.setAuctionBidVolume(auction.getBidVolume());
        c.setAuctionAskVolume(auction.getAskVolume());
        c.setAskBidRatio(ratio);
        c.setBoardCount(boards);
        c.setDragonTigerNetAmount(dragonTiger == null ? 0D : dragonTiger.getNetAmount());
        c.setYaoProbability(score);
        c.setProbabilityLevel(score >= 75 ? "高" : (score >= 55 ? "中" : "低"));
        c.setExactAuctionSnapshot(auction.isExactAuctionSnapshot());
        c.setReasons(reasons);
        return c;
    }
}
