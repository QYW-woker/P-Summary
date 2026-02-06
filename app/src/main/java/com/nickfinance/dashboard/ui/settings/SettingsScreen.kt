package com.nickfinance.dashboard.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nickfinance.dashboard.ui.theme.AppTheme
import com.nickfinance.dashboard.ui.theme.ThemeMode
import com.nickfinance.dashboard.util.FormatUtils

// ══════════════════════════════════════════════════════════════════════
//  SettingsScreen
// ══════════════════════════════════════════════════════════════════════

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    val themeMode by settingsViewModel.themeMode.collectAsState()
    val currencySymbol by settingsViewModel.currencySymbol.collectAsState()
    val savingsTarget by settingsViewModel.savingsTarget.collectAsState()

    // ── Dialog states ──
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showSavingsTargetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // ── Title ──
        Text(
            text = "设置",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ══ Section: 显示 ══
        SectionHeader(title = "显示")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsItem(
                label = "深色模式",
                value = when (themeMode) {
                    ThemeMode.DARK -> "深色"
                    ThemeMode.LIGHT -> "浅色"
                    ThemeMode.SYSTEM -> "跟随系统"
                },
                onClick = { showThemeDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsItem(
                label = "货币符号",
                value = currencySymbol,
                onClick = { showCurrencyDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ══ Section: 目标 ══
        SectionHeader(title = "目标")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsItem(
                label = "年度存款目标",
                value = FormatUtils.formatCurrency(savingsTarget.toString(), currencySymbol),
                onClick = { showSavingsTargetDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ══ Section: 数据 ══
        SectionHeader(title = "数据")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsItem(
                label = "导出为 CSV",
                showArrow = true,
                onClick = {
                    Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsItem(
                label = "导出为 Excel (.xlsx)",
                showArrow = true,
                onClick = {
                    Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsItem(
                label = "备份数据",
                showArrow = true,
                onClick = {
                    Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsItem(
                label = "恢复数据",
                showArrow = true,
                onClick = {
                    Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ══ Section: 关于 ══
        SectionHeader(title = "关于")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsItem(
                label = "版本",
                value = "1.0.0"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsItem(
                label = "开发者",
                value = "Nick"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // ══════════════════════════════════════════════════════════════════
    //  Dialogs
    // ══════════════════════════════════════════════════════════════════

    // ── Theme mode dialog ──
    if (showThemeDialog) {
        ThemeModeDialog(
            currentMode = themeMode,
            onSelect = { mode ->
                settingsViewModel.setThemeMode(mode)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // ── Currency symbol dialog ──
    if (showCurrencyDialog) {
        CurrencySymbolDialog(
            currentSymbol = currencySymbol,
            onSelect = { symbol ->
                settingsViewModel.setCurrencySymbol(symbol)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    // ── Savings target dialog ──
    if (showSavingsTargetDialog) {
        SavingsTargetDialog(
            currentTarget = savingsTarget,
            onConfirm = { newTarget ->
                settingsViewModel.setSavingsTarget(newTarget)
                showSavingsTargetDialog = false
            },
            onDismiss = { showSavingsTargetDialog = false }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Reusable components
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(title: String) {
    val colors = AppTheme.colors
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
        color = colors.textSecondary,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardSurface)
    ) {
        content()
    }
}

@Composable
private fun SettingsItem(
    label: String,
    value: String? = null,
    showArrow: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
            if (showArrow || onClick != null) {
                if (value != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.textSecondary
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Theme mode selection dialog
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun ThemeModeDialog(
    currentMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val options = listOf(
        ThemeMode.DARK to "深色",
        ThemeMode.LIGHT to "浅色",
        ThemeMode.SYSTEM to "跟随系统"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        titleContentColor = colors.textPrimary,
        title = {
            Text(
                text = "深色模式",
                style = MaterialTheme.typography.titleSmall
            )
        },
        text = {
            Column {
                options.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(mode) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == currentMode,
                            onClick = { onSelect(mode) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.accentBlue,
                                unselectedColor = colors.textSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = colors.accentBlue
                )
            }
        }
    )
}

// ══════════════════════════════════════════════════════════════════════
//  Currency symbol selection dialog
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun CurrencySymbolDialog(
    currentSymbol: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val symbols = listOf("¥", "$", "€", "£")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        titleContentColor = colors.textPrimary,
        title = {
            Text(
                text = "货币符号",
                style = MaterialTheme.typography.titleSmall
            )
        },
        text = {
            Column {
                symbols.forEach { symbol ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(symbol) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = symbol == currentSymbol,
                            onClick = { onSelect(symbol) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.accentBlue,
                                unselectedColor = colors.textSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = symbol,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = colors.accentBlue
                )
            }
        }
    )
}

// ══════════════════════════════════════════════════════════════════════
//  Savings target number input dialog
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun SavingsTargetDialog(
    currentTarget: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val focusManager = LocalFocusManager.current
    var inputText by remember {
        mutableStateOf(
            if (currentTarget == currentTarget.toLong().toDouble()) {
                currentTarget.toLong().toString()
            } else {
                currentTarget.toString()
            }
        )
    }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        titleContentColor = colors.textPrimary,
        title = {
            Text(
                text = "年度存款目标",
                style = MaterialTheme.typography.titleSmall
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { newValue ->
                        // Allow only digits and a single decimal point
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            inputText = newValue
                            isError = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            val parsed = inputText.toDoubleOrNull()
                            if (parsed != null && parsed > 0) {
                                onConfirm(parsed)
                            } else {
                                isError = true
                            }
                        }
                    ),
                    placeholder = {
                        Text(
                            text = "输入目标金额",
                            color = colors.textTertiary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentBlue,
                        unfocusedBorderColor = colors.border,
                        cursorColor = colors.accentBlue,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )
                if (isError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "请输入有效的正数",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = inputText.toDoubleOrNull()
                    if (parsed != null && parsed > 0) {
                        onConfirm(parsed)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(
                    text = "确定",
                    color = colors.accentBlue
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = colors.textSecondary
                )
            }
        }
    )
}
