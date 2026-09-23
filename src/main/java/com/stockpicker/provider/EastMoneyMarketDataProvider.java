package com.stockpicker.provider;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.stockpicker.config.StockPickerProperties;
import com.stockpicker.model.AuctionSnapshot;
import com.stockpicker.model.DragonTigerRecord;
import com.stockpicker.model.LimitUpRecord;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@ConditionalOnProperty(name = "stock-picker.provider", havingValue = "eastmoney", matchIfMissing = true)
public class EastMoneyMarketDataProvider implements MarketDataProvider {

    private static final ZoneId CHINA = ZoneId.of("Asia/Shanghai");
    private static final String QUOTE_LIST = "https://push2.eastmoney.com/api/qt/clist/get";
    private static final String QUOTE_DETAIL = "https://push2.eastmoney.com/api/qt/stock/get";
    private static final String LIMIT_UP = "https://push2ex.eastmoney.com/getTopicZTPool";
    private static final String DATA_CENTER = "https://datacenter-web.eastmoney.com/api/data/v1/get";
    private static final String UT = "fa5fd1943c7b386f172d6893dbfba10b";

    // 沪深主板 + 创业板 + 科创板。北交所可后续按数据源口径补充。
    private static final String A_SHARE_FS = "m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23";

    // 五档盘口字段需要较完整字段集，避免部分节点对“窄 fields”返回空盘口。
    private static final String ORDER_BOOK_FIELDS =
            "f120,f121,f122,f174,f175,f59,f163,f43,f57,f58,f169,f170,f46,f44,f51," +
            "f168,f47,f164,f116,f60,f45,f52,f50,f48,f167,f117,f71,f161,f49,f530," +
            "f135,f136,f137,f138,f139,f141,f142,f144,f145,f147,f148,f140,f143,f146," +
            "f149,f55,f62,f162,f92,f173,f104,f105,f84,f85,f183,f184,f185,f186,f187," +
            "f188,f189,f190,f191,f192,f107,f111,f86,f177,f78,f110,f262,f263,f264,f267," +
            "f268,f255,f256,f257,f258,f127,f199,f128,f198,f259,f260,f261,f171,f277,f278," +
            "f279,f288,f152,f250,f251,f252,f253,f254,f269,f270,f271,f272,f273,f274,f275," +
            "f276,f265,f266,f289,f290,f286,f285,f292,f293,f294,f295," +
            "f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f31,f32,f33,f34,f35,f36,f37,f38,f39,f40";

    private final RestTemplate restTemplate;
    private final StockPickerProperties properties;

