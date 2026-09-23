# stock-picker

基于 **Java 8 + Spring Boot 2.7.18** 的 A 股竞价选股工具，聚合：

- 09:25 集合竞价快照
- 龙虎榜资金流向
- 涨停 / 连板数据
- 自定义硬筛选条件
- 可解释的“妖股概率”启发式评分
- 简单 Web 看板
- Mock 数据模式

> “妖股概率”是规则型评分，不是经过历史样本校准的真实概率，也不代表未来收益，不构成投资建议。

## 1. 当前筛选规则

股票必须同时满足：

1. 竞价阶段换手率 **> 2%**
2. 09:25 涨跌幅 **> 5%**
3. 集合竞价五档 **委卖量 > 委买量**

阈值可以在 `application.yml` 修改：

```yaml
stock-picker:
  auction-turnover-rate: 2.0
  auction-pct-change: 5.0
```

## 2. 妖股评分

评分范围：**0 ~ 95**。

当前会综合：

- 竞价换手率
- 09:25 涨幅
- 委卖 / 委买比例
- 连板数
- 龙虎榜净流入
- 封单资金 / 流通市值
- 炸板次数

其中“委卖 > 委买”只作为用户指定的硬筛选条件。

评分不会简单认为委卖越大越好：若委卖 / 委买比例过高，会按卖压风险扣分。

分层：

- 75 ~ 95：高
- 55 ~ 74：中
- 0 ~ 54：低

建议后续基于历史竞价快照和真实后验收益做回测，再把当前启发式评分升级为经过校准的统计概率。

## 3. 数据时序

### 09:25 集合竞价

程序默认在 **Asia/Shanghai 09:25:05** 自动抓一次快照：

```yaml
stock-picker:
  auction-capture-cron: "5 25 9 * * MON-FRI"
```

程序应在 09:25 前启动。

快照会保存到：

```text
./data/auction-YYYYMMDD.json
```

如果当天没有保存过 09:25 快照，页面会临时读取实时行情，但会明确标为“非严格 09:25 快照”。

盘中实时换手率不能等同于竞价阶段换手率。

### 龙虎榜

龙虎榜通常在盘后发布，因此早盘查询“当天龙虎榜”时并没有当天最终数据。

程序会从目标日期开始向前最多回退 7 天，找到最近一个有已公布龙虎榜数据的交易日，并在页面显示真实数据日期。

### 连板

涨停 / 连板池按目标交易日读取，主要字段包括：

- 连板数
- 炸板次数
- 换手率
- 封单资金
- 流通市值
- 首次封板时间
- 最后封板时间
- 行业

## 4. 数据源

默认使用东方财富网页公开接口适配器：

- 实时行情：`push2.eastmoney.com`
- 涨停 / 连板池：`push2ex.eastmoney.com/getTopicZTPool`
- 龙虎榜：`datacenter-web.eastmoney.com`

这些是网页数据接口，并非承诺长期稳定的正式商业 API，字段、访问频率和反爬策略都可能变化。

如果用于长期实盘，建议实现 `MarketDataProvider` 接入：

- 券商 Level-2
- 授权行情服务
- 自建行情网关

核心选股和评分代码不需要改。

## 5. 运行

要求：

- JDK 8
- Maven 3.6+

启动：

```bash
mvn clean package
java -jar target/stock-picker-0.1.0.jar
```

打开：

```text
http://localhost:8080
```

## 6. Mock 模式

没有行情网络时可以直接测试页面：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

或：

```bash
java -jar target/stock-picker-0.1.0.jar --spring.profiles.active=mock
```

## 7. REST API

### 看板

```http
GET /api/dashboard
GET /api/dashboard?date=2026-09-23
```

返回：

- 竞价候选
- 龙虎榜明细
- 涨停 / 连板池
- 妖股评分
- 竞价快照是否为严格 09:25 数据
- 龙虎榜实际数据日期

### 手动抓取

```http
POST /api/auction/capture
```

用于调试或临时保存当前行情。

若当前不在 09:25 附近，结果只应作为调试数据。

### 当前规则

```http
GET /api/rules
```

## 8. 代码结构

```text
src/main/java/com/stockpicker
├── config
│   └── StockPickerProperties
├── controller
│   └── StockPickerController
├── model
├── provider
│   ├── MarketDataProvider
│   ├── EastMoneyMarketDataProvider
│   └── MockMarketDataProvider
└── service
    ├── AuctionCaptureService
    ├── AuctionSnapshotStore
    ├── StockPickerService
    └── YaoStockScorer
```

## 9. 后续建议

优先级较高的下一步：

1. 增加 MySQL / SQLite 保存每日 09:25 全量竞价快照。
2. 保存候选股后续 09:30、10:00、收盘、次日收益。
3. 对“妖股概率”做历史回测和概率校准。
4. 加入竞价成交额 / 自由流通市值、量比、题材热度、昨日封单强度。
5. 接入 Level-2 的真实委托队列与撤单数据。
6. 增加 ST、退市整理、上市天数、流动性等风险过滤。
7. 增加策略版本号，保证回测结果可复现。

## 10. 测试

```bash
mvn test
```

仓库包含 GitHub Actions，在 `main` 分支 push 后自动使用 JDK 8 执行 Maven 测试。
