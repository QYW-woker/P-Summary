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
