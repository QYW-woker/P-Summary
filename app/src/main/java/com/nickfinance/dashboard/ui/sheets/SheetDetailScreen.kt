package com.nickfinance.dashboard.ui.sheets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nickfinance.dashboard.data.local.converter.ColumnType
import com.nickfinance.dashboard.data.local.entity.ColumnDefEntity
import com.nickfinance.dashboard.data.model.RowWithCells
import com.nickfinance.dashboard.data.model.SheetFullData
import com.nickfinance.dashboard.ui.theme.AppTheme
import com.nickfinance.dashboard.ui.theme.GroupColorPresets
import com.nickfinance.dashboard.ui.theme.MonoFontFamily
import com.nickfinance.dashboard.ui.theme.parseHexColor
import com.nickfinance.dashboard.util.FormatUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ══════════════════════════════════════════════════
// Constants
// ══════════════════════════════════════════════════

private val DATE_COLUMN_WIDTH = 90.dp
private val STANDARD_COLUMN_WIDTH = 105.dp
private val ROW_HEIGHT = 44.dp

// ══════════════════════════════════════════════════
// Main Screen
// ══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetDetailScreen(
    sheetId: Long,
    navController: NavController,
    viewModel: SheetDetailViewModel = hiltViewModel()
) {
    val fullData by viewModel.sheetFullData.collectAsState()
    val colors = AppTheme.colors

    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showColumnManageSheet by rememberSaveable { mutableStateOf(false) }

    // ── Row form state ──
    var showRowForm by remember { mutableStateOf(false) }
    var editingRowId by remember { mutableStateOf<Long?>(null) }

    val data = fullData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top bar ──
        SheetTopBar(
            sheetName = data?.sheet?.name ?: "",
            rowCount = data?.rows?.size ?: 0,
            onBack = { navController.popBackStack() },
            onNameClick = { showRenameDialog = true },
            onManageColumns = { showColumnManageSheet = true }
        )

        if (data != null && data.columns.isNotEmpty()) {
            // ── Group legend ──
            GroupLegendRow(columns = data.columns)

            // ── Table ──
            SheetTable(
                fullData = data,
                onRowClick = { rowId ->
                    editingRowId = rowId
                    showRowForm = true
                },
                onDeleteRow = { rowId -> viewModel.deleteRow(rowId) },
                onAddRow = {
                    editingRowId = null
                    showRowForm = true
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "暂无列定义",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击右上角「管理列」添加列",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary
                    )
                }
            }
        }
    }

    // ── Rename dialog ──
    if (showRenameDialog) {
        RenameSheetDialog(
            currentName = data?.sheet?.name ?: "",
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                viewModel.renameSheet(newName)
                showRenameDialog = false
            }
        )
    }

    // ── Row form bottom sheet ──
    if (showRowForm && data != null && data.columns.isNotEmpty()) {
        val isEditing = editingRowId != null
        val existingValues = if (isEditing) {
            data.rows.find { it.row.id == editingRowId }?.cells ?: emptyMap()
        } else {
            emptyMap()
        }

        RowFormBottomSheet(
            columns = data.columns,
            existingValues = existingValues,
            isEditing = isEditing,
            onDismiss = {
                showRowForm = false
                editingRowId = null
            },
            onSave = { values ->
                if (isEditing && editingRowId != null) {
                    viewModel.updateRowValues(editingRowId!!, values)
                } else {
                    viewModel.addRowWithValues(values)
                }
                showRowForm = false
                editingRowId = null
            }
        )
    }

    // ── Column manage bottom sheet ──
    if (showColumnManageSheet && data != null) {
        ColumnManageSheet(
            columns = data.columns,
            onDismiss = { showColumnManageSheet = false },
            onAddColumn = { name, type, groupName, groupColor, isNegativeRed ->
                viewModel.addColumn(name, type, groupName, groupColor, isNegativeRed, null)
            },
            onUpdateColumn = { column -> viewModel.updateColumn(column) },
            onDeleteColumn = { columnId -> viewModel.deleteColumn(columnId) },
            onReorderColumns = { columns -> viewModel.reorderColumns(columns) }
        )
    }
}

