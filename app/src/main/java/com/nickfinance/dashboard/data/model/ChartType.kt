package com.nickfinance.dashboard.data.model

enum class ChartType(val displayName: String, val icon: String) {
    LINE("折线图", "\uD83D\uDCC8"),
    BAR("柱状图", "\uD83D\uDCCA"),
    PIE("饼状图", "\uD83C\uDF69"),
    AREA("面积图", "\uD83D\uDCC9"),
    HORIZONTAL_BAR("条形图", "\uD83D\uDCCF"),
    PROGRESS("进度图", "\uD83C\uDFAF"),
    STAT_NUMBER("统计数字", "\uD83D\uDD22"),
    COMBINED("组合图", "\uD83D\uDCCA");

    companion object {
        fun fromString(value: String): ChartType {
            return entries.find { it.name == value } ?: LINE
        }
    }
}
