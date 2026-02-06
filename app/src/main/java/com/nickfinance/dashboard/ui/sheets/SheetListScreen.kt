package com.nickfinance.dashboard.ui.sheets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nickfinance.dashboard.data.local.entity.DataSheetEntity
import com.nickfinance.dashboard.data.model.TemplateType
import com.nickfinance.dashboard.ui.navigation.Screen
import com.nickfinance.dashboard.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Template card model ──

private data class TemplateOption(
    val emoji: String,
    val label: String,
    val templateType: TemplateType? // null means blank sheet
)

private val templateOptions = listOf(
    TemplateOption("\uD83D\uDCB0", "\u8D44\u4EA7\u4FE1\u8D37", TemplateType.ASSET),
    TemplateOption("\uD83D\uDCB3", "\u6536\u652F\u7EDF\u8BA1", TemplateType.INCOME_EXPENSE),
    TemplateOption("\uD83D\uDCC8", "\u5B9A\u6295\u7EDF\u8BA1", TemplateType.INVESTMENT),
    TemplateOption("\uD83D\uDCC4", "\u7A7A\u767D\u8868\u683C", null)
)

// ── Screen ──

@Composable
fun SheetListScreen(
    navController: NavController,
    viewModel: SheetListViewModel = hiltViewModel()
) {
    val sheets by viewModel.sheets.collectAsState()
    val rowCounts by viewModel.rowCounts.collectAsState()

    var showBlankSheetDialog by remember { mutableStateOf(false) }
    var blankSheetName by remember { mutableStateOf("") }

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameSheetId by remember { mutableLongStateOf(0L) }
    var renameText by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteSheetId by remember { mutableLongStateOf(0L) }

    val colors = AppTheme.colors
    val dateFormat = remember { SimpleDateFormat("M/d", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top bar ──
        TopBar(
            onAddClick = { showBlankSheetDialog = true }
        )

        if (sheets.isEmpty()) {
            // ── Empty state ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u8FD8\u6CA1\u6709\u6570\u636E\u8868\uFF0C\u70B9\u51FB\u53F3\u4E0A\u89D2\u521B\u5EFA",
                    color = colors.textTertiary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // ── Sheet list ──
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sheets, key = { it.id }) { sheet ->
                    LaunchedEffect(sheet.id) {
                        viewModel.observeRowCount(sheet.id)
                    }
                    val count = rowCounts[sheet.id] ?: 0
                    SheetCard(
                        sheet = sheet,
                        rowCount = count,
                        dateFormat = dateFormat,
                        onClick = {
                            navController.navigate(Screen.SheetDetail.createRoute(sheet.id))
                        },
                        onRename = {
                            renameSheetId = sheet.id
                            renameText = sheet.name
                            showRenameDialog = true
                        },
                        onDelete = {
                            deleteSheetId = sheet.id
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        // ── Template section ──
        TemplateSection(
            onTemplateClick = { option ->
                if (option.templateType != null) {
                    viewModel.createFromTemplate(option.templateType) { sheetId ->
                        navController.navigate(Screen.SheetDetail.createRoute(sheetId))
                    }
                } else {
                    blankSheetName = ""
                    showBlankSheetDialog = true
                }
            }
        )
    }

    // ── Blank sheet name dialog ──
    if (showBlankSheetDialog) {
        AlertDialog(
            onDismissRequest = { showBlankSheetDialog = false },
            containerColor = colors.cardSurface,
            titleContentColor = colors.textPrimary,
            title = { Text("\u65B0\u5EFA\u7A7A\u767D\u8868\u683C") },
            text = {
                OutlinedTextField(
                    value = blankSheetName,
                    onValueChange = { blankSheetName = it },
                    placeholder = {
                        Text(
                            "\u8BF7\u8F93\u5165\u8868\u683C\u540D\u79F0",
                            color = colors.textTertiary
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        cursorColor = colors.accentBlue,
                        focusedBorderColor = colors.accentBlue,
                        unfocusedBorderColor = colors.border
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = blankSheetName.trim().ifEmpty { "\u65B0\u5EFA\u8868\u683C" }
                        viewModel.createBlankSheet(name) { sheetId ->
                            navController.navigate(Screen.SheetDetail.createRoute(sheetId))
                        }
                        showBlankSheetDialog = false
                    }
                ) {
                    Text("\u521B\u5EFA", color = colors.accentBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlankSheetDialog = false }) {
                    Text("\u53D6\u6D88", color = colors.textSecondary)
                }
            }
        )
    }

    // ── Rename dialog ──
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = colors.cardSurface,
            titleContentColor = colors.textPrimary,
            title = { Text("\u91CD\u547D\u540D") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    placeholder = {
                        Text(
                            "\u8BF7\u8F93\u5165\u65B0\u540D\u79F0",
                            color = colors.textTertiary
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        cursorColor = colors.accentBlue,
                        focusedBorderColor = colors.accentBlue,
                        unfocusedBorderColor = colors.border
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newName = renameText.trim()
                        if (newName.isNotEmpty()) {
                            viewModel.renameSheet(renameSheetId, newName)
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("\u786E\u5B9A", color = colors.accentBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("\u53D6\u6D88", color = colors.textSecondary)
                }
            }
        )
    }

    // ── Delete confirmation dialog ──
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = colors.cardSurface,
            titleContentColor = colors.textPrimary,
            title = { Text("\u5220\u9664\u786E\u8BA4") },
            text = {
                Text(
                    "\u786E\u5B9A\u8981\u5220\u9664\u8FD9\u4E2A\u6570\u636E\u8868\u5417\uFF1F\u6B64\u64CD\u4F5C\u4E0D\u53EF\u64A4\u9500\u3002",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSheet(deleteSheetId)
                        showDeleteDialog = false
                    }
                ) {
                    Text("\u5220\u9664", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("\u53D6\u6D88", color = colors.textSecondary)
                }
            }
        )
    }
}

// ── Top bar ──

@Composable
private fun TopBar(onAddClick: () -> Unit) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "\u6570\u636E\u8868",
            color = colors.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "\u65B0\u5EFA\u6570\u636E\u8868",
                tint = colors.accentBlue
            )
        }
    }
}

// ── Sheet card ──

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SheetCard(
    sheet: DataSheetEntity,
    rowCount: Int,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors
    var showMenu by remember { mutableStateOf(false) }
    val updatedDate = remember(sheet.updatedAt) {
        dateFormat.format(Date(sheet.updatedAt))
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.cardSurface)
                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji icon container
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.cardSurfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sheet.icon,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sheet.name,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${rowCount} \u6761\u8BB0\u5F55 \u00B7 \u66F4\u65B0\u4E8E ${updatedDate}",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Context menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = colors.cardSurfaceSecondary
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "\u91CD\u547D\u540D",
                        color = colors.textPrimary,
                        fontSize = 14.sp
                    )
                },
                onClick = {
                    showMenu = false
                    onRename()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "\u5220\u9664",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                },
                onClick = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}

// ── Template section ──

@Composable
private fun TemplateSection(onTemplateClick: (TemplateOption) -> Unit) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "\u4ECE\u6A21\u677F\u521B\u5EFA",
            color = colors.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(110.dp),
            userScrollEnabled = false
        ) {
            items(templateOptions) { option ->
                TemplateCard(
                    option = option,
                    onClick = { onTemplateClick(option) }
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    option: TemplateOption,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.cardSurfaceSecondary)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = option.emoji,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = option.label,
            color = colors.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