// ══════════════════════════════════════════════════
// Top bar
// ══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetTopBar(
    sheetName: String,
    rowCount: Int,
    onBack: () -> Unit,
    onNameClick: () -> Unit,
    onManageColumns: () -> Unit
) {
    val colors = AppTheme.colors

    TopAppBar(
        title = {
            Column(
                modifier = Modifier.clickable { onNameClick() }
            ) {
                Text(
                    text = sheetName.ifEmpty { "数据表" },
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${rowCount}条记录",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = colors.textPrimary
                )
            }
        },
        actions = {
            TextButton(onClick = onManageColumns) {
                Text(
                    text = "管理列",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.accentBlue
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.cardSurface
        )
    )
}

// ══════════════════════════════════════════════════
// Group legend row
// ══════════════════════════════════════════════════

@Composable
private fun GroupLegendRow(columns: List<ColumnDefEntity>) {
    val colors = AppTheme.colors
    val groups = columns
        .filter { !it.groupName.isNullOrBlank() && !it.groupColor.isNullOrBlank() }
        .map { it.groupName!! to it.groupColor!! }
        .distinctBy { it.first }

    if (groups.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.cardSurface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        groups.forEach { (name, colorHex) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(colorHex))
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
        }
    }

    HorizontalDivider(thickness = 1.dp, color = colors.border)
}

// ══════════════════════════════════════════════════
// Sheet table (frozen first column + scrollable rest)
// ══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SheetTable(
    fullData: SheetFullData,
    onRowClick: (rowId: Long) -> Unit,
    onDeleteRow: (rowId: Long) -> Unit,
    onAddRow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val columns = fullData.columns
    val rows = fullData.rows
    val horizontalScrollState = rememberScrollState()

    if (columns.isEmpty()) return

    val firstColumn = columns.first()
    val restColumns = columns.drop(1)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // ── Sticky header row ──
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.cardSurface)
            ) {
                HeaderCell(
                    column = firstColumn,
                    width = DATE_COLUMN_WIDTH,
                    isFirst = true
                )
                Row(
                    modifier = Modifier.horizontalScroll(horizontalScrollState)
                ) {
                    restColumns.forEach { column ->
                        HeaderCell(
                            column = column,
                            width = STANDARD_COLUMN_WIDTH,
                            isFirst = false
                        )
                    }
                }
            }
            HorizontalDivider(thickness = 1.dp, color = colors.border)
        }

        // ── Data rows with swipe-to-dismiss ──
        itemsIndexed(
            items = rows,
            key = { _, rowWithCells -> rowWithCells.row.id }
        ) { _, rowWithCells ->
            SwipeToDeleteRow(
                rowWithCells = rowWithCells,
                firstColumn = firstColumn,
                restColumns = restColumns,
                horizontalScrollState = horizontalScrollState,
                onRowClick = { onRowClick(rowWithCells.row.id) },
                onDelete = { onDeleteRow(rowWithCells.row.id) }
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = colors.border.copy(alpha = 0.5f)
            )
        }

        // ── Add row button ──
        item {
            Spacer(modifier = Modifier.height(8.dp))
            AddRowButton(onClick = onAddRow)
        }
    }
}

// ══════════════════════════════════════════════════
// Header cell
// ══════════════════════════════════════════════════

@Composable
private fun HeaderCell(
    column: ColumnDefEntity,
    width: androidx.compose.ui.unit.Dp,
    isFirst: Boolean
) {
    val colors = AppTheme.colors
    val groupColor = if (!column.groupColor.isNullOrBlank()) {
        parseHexColor(column.groupColor)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(ROW_HEIGHT)
            .then(
                if (isFirst) Modifier.background(colors.cardSurface) else Modifier
            )
            .drawBehind {
                if (groupColor != Color.Transparent) {
                    val barHeight = 2.dp.toPx()
                    drawRect(
                        color = groupColor,
                        topLeft = Offset(0f, size.height - barHeight),
                        size = androidx.compose.ui.geometry.Size(size.width, barHeight)
                    )
                }
            }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = column.name,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ══════════════════════════════════════════════════
// Swipe-to-delete row wrapper
// ══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteRow(
    rowWithCells: RowWithCells,
    firstColumn: ColumnDefEntity,
    restColumns: List<ColumnDefEntity>,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    onRowClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val backgroundColor by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    colors.semanticRed
                } else {
                    Color.Transparent
                },
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.White
                )
            }
        },
        content = {
            DataRow(
                rowWithCells = rowWithCells,
                firstColumn = firstColumn,
                restColumns = restColumns,
                horizontalScrollState = horizontalScrollState,
                onRowClick = onRowClick
            )
        }
    )
}