    public EastMoneyMarketDataProvider(RestTemplateBuilder builder, StockPickerProperties properties) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(6))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();
        this.properties = properties;
    }

    @Override
    public String name() {
        return "eastmoney";
    }

    @Override
    public List<AuctionSnapshot> fetchAuctionCandidates() {
        URI uri = UriComponentsBuilder.fromHttpUrl(QUOTE_LIST)
                .queryParam("pn", 1)
                .queryParam("pz", 6000)
                .queryParam("po", 1)
                .queryParam("np", 1)
                .queryParam("fltt", 2)
                .queryParam("invt", 2)
                .queryParam("fid", "f3")
                .queryParam("fs", A_SHARE_FS)
                .queryParam("fields", "f2,f3,f8,f12,f14")
                .build().encode().toUri();

        JsonNode root = getJson(uri);
        JsonNode diff = root.path("data").path("diff");
        if (!diff.isArray()) {
            return Collections.emptyList();
        }

        LocalDateTime capturedAt = LocalDateTime.now(CHINA);
        boolean exact = isAuctionWindow(capturedAt.toLocalTime());
        List<AuctionSnapshot> result = new ArrayList<AuctionSnapshot>();

        for (JsonNode row : diff) {
            double pct = number(row, "f3");
            double turnover = number(row, "f8");
            if (pct <= properties.getAuctionPctChange() || turnover <= properties.getAuctionTurnoverRate()) {
                continue;
            }

            String code = text(row, "f12");
            if (code.isEmpty()) {
                continue;
            }

            AuctionSnapshot snapshot = new AuctionSnapshot();
            snapshot.setCode(code);
            snapshot.setName(text(row, "f14"));
            snapshot.setPrice(number(row, "f2"));
            snapshot.setPctChange(pct);
            snapshot.setTurnoverRate(turnover);
            snapshot.setCapturedAt(capturedAt);
            snapshot.setExactAuctionSnapshot(exact);
            enrichOrderBook(snapshot);
            result.add(snapshot);
        }
        return result;
    }

    @Override
    public List<DragonTigerRecord> fetchDragonTiger(LocalDate tradeDate) {
        String columns = "SECURITY_CODE,SECURITY_NAME_ABBR,CHANGE_RATE,BILLBOARD_NET_AMT," +
                "BILLBOARD_BUY_AMT,BILLBOARD_SELL_AMT,DEAL_AMOUNT_RATIO,EXPLANATION,TRADE_DATE";

        URI uri = UriComponentsBuilder.fromHttpUrl(DATA_CENTER)
                .queryParam("reportName", "RPT_DAILYBILLBOARD_DETAILSNEW")
                .queryParam("columns", columns)
                .queryParam("pageNumber", 1)
                .queryParam("pageSize", 500)
                .queryParam("sortColumns", "BILLBOARD_NET_AMT")
                .queryParam("sortTypes", -1)
                .queryParam("source", "WEB")
                .queryParam("client", "WEB")
                .queryParam("filter", "(TRADE_DATE='" + tradeDate + "')")
                .build().encode().toUri();

        JsonNode data = getJson(uri).path("result").path("data");
        if (!data.isArray()) {
            return Collections.emptyList();
        }

        List<DragonTigerRecord> result = new ArrayList<DragonTigerRecord>();
        for (JsonNode row : data) {
            DragonTigerRecord r = new DragonTigerRecord();
            r.setCode(text(row, "SECURITY_CODE"));
            r.setName(text(row, "SECURITY_NAME_ABBR"));
            r.setPctChange(number(row, "CHANGE_RATE"));
            r.setNetAmount(number(row, "BILLBOARD_NET_AMT"));
            r.setBuyAmount(number(row, "BILLBOARD_BUY_AMT"));
            r.setSellAmount(number(row, "BILLBOARD_SELL_AMT"));
            r.setDealAmountRatio(number(row, "DEAL_AMOUNT_RATIO"));
            r.setReason(text(row, "EXPLANATION"));
            r.setTradeDate(tradeDate);
            result.add(r);
        }
        return result;
    }

    @Override
    public List<LimitUpRecord> fetchLimitUps(LocalDate tradeDate) {
        URI uri = UriComponentsBuilder.fromHttpUrl(LIMIT_UP)
                .queryParam("ut", "7eea3edcaed734bea9cbfc24409ed989")
                .queryParam("dpt", "wz.ztzt")
                .queryParam("Pageindex", 0)
                .queryParam("pagesize", 1000)
                .queryParam("sort", "fbt:asc")
                .queryParam("date", tradeDate.format(DateTimeFormatter.BASIC_ISO_DATE))
                .build().encode().toUri();

        JsonNode pool = getJson(uri).path("data").path("pool");
        if (!pool.isArray()) {
            return Collections.emptyList();
        }

        List<LimitUpRecord> result = new ArrayList<LimitUpRecord>();
        for (JsonNode row : pool) {
            LimitUpRecord r = new LimitUpRecord();
            r.setCode(text(row, "c"));
            r.setName(text(row, "n"));
            r.setPctChange(number(row, "zdp"));
            r.setTurnoverRate(number(row, "hs"));
            r.setSealFund(number(row, "fund"));
            r.setFloatMarketCap(number(row, "ltsz"));
            r.setBoardCount(integer(row, "lbc"));
            r.setOpenBoardCount(integer(row, "zbc"));
            r.setIndustry(text(row, "hybk"));
            r.setFirstLimitTime(formatTime(text(row, "fbt")));
            r.setLastLimitTime(formatTime(text(row, "lbt")));
            r.setTradeDate(tradeDate);
            result.add(r);
        }
        return result;
    }

    private void enrichOrderBook(AuctionSnapshot snapshot) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(QUOTE_DETAIL)
                    .queryParam("secid", secId(snapshot.getCode()))
                    .queryParam("fltt", 2)
                    .queryParam("invt", 2)
                    .queryParam("ut", UT)
                    .queryParam("fields", ORDER_BOOK_FIELDS)
                    .build().encode().toUri();

            JsonNode data = getJson(uri).path("data");
            long bid = longNumber(data, "f20") + longNumber(data, "f18") + longNumber(data, "f16")
                    + longNumber(data, "f14") + longNumber(data, "f12");
            long ask = longNumber(data, "f40") + longNumber(data, "f38") + longNumber(data, "f36")
                    + longNumber(data, "f34") + longNumber(data, "f32");
            snapshot.setBidVolume(bid);
            snapshot.setAskVolume(ask);
        } catch (RuntimeException ex) {
            snapshot.setBidVolume(0L);
            snapshot.setAskVolume(0L);
        }
    }

    private JsonNode getJson(URI uri) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        headers.set("Referer", "http://www.eastmoney.com/");
        HttpEntity<Void> entity = new HttpEntity<Void>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, entity, JsonNode.class);
        JsonNode body = response.getBody();
        return body == null ? com.fasterxml.jackson.databind.node.NullNode.getInstance() : body;
    }

    private String secId(String code) {
        return (code.startsWith("6") ? "1." : "0.") + code;
    }

    private boolean isAuctionWindow(LocalTime time) {
        return !time.isBefore(LocalTime.of(9, 24, 30)) && !time.isAfter(LocalTime.of(9, 26, 0));
    }

    private String formatTime(String raw) {
        if (raw == null || raw.length() == 0 || "-".equals(raw) || "0".equals(raw)) {
            return "--";
        }
        String value = raw;
        while (value.length() < 6) {
            value = "0" + value;
        }
        if (value.length() >= 6) {
            return value.substring(0, 2) + ":" + value.substring(2, 4) + ":" + value.substring(4, 6);
        }
        return raw;
    }

    private String text(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || n.isNull()) {
            return "";
        }
        String value = n.asText("");
        return "-".equals(value) ? "" : value;
    }

    private double number(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || n.isNull()) {
            return 0D;
        }
        if (n.isNumber()) {
            return n.asDouble();
        }
        try {
            String value = n.asText();
            return value.length() == 0 || "-".equals(value) ? 0D : Double.parseDouble(value);
        } catch (Exception e) {
            return 0D;
        }
    }

    private long longNumber(JsonNode node, String field) {
        return Math.round(number(node, field));
    }

    private int integer(JsonNode node, String field) {
        return (int) Math.round(number(node, field));
    }
}
