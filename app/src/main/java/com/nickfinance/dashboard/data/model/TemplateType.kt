package com.nickfinance.dashboard.data.model

data class TemplateColumnDef(
    val name: String,
    val type: String,
    val groupName: String? = null,
    val groupColor: String? = null,
    val isNegativeRed: Boolean = false,
    val formula: String? = null
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
            TemplateColumnDef("备用金", "CURRENCY", "资产", "#2ECC71"),
            TemplateColumnDef("养老金", "CURRENCY", "资产", "#2ECC71"),
            TemplateColumnDef("可用资金", "CURRENCY", "资产", "#2ECC71"),
            TemplateColumnDef("理财资产", "CURRENCY", "资产", "#2ECC71"),
            TemplateColumnDef("信贷资产", "CURRENCY", "信贷", "#E74C56", isNegativeRed = true),
            TemplateColumnDef("信贷说明", "TEXT", "信贷", "#E74C56"),
            TemplateColumnDef("计划还款日", "DATE", "信贷", "#E74C56"),
            TemplateColumnDef("总计", "CURRENCY", "数据", "#F0B840"),
            TemplateColumnDef("资产变化额", "CURRENCY", "数据", "#F0B840"),
            TemplateColumnDef("资产变化率", "PERCENTAGE", "数据", "#F0B840")
        )
    ),
    INCOME_EXPENSE(
        displayName = "月度收支统计",
        icon = "\uD83D\uDCB3", // 💳
        columns = listOf(
            TemplateColumnDef("日期", "DATE"),
            TemplateColumnDef("工资", "CURRENCY", "收入", "#2ECC71"),
            TemplateColumnDef("其他收入", "CURRENCY", "收入", "#2ECC71"),
            TemplateColumnDef("日常开销", "CURRENCY", "支出", "#E74C56", isNegativeRed = true),
            TemplateColumnDef("养老金", "CURRENCY", "支出", "#E74C56"),
            TemplateColumnDef("备用金", "CURRENCY", "支出", "#E74C56"),
            TemplateColumnDef("理财资产", "CURRENCY", "支出", "#E74C56"),
            TemplateColumnDef("家庭基金", "CURRENCY", "支出", "#E74C56"),
            TemplateColumnDef("大额开销", "CURRENCY", "支出", "#E74C56"),
            TemplateColumnDef("收入总计", "CURRENCY", "数据与说明", "#F0B840"),
            TemplateColumnDef("开销总计", "CURRENCY", "数据与说明", "#F0B840", isNegativeRed = true),
            TemplateColumnDef("其他收入说明", "TEXT", "数据与说明", "#F0B840"),
            TemplateColumnDef("大额开销说明", "TEXT", "数据与说明", "#F0B840"),
            TemplateColumnDef("储蓄率", "PERCENTAGE", "数据与说明", "#F0B840"),
            TemplateColumnDef("开销率", "PERCENTAGE", "数据与说明", "#F0B840")
        )
    ),
    INVESTMENT(
        displayName = "月度定投统计",
        icon = "\uD83D\uDCC8", // 📈
        columns = listOf(
            TemplateColumnDef("日期", "DATE"),
            TemplateColumnDef("当月存入", "CURRENCY"),
            TemplateColumnDef("实际定投", "CURRENCY"),
            TemplateColumnDef("创业板ETF连接A", "CURRENCY", "定投金额", "#E74C56"),
            TemplateColumnDef("上证科创50连接A", "CURRENCY", "定投金额", "#E74C56"),
            TemplateColumnDef("中证500ETF连接A", "CURRENCY", "定投金额", "#E74C56"),
            TemplateColumnDef("沪深300ETF连接A", "CURRENCY", "定投金额", "#E74C56"),
            TemplateColumnDef("期初资产", "CURRENCY", "统计数据", "#E74C56"),
            TemplateColumnDef("期末资产", "CURRENCY", "统计数据", "#E74C56"),
            TemplateColumnDef("上期收益", "CURRENCY", "统计数据", "#E74C56"),
            TemplateColumnDef("上期收益率", "PERCENTAGE", "统计数据", "#E74C56"),
            TemplateColumnDef("创业板", "PERCENTAGE", "定投占比", "#F0B840"),
            TemplateColumnDef("科创板", "PERCENTAGE", "定投占比", "#F0B840"),
            TemplateColumnDef("中证500", "PERCENTAGE", "定投占比", "#F0B840"),
            TemplateColumnDef("沪深300", "PERCENTAGE", "定投占比", "#F0B840")
        )
    )
}