// ══════════════════════════════════════════════════
// Data row (frozen first column + scrollable cells)
// ══════════════════════════════════════════════════

@Composable
private fun DataRow(
    rowWithCells: RowWithCells,
    firstColumn: ColumnDefEntity,
    restColumns: List<ColumnDefEntity>,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    onRowClick: () -> Unit
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Frozen first column cell
        DataCell(
            value = rowWithCells.cells[firstColumn.id] ?: "",
            column = firstColumn,
            width = DATE_COLUMN_WIDTH,
            isFirst = true,
            onClick = onRowClick
        )
        // Scrollable remaining cells
        Row(
            modifier = Modifier.horizontalScroll(horizontalScrollState)
        ) {
            restColumns.forEach { column ->
                DataCell(
                    value = rowWithCells.cells[column.id] ?: "",
                    column = column,
                    width = STANDARD_COLUMN_WIDTH,
                    isFirst = false,
                    onClick = onRowClick
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════
// Data cell
// ══════════════════════════════════════════════════

@Composable
private fun DataCell(
    value: String,
    column: ColumnDefEntity,
    width: androidx.compose.ui.unit.Dp,
    isFirst: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    val colType = ColumnType.fromString(column.type)

    val displayText = when {
        value.isBlank() -> "\u2014"
        else -> when (colType) {
            ColumnType.CURRENCY -> FormatUtils.formatCurrency(value)
            ColumnType.PERCENTAGE -> FormatUtils.formatPercentage(value)
            ColumnType.NUMBER -> FormatUtils.formatNumber(value)
            ColumnType.DATE -> FormatUtils.formatDate(value)
            ColumnType.TEXT -> value
        }
    }

    val isNegative = column.isNegativeRed && FormatUtils.isNegative(value)
    val isEmpty = value.isBlank()
    val textColor = when {
        isEmpty -> colors.textTertiary
        isNegative -> colors.semanticRed
        else -> colors.textPrimary
    }
    val fontFamily: FontFamily? = when (colType) {
        ColumnType.CURRENCY, ColumnType.NUMBER, ColumnType.PERCENTAGE -> MonoFontFamily
        else -> null
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(ROW_HEIGHT)
            .then(
                if (isFirst) Modifier.background(colors.cardSurface) else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        contentAlignment = when (colType) {
            ColumnType.CURRENCY, ColumnType.NUMBER, ColumnType.PERCENTAGE -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontFamily = fontFamily,
            textAlign = when (colType) {
                ColumnType.CURRENCY, ColumnType.NUMBER, ColumnType.PERCENTAGE -> TextAlign.End
                else -> TextAlign.Start
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ══════════════════════════════════════════════════
// Add row button
// ══════════════════════════════════════════════════

@Composable
private fun AddRowButton(onClick: () -> Unit) {
    val colors = AppTheme.colors
    val borderColor = colors.border

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(44.dp)
            .drawBehind {
                val dashWidth = 6.dp.toPx()
                val gapWidth = 4.dp.toPx()
                val strokeWidth = 1.dp.toPx()
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, gapWidth))
                val cornerRadius = 8.dp.toPx()
                drawRoundRect(
                    color = borderColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth,
                        pathEffect = pathEffect
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                )
            }
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = colors.accentBlue,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "新增一行",
                style = MaterialTheme.typography.labelMedium,
                color = colors.accentBlue
            )
        }
    }
}

// ══════════════════════════════════════════════════
// Row form bottom sheet (form-based data entry)
// ══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowFormBottomSheet(
    columns: List<ColumnDefEntity>,
    existingValues: Map<Long, String>,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onSave: (Map<Long, String>) -> Unit
) {
    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Mutable form values
    val formValues = remember(existingValues, columns) {
        mutableStateMapOf<Long, String>().apply {
            columns.forEach { col ->
                put(col.id, existingValues[col.id] ?: "")
            }
        }
    }

    // Date picker state
    var datePickerColumnId by remember { mutableStateOf<Long?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardSurface,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.textTertiary.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            // ── Title row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "编辑记录" else "新增记录",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Group columns by group name ──
            val ungrouped = columns.filter { it.groupName.isNullOrBlank() }
            val groupedEntries = columns
                .filter { !it.groupName.isNullOrBlank() }
                .groupBy { it.groupName!! }
                .entries
                .toList()

            // Ungrouped columns first
            ungrouped.forEach { col ->
                FormFieldItem(
                    column = col,
                    value = formValues[col.id] ?: "",
                    onValueChange = { formValues[col.id] = it },
                    onDatePickerRequest = { datePickerColumnId = col.id }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Grouped columns with section headers
            groupedEntries.forEach { (groupName, cols) ->
                val groupColorHex = cols.firstOrNull()?.groupColor ?: ""

                // Group section header
                if (ungrouped.isNotEmpty() || groupedEntries.indexOf(groupedEntries.find { it.key == groupName }) > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (groupColorHex.isNotBlank()) parseHexColor(groupColorHex)
                                else colors.textTertiary
                            )
                    )
                    Text(
                        text = groupName,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (groupColorHex.isNotBlank()) parseHexColor(groupColorHex)
                        else colors.textSecondary
                    )
                }

                cols.forEach { col ->
                    FormFieldItem(
                        column = col,
                        value = formValues[col.id] ?: "",
                        onValueChange = { formValues[col.id] = it },
                        onDatePickerRequest = { datePickerColumnId = col.id }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Save button ──
            Button(
                onClick = { onSave(formValues.toMap()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEditing) "保存修改" else "添加记录",
                    fontSize = 15.sp
                )
            }
        }
    }

    // ── Date picker dialog within form ──
    val dpColId = datePickerColumnId
    if (dpColId != null) {
        FormDatePickerDialog(
            currentValue = formValues[dpColId] ?: "",
            onDismiss = { datePickerColumnId = null },
            onConfirm = { value ->
                formValues[dpColId] = value
                datePickerColumnId = null
            }
        )
    }
}

// ══════════════════════════════════════════════════
// Form field item (renders appropriate input per type)
// ══════════════════════════════════════════════════

@Composable
private fun FormFieldItem(
    column: ColumnDefEntity,
    value: String,
    onValueChange: (String) -> Unit,
    onDatePickerRequest: () -> Unit
) {
    val colors = AppTheme.colors
    val colType = ColumnType.fromString(column.type)

    when (colType) {
        ColumnType.DATE -> {
            // Date field: read-only text field with calendar icon, clickable
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text(column.name) },
                    placeholder = { Text("点击选择日期", color = colors.textTertiary) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "选择日期",
                            tint = colors.accentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = colors.accentBlue,
                        unfocusedBorderColor = colors.border,
                        focusedLabelColor = colors.accentBlue,
                        unfocusedLabelColor = colors.textSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                // Transparent overlay to capture clicks
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { onDatePickerRequest() }
                )
            }
        }

        ColumnType.CURRENCY -> {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                        onValueChange(newValue)
                    }
                },
                singleLine = true,
                label = { Text(column.name) },
                placeholder = { Text("输入金额", color = colors.textTertiary) },
                prefix = { Text("¥", color = colors.textSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.accentBlue,
                    focusedBorderColor = colors.accentBlue,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.accentBlue,
                    unfocusedLabelColor = colors.textSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        ColumnType.PERCENTAGE -> {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                        onValueChange(newValue)
                    }
                },
                singleLine = true,
                label = { Text(column.name) },
                placeholder = { Text("输入百分比", color = colors.textTertiary) },
                suffix = { Text("%", color = colors.textSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.accentBlue,
                    focusedBorderColor = colors.accentBlue,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.accentBlue,
                    unfocusedLabelColor = colors.textSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        ColumnType.NUMBER -> {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                        onValueChange(newValue)
                    }
                },
                singleLine = true,
                label = { Text(column.name) },
                placeholder = { Text("输入数值", color = colors.textTertiary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.accentBlue,
                    focusedBorderColor = colors.accentBlue,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.accentBlue,
                    unfocusedLabelColor = colors.textSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        ColumnType.TEXT -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                label = { Text(column.name) },
                placeholder = { Text("输入内容", color = colors.textTertiary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.accentBlue,
                    focusedBorderColor = colors.accentBlue,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.accentBlue,
                    unfocusedLabelColor = colors.textSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ══════════════════════════════════════════════════
// Date picker dialog (used within row form)
// ══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDatePickerDialog(
    currentValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val colors = AppTheme.colors
    val initialMillis = try {
        val parts = currentValue.split("/")
        if (parts.size == 3) {
            val sdf = SimpleDateFormat("yyyy/M/d", Locale.getDefault())
            sdf.parse(currentValue)?.time
        } else null
    } catch (e: Exception) {
        null
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val sdf = SimpleDateFormat("yyyy/M/d", Locale.getDefault())
                        onConfirm(sdf.format(Date(millis)))
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("确定", color = colors.accentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = colors.textSecondary)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// ══════════════════════════════════════════════════
// Rename sheet dialog
// ══════════════════════════════════════════════════

@Composable
private fun RenameSheetDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val colors = AppTheme.colors
    var name by rememberSaveable { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        title = {
            Text(
                text = "重命名数据表",
                color = colors.textPrimary
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("输入名称", color = colors.textTertiary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.accentBlue,
                    focusedBorderColor = colors.accentBlue,
                    unfocusedBorderColor = colors.border
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("确定", color = if (name.isNotBlank()) colors.accentBlue else colors.textTertiary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = colors.textSecondary)
            }
        }
    )
}

// ══════════════════════════════════════════════════
// Column manage bottom sheet
// ══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnManageSheet(
    columns: List<ColumnDefEntity>,
    onDismiss: () -> Unit,
    onAddColumn: (name: String, type: String, groupName: String?, groupColor: String?, isNegativeRed: Boolean) -> Unit,
    onUpdateColumn: (ColumnDefEntity) -> Unit,
    onDeleteColumn: (Long) -> Unit,
    onReorderColumns: (List<ColumnDefEntity>) -> Unit
) {
    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var showColumnEditDialog by remember { mutableStateOf(false) }
    var editingColumn by remember { mutableStateOf<ColumnDefEntity?>(null) }

    val reorderableColumns = remember(columns) { mutableStateListOf(*columns.toTypedArray()) }

    LaunchedEffect(columns) {
        reorderableColumns.clear()
        reorderableColumns.addAll(columns)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardSurface,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.textTertiary.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "管理列",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(
                    items = reorderableColumns,
                    key = { _, col -> col.id }
                ) { index, column ->
                    ColumnManageItem(
                        column = column,
                        canMoveUp = index > 0,
                        canMoveDown = index < reorderableColumns.size - 1,
                        onMoveUp = {
                            if (index > 0) {
                                val temp = reorderableColumns[index]
                                reorderableColumns[index] = reorderableColumns[index - 1]
                                reorderableColumns[index - 1] = temp
                                onReorderColumns(reorderableColumns.toList())
                            }
                        },
                        onMoveDown = {
                            if (index < reorderableColumns.size - 1) {
                                val temp = reorderableColumns[index]
                                reorderableColumns[index] = reorderableColumns[index + 1]
                                reorderableColumns[index + 1] = temp
                                onReorderColumns(reorderableColumns.toList())
                            }
                        },
                        onEdit = {
                            editingColumn = column
                            showColumnEditDialog = true
                        },
                        onDelete = { onDeleteColumn(column.id) }
                    )

                    if (index < reorderableColumns.size - 1) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = colors.border.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AddColumnButton(
                onClick = {
                    editingColumn = null
                    showColumnEditDialog = true
                }
            )
        }
    }

    if (showColumnEditDialog) {
        ColumnEditDialog(
            existingColumn = editingColumn,
            existingGroups = columns
                .filter { !it.groupName.isNullOrBlank() }
                .map { it.groupName!! to (it.groupColor ?: "") }
                .distinctBy { it.first },
            onDismiss = {
                showColumnEditDialog = false
                editingColumn = null
            },
            onConfirm = { name, type, groupName, groupColor, isNegativeRed ->
                val existing = editingColumn
                if (existing != null) {
                    onUpdateColumn(
                        existing.copy(
                            name = name,
                            type = type,
                            groupName = groupName,
                            groupColor = groupColor,
                            isNegativeRed = isNegativeRed
                        )
                    )
                } else {
                    onAddColumn(name, type, groupName, groupColor, isNegativeRed)
                }
                showColumnEditDialog = false
                editingColumn = null
            }
        )
    }
}

// ══════════════════════════════════════════════════
// Column manage list item
// ══════════════════════════════════════════════════

@Composable
private fun ColumnManageItem(
    column: ColumnDefEntity,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors
    val groupColor = if (!column.groupColor.isNullOrBlank()) {
        parseHexColor(column.groupColor)
    } else {
        colors.textTertiary
    }
    val colType = ColumnType.fromString(column.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(groupColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = column.name,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = buildString {
                if (!column.groupName.isNullOrBlank()) {
                    append(column.groupName)
                    append(" \u00B7 ")
                }
                append(colType.displayName)
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary
            )
        }
        IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "上移",
                tint = if (canMoveUp) colors.textSecondary else colors.textTertiary.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
        IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "下移",
                tint = if (canMoveDown) colors.textSecondary else colors.textTertiary.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "编辑",
                tint = colors.accentBlue,
                modifier = Modifier.size(16.dp)
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = colors.semanticRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ══════════════════════════════════════════════════
// Add column button (dashed border)
// ══════════════════════════════════════════════════

@Composable
private fun AddColumnButton(onClick: () -> Unit) {
    val colors = AppTheme.colors
    val borderColor = colors.border

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(44.dp)
            .drawBehind {
                val dashWidth = 6.dp.toPx()
                val gapWidth = 4.dp.toPx()
                val strokeWidth = 1.dp.toPx()
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, gapWidth))
                val cornerRadius = 8.dp.toPx()
                drawRoundRect(
                    color = borderColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth,
                        pathEffect = pathEffect
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                )
            }
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = colors.accentBlue,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "添加新列",
                style = MaterialTheme.typography.labelMedium,
                color = colors.accentBlue
            )
        }
    }
}

// ══════════════════════════════════════════════════
// Column edit / add dialog
// ══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnEditDialog(
    existingColumn: ColumnDefEntity?,
    existingGroups: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, groupName: String?, groupColor: String?, isNegativeRed: Boolean) -> Unit
) {
    val colors = AppTheme.colors
    val isEditing = existingColumn != null

    var name by rememberSaveable { mutableStateOf(existingColumn?.name ?: "") }
    var selectedType by rememberSaveable { mutableStateOf(existingColumn?.type ?: ColumnType.TEXT.name) }
    var groupName by rememberSaveable { mutableStateOf(existingColumn?.groupName ?: "") }
    var groupColor by rememberSaveable { mutableStateOf(existingColumn?.groupColor ?: "") }
    var isNegativeRed by rememberSaveable { mutableStateOf(existingColumn?.isNegativeRed ?: false) }
    var isNewGroup by rememberSaveable { mutableStateOf(false) }
    var newGroupName by rememberSaveable { mutableStateOf("") }

    var typeExpanded by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }

    var selectedColorIndex by remember {
        mutableIntStateOf(
            if (groupColor.isNotBlank()) {
                GroupColorPresets.indexOfFirst {
                    colorToHex(it).equals(groupColor, ignoreCase = true)
                }.coerceAtLeast(0)
            } else {
                0
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        title = {
            Text(
                text = if (isEditing) "编辑列" else "添加新列",
                color = colors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("列名称") },
                    placeholder = { Text("输入列名称", color = colors.textTertiary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        cursorColor = colors.accentBlue,
                        focusedBorderColor = colors.accentBlue,
                        unfocusedBorderColor = colors.border,
                        focusedLabelColor = colors.accentBlue,
                        unfocusedLabelColor = colors.textTertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = ColumnType.fromString(selectedType).displayName,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("数据类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.accentBlue,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = colors.accentBlue,
                            unfocusedLabelColor = colors.textTertiary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        containerColor = colors.cardSurfaceSecondary
                    ) {
                        ColumnType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(text = type.displayName, color = colors.textPrimary) },
                                onClick = {
                                    selectedType = type.name
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                val groupOptions = buildList {
                    add("无分组" to "")
                    existingGroups.forEach { (gName, gColor) -> add(gName to gColor) }
                    add("新建分组" to "__new__")
                }

                ExposedDropdownMenuBox(
                    expanded = groupExpanded,
                    onExpandedChange = { groupExpanded = it }
                ) {
                    OutlinedTextField(
                        value = when {
                            isNewGroup -> newGroupName.ifBlank { "新建分组" }
                            groupName.isNotBlank() -> groupName
                            else -> "无分组"
                        },
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("分组") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.accentBlue,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = colors.accentBlue,
                            unfocusedLabelColor = colors.textTertiary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = groupExpanded,
                        onDismissRequest = { groupExpanded = false },
                        containerColor = colors.cardSurfaceSecondary
                    ) {
                        groupOptions.forEach { (label, value) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = label,
                                        color = if (value == "__new__") colors.accentBlue else colors.textPrimary
                                    )
                                },
                                onClick = {
                                    when (value) {
                                        "" -> { isNewGroup = false; groupName = ""; groupColor = "" }
                                        "__new__" -> { isNewGroup = true; groupName = ""; newGroupName = "" }
                                        else -> { isNewGroup = false; groupName = label; groupColor = value }
                                    }
                                    groupExpanded = false
                                }
                            )
                        }
                    }
                }

                if (isNewGroup) {
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        singleLine = true,
                        label = { Text("新分组名称") },
                        placeholder = { Text("输入分组名称", color = colors.textTertiary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            cursorColor = colors.accentBlue,
                            focusedBorderColor = colors.accentBlue,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = colors.accentBlue,
                            unfocusedLabelColor = colors.textTertiary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (groupName.isNotBlank() || isNewGroup) {
                    Column {
                        Text(
                            text = "分组颜色",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            GroupColorPresets.forEachIndexed { index, presetColor ->
                                val isSelected = index == selectedColorIndex
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(presetColor)
                                        .then(
                                            if (isSelected) Modifier.border(2.dp, colors.textPrimary, CircleShape)
                                            else Modifier
                                        )
                                        .clickable {
                                            selectedColorIndex = index
                                            groupColor = colorToHex(presetColor)
                                        }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("负值标红", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        Text("负数以红色显示", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
                    }
                    Switch(
                        checked = isNegativeRed,
                        onCheckedChange = { isNegativeRed = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.accentBlue,
                            uncheckedThumbColor = colors.textTertiary,
                            uncheckedTrackColor = colors.border
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalGroupName = when {
                        isNewGroup && newGroupName.isNotBlank() -> newGroupName.trim()
                        !isNewGroup && groupName.isNotBlank() -> groupName
                        else -> null
                    }
                    val finalGroupColor = if (finalGroupName != null && groupColor.isNotBlank()) {
                        groupColor
                    } else if (finalGroupName != null) {
                        colorToHex(GroupColorPresets[selectedColorIndex])
                    } else {
                        null
                    }
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedType, finalGroupName, finalGroupColor, isNegativeRed)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("确定", color = if (name.isNotBlank()) colors.accentBlue else colors.textTertiary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = colors.textSecondary)
            }
        }
    )
}

// ══════════════════════════════════════════════════
// Utility: Convert Color to hex string
// ══════════════════════════════════════════════════

private fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}
