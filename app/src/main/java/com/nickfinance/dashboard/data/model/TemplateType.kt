package com.nickfinance.dashboard.data.model

data class TemplateColumnDef(
    val name: String,
    val type: String,
    val groupName: String? = null,
    val groupColor: String? = null,
    val isNegativeRed: Boolean = false,
    val formula: String? = null,
    val columnRole: String? = null
)

enum class TemplateType(
    val displayName: String,
    val icon: String,
    val columns: List<TemplateColumnDef>
) {
    ASSET(
        displayName = "月度资产信贷统计",
        icon = "\uD83D\uDCB0", // 💰
        columns = listOf(
            TemplateColumnDef("日期", "DATE"),
            TemplateColumnDef("备用金", "CURRENCY", "资产", "#2ECC71", columnRole = "ASSET"),
            TemplateColumnDef("养老金", "CURRENCY", "资产", "#2ECC71", columnRole = "ASSET"),
            TemplateColumnDef("可用资金", "CURRENCY", "资产", "#2ECC71", columnRole = "ASSET"),
            TemplateColumnDef("理财资产", "CURRENCY", "资产", "#2ECC71", columnRole = "ASSET"),
            TemplateColumnDef("信贷资产", "CURRENCY", "信贷", "#E74C56",
                isNegativeRed = true, columnRole = "LIABILITY"),
            TemplateColumnDef("信贷说明", "TEXT", "信贷", "#E74C56"),
            TemplateColumnDef("计划还款日", "DATE", "信贷", "#E74C56"),
            TemplateColumnDef("总计", "CURRENCY", "数据", "#F0B840",
                formula = "SUM_ROLE(ASSET) + SUM_ROLE(LIABILITY)"),
            TemplateColumnDef("资产变化额", "CURRENCY", "数据", "#F0B840",
                formula = "总计 - PREV(总计)"),
            TemplateColumnDef("资产变化率", "PERCENTAGE", "数据", "#F0B840",
                formula = "资产变化额 / PREV(总计) * 100")
        )
    ),
    INCOME_EXPENSE(
        displayName = "月度收支统计",
        icon = "\uD83D\uDCB3", // 💳
        columns = listOf(
            TemplateColumnDef("日期", "DATE"),
            TemplateColumnDef("工资", "CURRENCY", "收入", "#2ECC71", columnRole = "INCOME"),
            TemplateColumnDef("其他收入", "CURRENCY", "收入", "#2ECC71", columnRole = "INCOME"),
            TemplateColumnDef("日常开销", "CURRENCY", "支出", "#E74C56",
                isNegativeRed = true, columnRole = "EXPENSE"),
            TemplateColumnDef("养老金", "CURRENCY", "支出", "#E74C56"),
            TemplateColumnDef("备用金", "CURRENCY", "支出", "#E74C56"),
            TemplateColumnDef("理财资产", "CURRENCY", "支出", "#E74C56"),
            TemplateColumnDef("家庭基金", "CURRENCY", "支出", "#E74C56"),
            TemplateColumnDef("大额开销", "CURRENCY", "支出", "#E74C56",
                isNegativeRed = true, columnRole = "EXPENSE"),
            TemplateColumnDef("收入总计", "CURRENCY", "数据与说明", "#F0B840",
                formula = "SUM_ROLE(INCOME)"),
            TemplateColumnDef("开销总计", "CURRENCY", "数据与说明", "#F0B840",
                isNegativeRed = true, formula = "SUM_ROLE(EXPENSE)"),
            TemplateColumnDef("其他收入说明", "TEXT", "数据与说明", "#F0B840"),
            TemplateColumnDef("大额开销说明", "TEXT", "数据与说明", "#F0B840"),
            TemplateColumnDef("储蓄率", "PERCENTAGE", "数据与说明", "#F0B840",
                formula = "(收入总计 - 开销总计) / 收入总计 * 100"),
            TemplateColumnDef("开销率", "PERCENTAGE", "数据与说明", "#F0B840",
                formula = "开销总计 / 收入总计 * 100")
        )
    ),
    INVESTMENT(
        displayName = "月度定投统计",
        icon = "\uD83D\uDCC8", // 📈
        columns = listOf(
            TemplateColumnDef("日期", "DATE"),
            TemplateColumnDef("当月存入", "CURRENCY"),
            TemplateColumnDef("创业板ETF连接A", "CURRENCY", "定投金额", "#E74C56",
                columnRole = "INVESTMENT"),
            TemplateColumnDef("上证科创50连接A", "CURRENCY", "定投金额", "#E74C56",
                columnRole = "INVESTMENT"),
            TemplateColumnDef("中证500ETF连接A", "CURRENCY", "定投金额", "#E74C56",
                columnRole = "INVESTMENT"),
            TemplateColumnDef("沪深300ETF连接A", "CURRENCY", "定投金额", "#E74C56",
                columnRole = "INVESTMENT"),
            TemplateColumnDef("实际定投", "CURRENCY",
                formula = "SUM_ROLE(INVESTMENT)"),
            TemplateColumnDef("期初资产", "CURRENCY", "统计数据", "#E74C56"),
            TemplateColumnDef("期末资产", "CURRENCY", "统计数据", "#E74C56"),
            TemplateColumnDef("上期收益", "CURRENCY", "统计数据", "#E74C56",
                formula = "期末资产 - 期初资产 - 实际定投"),
            TemplateColumnDef("上期收益率", "PERCENTAGE", "统计数据", "#E74C56",
                formula = "上期收益 / 期初资产 * 100"),
            TemplateColumnDef("创业板", "PERCENTAGE", "定投占比", "#F0B840",
                formula = "创业板ETF连接A / 实际定投 * 100"),
            TemplateColumnDef("科创板", "PERCENTAGE", "定投占比", "#F0B840",
                formula = "上证科创50连接A / 实际定投 * 100"),
            TemplateColumnDef("中证500", "PERCENTAGE", "定投占比", "#F0B840",
                formula = "中证500ETF连接A / 实际定投 * 100"),
            TemplateColumnDef("沪深300", "PERCENTAGE", "定投占比", "#F0B840",
                formula = "沪深300ETF连接A / 实际定投 * 100")
        )
    )
}
