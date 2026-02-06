package com.nickfinance.dashboard.util

import java.text.DecimalFormat

object FormatUtils {

    private val currencyFormat = DecimalFormat("#,##0.00")
    private val percentFormat = DecimalFormat("0.00")
    private val numberFormat = DecimalFormat("#,##0.##")

    fun formatCurrency(value: String, currencySymbol: String = "¥"): String {
        val number = value.toDoubleOrNull() ?: return "—"
        return "$currencySymbol${currencyFormat.format(number)}"
    }

    fun formatPercentage(value: String): String {
        val number = value.toDoubleOrNull() ?: return "—"
        return "${percentFormat.format(number)}%"
    }

    fun formatNumber(value: String): String {
        val number = value.toDoubleOrNull() ?: return "—"
        return numberFormat.format(number)
    }

    fun formatDate(value: String): String {
        return value.ifBlank { "—" }
    }

    fun isNegative(value: String): Boolean {
        val number = value.toDoubleOrNull() ?: return false
        return number < 0
    }

    fun formatAxisValue(value: Double): String {
        return when {
            value >= 10000 || value <= -10000 -> "${(value / 1000).toInt()}k"
            value >= 1000 || value <= -1000 -> String.format("%.1fk", value / 1000)
            else -> value.toInt().toString()
        }
    }

    fun formatMonthLabel(dateStr: String): String {
        // Expected formats: yyyy/M/d or yyyy/MM/dd
        return try {
            val parts = dateStr.split("/")
            if (parts.size >= 2) {
                "${parts[1].toInt()}月"
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun parseDouble(value: String): Double? = value.toDoubleOrNull()
}
