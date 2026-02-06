package com.nickfinance.dashboard.util

import com.nickfinance.dashboard.data.local.entity.ColumnDefEntity

object FormulaParser {

    /**
     * Evaluates a formula string using column values from the current row.
     * Supported formulas:
     * - SUM(col1, col2, ...) — sum of specified columns
     * - col1 - col2 — subtraction
     * - col1 / col2 * 100 — ratio calculation
     * - (col1 - col2) / col1 * 100 — percentage calculation
     *
     * Column references use column names.
     */
    fun evaluate(
        formula: String,
        columns: List<ColumnDefEntity>,
        cellValues: Map<Long, String> // columnId -> value
    ): String {
        return try {
            val trimmed = formula.trim()

            // Check for SUM function
            if (trimmed.startsWith("SUM(", ignoreCase = true)) {
                return evaluateSum(trimmed, columns, cellValues)
            }

            // General expression evaluation
            val result = evaluateExpression(trimmed, columns, cellValues)
            if (result.isNaN() || result.isInfinite()) "—" else result.toString()
        } catch (e: Exception) {
            if (e.message == "#REF!") "#REF!" else "—"
        }
    }

    private fun evaluateSum(
        formula: String,
        columns: List<ColumnDefEntity>,
        cellValues: Map<Long, String>
    ): String {
        // Extract column names from SUM(col1, col2, ...)
        val inner = formula.substringAfter("SUM(").substringBeforeLast(")")
        val colNames = inner.split(",").map { it.trim() }

        var sum = 0.0
        for (name in colNames) {
            val col = columns.find { it.name == name }
                ?: throw Exception("#REF!")
            val value = cellValues[col.id]?.toDoubleOrNull() ?: 0.0
            sum += value
        }
        return sum.toString()
    }

    private fun evaluateExpression(
        expr: String,
        columns: List<ColumnDefEntity>,
        cellValues: Map<Long, String>
    ): Double {
        // Replace column names with their numeric values (longest names first to avoid partial matches)
        var replaced = expr
        val sortedColumns = columns.sortedByDescending { it.name.length }
        for (col in sortedColumns) {
            if (replaced.contains(col.name)) {
                val value = cellValues[col.id]?.toDoubleOrNull() ?: 0.0
                replaced = replaced.replace(col.name, value.toString())
            }
        }

        // Simple arithmetic expression evaluator
        return calculateExpression(replaced)
    }

    private fun calculateExpression(expr: String): Double {
        val tokens = tokenize(expr.replace(" ", ""))
        val pos = intArrayOf(0)
        val result = parseAddSub(tokens, pos)
        return result
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            when {
                expr[i].isDigit() || expr[i] == '.' -> {
                    val start = i
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                    tokens.add(expr.substring(start, i))
                }
                expr[i] == '-' && (tokens.isEmpty() || tokens.last() in listOf("(", "+", "-", "*", "/")) -> {
                    // Negative number
                    val start = i
                    i++
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                    tokens.add(expr.substring(start, i))
                }
                expr[i] in "+-*/()" -> {
                    tokens.add(expr[i].toString())
                    i++
                }
                else -> i++
            }
        }
        return tokens
    }

    private fun parseAddSub(tokens: List<String>, pos: IntArray): Double {
        var result = parseMulDiv(tokens, pos)
        while (pos[0] < tokens.size && tokens[pos[0]] in listOf("+", "-")) {
            val op = tokens[pos[0]]
            pos[0]++
            val right = parseMulDiv(tokens, pos)
            result = if (op == "+") result + right else result - right
        }
        return result
    }

    private fun parseMulDiv(tokens: List<String>, pos: IntArray): Double {
        var result = parsePrimary(tokens, pos)
        while (pos[0] < tokens.size && tokens[pos[0]] in listOf("*", "/")) {
            val op = tokens[pos[0]]
            pos[0]++
            val right = parsePrimary(tokens, pos)
            result = if (op == "*") result * right else result / right
        }
        return result
    }

    private fun parsePrimary(tokens: List<String>, pos: IntArray): Double {
        if (pos[0] >= tokens.size) return 0.0
        return if (tokens[pos[0]] == "(") {
            pos[0]++ // skip (
            val result = parseAddSub(tokens, pos)
            if (pos[0] < tokens.size && tokens[pos[0]] == ")") pos[0]++ // skip )
            result
        } else {
            val value = tokens[pos[0]].toDoubleOrNull() ?: 0.0
            pos[0]++
            value
        }
    }

    /**
     * Check if a formula references a column that no longer exists.
     */
    fun hasInvalidReferences(
        formula: String,
        columns: List<ColumnDefEntity>
    ): Boolean {
        val trimmed = formula.trim()
        if (trimmed.startsWith("SUM(", ignoreCase = true)) {
            val inner = trimmed.substringAfter("SUM(").substringBeforeLast(")")
            val colNames = inner.split(",").map { it.trim() }
            return colNames.any { name -> columns.none { it.name == name } }
        }
        // For expressions, check if any column name appears
        val colNames = columns.map { it.name }
        var remaining = trimmed
        for (name in colNames.sortedByDescending { it.length }) {
            remaining = remaining.replace(name, "")
        }
        // If remaining has non-operator/non-number chars, might have invalid refs
        return remaining.any { it.isLetter() }
    }
}
