package com.stockpicker.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.stockpicker.config.StockPickerProperties;
import com.stockpicker.model.AuctionSnapshot;
import com.stockpicker.model.DashboardResponse;
import com.stockpicker.service.StockPickerService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockPickerController {

    private static final ZoneId CHINA = ZoneId.of("Asia/Shanghai");

    private final StockPickerService stockPickerService;
    private final StockPickerProperties properties;

    public StockPickerController(StockPickerService stockPickerService, StockPickerProperties properties) {
        this.stockPickerService = stockPickerService;
        this.properties = properties;
    }

    @GetMapping("/api/dashboard")
    public DashboardResponse dashboard(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return stockPickerService.dashboard(date == null ? LocalDate.now(CHINA) : date);
    }

    @PostMapping("/api/auction/capture")
    public Map<String, Object> capture() {
        List<AuctionSnapshot> rows = stockPickerService.captureAuctionNow();
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("count", rows.size());
        result.put("capturedAt", rows.isEmpty() ? null : rows.get(0).getCapturedAt());
        result.put("exactAuctionSnapshot", !rows.isEmpty() && rows.get(0).isExactAuctionSnapshot());
        return result;
    }

    @GetMapping("/api/rules")
    public Map<String, Object> rules() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("auctionTurnoverRateGreaterThan", properties.getAuctionTurnoverRate());
        result.put("auctionPctChangeGreaterThan", properties.getAuctionPctChange());
        result.put("auctionAskGreaterThanBid", true);
        result.put("probabilityNote", "妖股概率为启发式评分，不是经校准的真实上涨概率，也不构成投资建议。");
        return result;
    }
}
