package com.nickfinance.dashboard.data.local.converter

enum class ColumnType(val displayName: String) {
    DATE("日期"),
    NUMBER("数值"),
    CURRENCY("货币"),
    PERCENTAGE("百分比"),
    TEXT("文本");

    companion object {
        fun fromString(value: String): ColumnType {
            return entries.find { it.name == value } ?: TEXT
        }
    }
}

enum class ColumnRole(val displayName: String) {
    NONE("无"),
    INCOME("收入"),
    EXPENSE("支出"),
    ASSET("资产"),
    LIABILITY("负债"),
    INVESTMENT("定投");

    companion object {
        fun fromString(value: String?): ColumnRole {
            if (value.isNullOrBlank()) return NONE
            return entries.find { it.name == value } ?: NONE
        }
    }
}
