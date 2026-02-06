package com.nickfinance.dashboard.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.nickfinance.dashboard.data.local.converter.ColumnType
import com.nickfinance.dashboard.data.local.entity.ColumnDefEntity
import com.nickfinance.dashboard.data.local.entity.DashboardCardEntity
import com.nickfinance.dashboard.data.local.entity.DataSheetEntity
import com.nickfinance.dashboard.data.model.ChartType
import com.nickfinance.dashboard.data.repository.DashboardRepository
import com.nickfinance.dashboard.data.repository.SheetRepository
import com.nickfinance.dashboard.ui.theme.AppTheme
import com.nickfinance.dashboard.ui.theme.ChartColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ══════════════════════════════════════════════════════════════════════
//  ChartConfigViewModel
// ══════════════════════════════════════════════════════════════════════

@HiltViewModel
class ChartConfigViewModel @Inject constructor(
    private val sheetRepository: SheetRepository,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    /** All available data sheets. */
    val sheets: StateFlow<List<DataSheetEntity>> = sheetRepository.getAllSheets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Columns for the currently selected sheet. */
    private val _columns = MutableStateFlow<List<ColumnDefEntity>>(emptyList())
    val columns: StateFlow<List<ColumnDefEntity>> = _columns

    /** Existing card being edited (null when creating new). */
    private val _existingCard = MutableStateFlow<DashboardCardEntity?>(null)
    val existingCard: StateFlow<DashboardCardEntity?> = _existingCard

    /** Load columns for a given sheet. */
    fun loadColumnsForSheet(sheetId: Long) {
        viewModelScope.launch {
            sheetRepository.getColumns(sheetId).collect { cols ->
                _columns.value = cols
            }
        }
    }

    /** Load an existing card for editing. */
    fun loadCard(cardId: Long) {
        viewModelScope.launch {
            val card = dashboardRepository.getCardById(cardId)
            _existingCard.value = card
        }
    }

    /** Insert or update a dashboard card. Returns true on success. */
    fun saveCard(
        existingCardId: Long?,
        title: String,
        chartType: ChartType,
        dataSheetId: Long,
        xAxisColumnId: Long?,
        selectedColumnIds: List<Long>,
        colorMapping: String?,
        cardSize: String,
        targetValue: Double?,
        currentValueColumnId: Long?,
        statColumnId: Long?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val selectedIdsStr = selectedColumnIds.joinToString(",")
            if (existingCardId != null && existingCardId > 0) {
                val existing = dashboardRepository.getCardById(existingCardId)
                if (existing != null) {
                    dashboardRepository.updateCard(
                        existing.copy(
                            title = title,
                            chartType = chartType.name,
                            dataSheetId = dataSheetId,
                            xAxisColumnId = xAxisColumnId,
                            selectedColumnIds = selectedIdsStr,
                            colorMapping = colorMapping,
                            cardSize = cardSize,
                            targetValue = targetValue,
                            currentValueColumnId = currentValueColumnId,
                            statColumnId = statColumnId,
                            statRowIndex = -1
                        )
                    )
                }
            } else {
                dashboardRepository.insertCard(
                    DashboardCardEntity(
                        title = title,
                        chartType = chartType.name,
                        dataSheetId = dataSheetId,
                        xAxisColumnId = xAxisColumnId,
                        selectedColumnIds = selectedIdsStr,
                        colorMapping = colorMapping,
                        cardSize = cardSize,
                        targetValue = targetValue,
                        currentValueColumnId = currentValueColumnId,
                        statColumnId = statColumnId,
                        statRowIndex = -1
                    )
                )
            }
            onComplete()
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  ChartConfigScreen
// ══════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChartConfigScreen(
    cardId: Long,
    initialChartType: String,
    navController: NavController,
    viewModel: ChartConfigViewModel = hiltViewModel()
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // ── State from ViewModel ──
    val sheets by viewModel.sheets.collectAsState()
    val columnsForSheet by viewModel.columns.collectAsState()
    val existingCard by viewModel.existingCard.collectAsState()

    // ── Local UI state ──
    var title by remember { mutableStateOf("新建图表") }
    var selectedTabIndex by remember { mutableIntStateOf(1) } // default to config tab
    var selectedSheetId by remember { mutableStateOf<Long?>(null) }
    var selectedChartType by remember { mutableStateOf(ChartType.fromString(initialChartType)) }
    var xAxisColumnId by remember { mutableStateOf<Long?>(null) }
    var selectedSeriesIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var cardSize by remember { mutableStateOf("FULL") }
    var targetValueText by remember { mutableStateOf("") }
    var currentValueColumnId by remember { mutableStateOf<Long?>(null) }
    var statColumnId by remember { mutableStateOf<Long?>(null) }

    val isEditing = cardId > 0

    // ── Load existing card if editing ──
    LaunchedEffect(cardId) {
        if (isEditing) {
            viewModel.loadCard(cardId)
        }
    }

    // ── Populate fields when existing card loads ──
    LaunchedEffect(existingCard) {
        existingCard?.let { card ->
            title = card.title
            selectedSheetId = card.dataSheetId
            selectedChartType = ChartType.fromString(card.chartType)
            xAxisColumnId = card.xAxisColumnId
            selectedSeriesIds = card.selectedColumnIds
                .split(",")
                .mapNotNull { it.trim().toLongOrNull() }
                .toSet()
            cardSize = card.cardSize
            targetValueText = card.targetValue?.let {
                if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
            } ?: ""
            currentValueColumnId = card.currentValueColumnId
            statColumnId = card.statColumnId
            viewModel.loadColumnsForSheet(card.dataSheetId)
        }
    }

    // ── Auto-load columns when sheet changes ──
    LaunchedEffect(selectedSheetId) {
        selectedSheetId?.let { id ->
            viewModel.loadColumnsForSheet(id)
        }
    }

    // ── Derived column lists ──
    val dateColumns = columnsForSheet.filter {
        ColumnType.fromString(it.type) == ColumnType.DATE
    }
    val numericColumns = columnsForSheet.filter {
        val ct = ColumnType.fromString(it.type)
        ct == ColumnType.CURRENCY || ct == ColumnType.NUMBER || ct == ColumnType.PERCENTAGE
    }

    // ── Auto-select first date column as x-axis ──
    LaunchedEffect(dateColumns) {
        if (xAxisColumnId == null && dateColumns.isNotEmpty()) {
            xAxisColumnId = dateColumns.first().id
        }
    }

    // ── Relevant chart types based on data ──
    val availableChartTypes = ChartType.entries.toList()

    // ══════════════════════════════════════════════════════════════════
    //  Layout
    // ══════════════════════════════════════════════════════════════════

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top bar ──
        TopBar(
            title = title,
            onTitleChange = { title = it },
            onBackClick = { navController.popBackStack() },
            onSaveClick = {
                if (selectedSheetId == null) {
                    Toast.makeText(context, "请先选择数据源", Toast.LENGTH_SHORT).show()
                    return@TopBar
                }
                val targetVal = targetValueText.toDoubleOrNull()
                viewModel.saveCard(
                    existingCardId = if (isEditing) cardId else null,
                    title = title.ifBlank { "未命名图表" },
                    chartType = selectedChartType,
                    dataSheetId = selectedSheetId!!,
                    xAxisColumnId = xAxisColumnId,
                    selectedColumnIds = selectedSeriesIds.toList(),
                    colorMapping = buildColorMappingJson(selectedSeriesIds.toList()),
                    cardSize = cardSize,
                    targetValue = targetVal,
                    currentValueColumnId = currentValueColumnId,
                    statColumnId = statColumnId,
                    onComplete = {
                        navController.popBackStack()
                    }
                )
            }
        )

        // ── Tabs ──
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = colors.cardSurface,
            contentColor = colors.textPrimary,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .height(3.dp)
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(colors.accentBlue)
                    )
                }
            },
            divider = {
                HorizontalDivider(color = colors.border, thickness = 1.dp)
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = "图表",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedTabIndex == 0) colors.accentBlue else colors.textSecondary
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        text = "配置",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedTabIndex == 1) colors.accentBlue else colors.textSecondary
                    )
                }
            )
        }

        // ── Tab content ──
        when (selectedTabIndex) {
            0 -> PreviewTab(
                title = title,
                chartType = selectedChartType
            )
            1 -> ConfigTab(
                sheets = sheets,
                selectedSheetId = selectedSheetId,
                onSheetSelected = { sheetId ->
                    selectedSheetId = sheetId
                    // Reset dependent selections on sheet change
                    xAxisColumnId = null
                    selectedSeriesIds = emptySet()
                    currentValueColumnId = null
                    statColumnId = null
                },
                dateColumns = dateColumns,
                numericColumns = numericColumns,
                xAxisColumnId = xAxisColumnId,
                onXAxisSelected = { xAxisColumnId = it },
                selectedChartType = selectedChartType,
                availableChartTypes = availableChartTypes,
                onChartTypeSelected = { selectedChartType = it },
                selectedSeriesIds = selectedSeriesIds,
                onSeriesToggle = { colId ->
                    selectedSeriesIds = if (colId in selectedSeriesIds) {
                        selectedSeriesIds - colId
                    } else {
                        selectedSeriesIds + colId
                    }
                },
                cardSize = cardSize,
                onCardSizeChange = { cardSize = it },
                targetValueText = targetValueText,
                onTargetValueChange = { targetValueText = it },
                currentValueColumnId = currentValueColumnId,
                onCurrentValueColumnSelected = { currentValueColumnId = it },
                statColumnId = statColumnId,
                onStatColumnSelected = { statColumnId = it }
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Top bar with editable title
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun TopBar(
    title: String,
    onTitleChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.cardSurface)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = colors.textPrimary
            )
        }

        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(colors.accentBlue)
        )

        TextButton(onClick = onSaveClick) {
            Text(
                text = "保存",
                style = MaterialTheme.typography.labelLarge,
                color = colors.accentBlue
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Preview tab
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun PreviewTab(
    title: String,
    chartType: ChartType
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.cardSurface)
                .border(1.dp, colors.border, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title.ifBlank { "未命名图表" },
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = chartType.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Placeholder visualization area
                Box(
                    modifier = Modifier
                        .size(120.dp, 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.cardSurfaceSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "图表预览",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Configuration tab
// ══════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConfigTab(
    sheets: List<DataSheetEntity>,
    selectedSheetId: Long?,
    onSheetSelected: (Long) -> Unit,
    dateColumns: List<ColumnDefEntity>,
    numericColumns: List<ColumnDefEntity>,
    xAxisColumnId: Long?,
    onXAxisSelected: (Long) -> Unit,
    selectedChartType: ChartType,
    availableChartTypes: List<ChartType>,
    onChartTypeSelected: (ChartType) -> Unit,
    selectedSeriesIds: Set<Long>,
    onSeriesToggle: (Long) -> Unit,
    cardSize: String,
    onCardSizeChange: (String) -> Unit,
    targetValueText: String,
    onTargetValueChange: (String) -> Unit,
    currentValueColumnId: Long?,
    onCurrentValueColumnSelected: (Long) -> Unit,
    statColumnId: Long?,
    onStatColumnSelected: (Long) -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ── Data source ──
        ConfigSectionLabel("数据源")
        Spacer(modifier = Modifier.height(6.dp))
        DropdownSelector(
            items = sheets.map { it.id to "${it.icon} ${it.name}" },
            selectedId = selectedSheetId,
            placeholder = "选择数据表",
            onSelected = { onSheetSelected(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── X-axis field ──
        ConfigSectionLabel("横轴字段")
        Spacer(modifier = Modifier.height(6.dp))
        if (dateColumns.isEmpty()) {
            ConfigHint("当前数据表无日期列")
        } else {
            DropdownSelector(
                items = dateColumns.map { it.id to it.name },
                selectedId = xAxisColumnId,
                placeholder = "选择日期列",
                onSelected = { onXAxisSelected(it) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Chart type ──
        ConfigSectionLabel("图表类型")
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableChartTypes.forEach { type ->
                val isSelected = type == selectedChartType
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) colors.accentBlueBg else colors.cardSurface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colors.accentBlue else colors.border,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onChartTypeSelected(type) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = type.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) colors.accentBlue else colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Data series (for charts that use series) ──
        if (selectedChartType != ChartType.PROGRESS && selectedChartType != ChartType.STAT_NUMBER) {
            ConfigSectionLabel("数据系列")
            Spacer(modifier = Modifier.height(6.dp))
            if (numericColumns.isEmpty()) {
                ConfigHint("当前数据表无数值列")
            } else {
                ConfigCard {
                    Column {
                        numericColumns.forEachIndexed { index, col ->
                            val colorIndex = index % ChartColors.size
                            val isChecked = col.id in selectedSeriesIds

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSeriesToggle(col.id) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onSeriesToggle(col.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = colors.accentBlue,
                                        uncheckedColor = colors.textSecondary,
                                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Color indicator
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(ChartColors[colorIndex])
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = col.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = ColumnType.fromString(col.type).displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textTertiary
                                )
                            }

                            if (index < numericColumns.lastIndex) {
                                HorizontalDivider(
                                    color = colors.border,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── Card size toggle ──
        ConfigSectionLabel("卡片大小")
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("FULL" to "整行宽", "HALF" to "半宽").forEach { (sizeValue, sizeLabel) ->
                val isSelected = cardSize == sizeValue
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) colors.accentBlueBg else colors.cardSurface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colors.accentBlue else colors.border,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onCardSizeChange(sizeValue) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sizeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) colors.accentBlue else colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── PROGRESS type specific fields ──
        if (selectedChartType == ChartType.PROGRESS) {
            ConfigSectionLabel("目标值")
            Spacer(modifier = Modifier.height(6.dp))
            ConfigCard {
                OutlinedTextField(
                    value = targetValueText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onTargetValueChange(newValue)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "输入目标数值",
                            color = colors.textTertiary
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { /* dismiss keyboard */ }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentBlue,
                        unfocusedBorderColor = colors.border,
                        cursorColor = colors.accentBlue,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ConfigSectionLabel("当前值列")
            Spacer(modifier = Modifier.height(6.dp))
            DropdownSelector(
                items = numericColumns.map { it.id to it.name },
                selectedId = currentValueColumnId,
                placeholder = "选择当前值列",
                onSelected = { onCurrentValueColumnSelected(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── STAT_NUMBER type specific fields ──
        if (selectedChartType == ChartType.STAT_NUMBER) {
            ConfigSectionLabel("关联列")
            Spacer(modifier = Modifier.height(6.dp))
            DropdownSelector(
                items = numericColumns.map { it.id to it.name },
                selectedId = statColumnId,
                placeholder = "选择统计列",
                onSelected = { onStatColumnSelected(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom padding for scroll
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Shared config composables
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun ConfigSectionLabel(text: String) {
    val colors = AppTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
        color = colors.textSecondary,
        modifier = Modifier.padding(start = 2.dp)
    )
}

@Composable
private fun ConfigHint(text: String) {
    val colors = AppTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = colors.textTertiary,
        modifier = Modifier.padding(start = 2.dp, top = 4.dp)
    )
}

@Composable
private fun ConfigCard(
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

// ══════════════════════════════════════════════════════════════════════
//  Dropdown selector
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun DropdownSelector(
    items: List<Pair<Long, String>>,
    selectedId: Long?,
    placeholder: String,
    onSelected: (Long) -> Unit
) {
    val colors = AppTheme.colors
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = items.find { it.first == selectedId }?.second

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.cardSurface)
                .clickable { expanded = true }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedLabel ?: placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedLabel != null) colors.textPrimary else colors.textTertiary
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(colors.cardSurface)
        ) {
            items.forEach { (id, label) ->
                val isCurrentlySelected = id == selectedId
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isCurrentlySelected) colors.accentBlue else colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isCurrentlySelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.accentBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Utility
// ══════════════════════════════════════════════════════════════════════

/**
 * Build a simple JSON color mapping string for selected column IDs,
 * assigning colors from [ChartColors] in order.
 */
private fun buildColorMappingJson(columnIds: List<Long>): String? {
    if (columnIds.isEmpty()) return null
    val entries = columnIds.mapIndexed { index, id ->
        val color = ChartColors[index % ChartColors.size]
        val hex = String.format("#%06X", 0xFFFFFF and color.hashCode())
        "\"$id\":\"$hex\""
    }
    return "{${entries.joinToString(",")}}"
}
