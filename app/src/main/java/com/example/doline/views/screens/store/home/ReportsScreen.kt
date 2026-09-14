package com.example.doline.views.screens.store.home


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.DeviceConfiguration
import com.example.doline.capitalize
import com.example.doline.formatWithCommas
import com.example.doline.data.CategoryEntity
import com.example.doline.data.CategoryRepository
import com.example.doline.data.ItemRepository
import com.example.doline.data.ItemWithBatches
import com.example.doline.data.Order
import com.example.doline.data.OrderProgress
import com.example.doline.data.OrderRepository
import com.example.doline.data.OrderStatus
import com.example.doline.data.Pricing
import com.example.doline.data.SELECTED_CURRENCY
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.ui.theme.successContainerLight
import com.example.doline.ui.theme.successDark
import com.example.doline.ui.theme.successLight
import com.example.doline.toDateString
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.EmptyMessage
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.PriceTag
import com.example.doline.views.components.TextType
import com.example.doline.views.components.WeeklyPerformanceChart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

private const val LOW_STOCK_THRESHOLD = 5.0
private const val OVERSTOCK_THRESHOLD = 100.0
private const val MAX_TREND_DAYS = 62

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController, viewmodel: ReportsScreenViewModel){
    val storeId = viewmodel.storeId
    val period by viewmodel.period.collectAsState()
    val customRange by viewmodel.customRange.collectAsState()
    val data by viewmodel.reportsData.collectAsState()

    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val deviceConfig = DeviceConfiguration.getWindowSizeClass(windowSizeClass)
    val statCardColumns = DeviceConfiguration.getStatCardColumnCount(deviceConfig)

    var showCustomPeriodSheet by remember { mutableStateOf(false) }

    LaunchedEffect(storeId) {
        viewmodel.observeReportsData(storeId)
    }

    CustomPeriodSheet(
        open = showCustomPeriodSheet,
        initialStart = customRange?.first,
        initialEnd = customRange?.second,
        onClose = { showCustomPeriodSheet = false },
        onApply = { start, end ->
            viewmodel.onCustomPeriodSelected(start, end)
            showCustomPeriodSheet = false
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Reports", variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier.padding(vertical = 0.dp),
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    subtitleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_left),
                            contentDescription = "Back",
                            modifier = Modifier.size(IconSize.NORMAL),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.share),
                            contentDescription = "Share",
                            Modifier.size(IconSize.NORMAL)
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.download),
                            contentDescription = "Download",
                            Modifier.size(IconSize.NORMAL)
                        )
                    }
                }
            )
        }
    ) {
            innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.MD),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        AppText("This Week", variant = TextType.Label,
                            modifier= Modifier
                                .padding(start = Spacing.SM)
                                .background(
                                    if (period == ReportPeriod.THISWEEK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.FULL)
                                )
                                .padding(horizontal = Spacing.MD, vertical = Spacing.XXS)
                                .clickable { viewmodel.onPeriodSelected(ReportPeriod.THISWEEK) },
                            color = if(period == ReportPeriod.THISWEEK) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    item {
                        AppText("This Month", variant = TextType.Label,
                            modifier= Modifier
                                .padding(start = Spacing.SM)
                                .background(
                                    if (period == ReportPeriod.THISMONTH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.FULL)
                                )
                                .padding(horizontal = Spacing.MD, vertical = Spacing.XXS)
                                .clickable { viewmodel.onPeriodSelected(ReportPeriod.THISMONTH) },
                            color = if(period == ReportPeriod.THISMONTH) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    item {
                        AppText("Custom Period", variant = TextType.Label,
                            modifier= Modifier
                                .padding(start = Spacing.SM)
                                .background(
                                    if (period == ReportPeriod.CUSTOM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.FULL)
                                )
                                .padding(horizontal = Spacing.MD, vertical = Spacing.XXS)
                                .clickable { showCustomPeriodSheet = true },
                            color = if(period == ReportPeriod.CUSTOM) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(Rounding.MD)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.SM),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        AppText("Sales Trend")
                        if (data.trendRangeTooLong){
                            Box(
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AppText(
                                    "Selected range is too long to show a daily trend.",
                                    color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                                )
                            }
                        } else if (data.trendValues.isEmpty() || data.trendValues.all { it == 0.0 }){
                            Box(
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AppText(
                                    "No sales recorded for this period.",
                                    color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                                )
                            }
                        } else {
                            WeeklyPerformanceChart(labels = data.trendLabels, amounts = data.trendValues)
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                ) {
                    ResponsiveStatGrid(
                        columns = statCardColumns,
                        cards = listOf(
                            {
                                StatCard(
                                    label = "Total Sales",
                                    valueContent = {
                                        PriceTag(
                                            Pricing(amount = data.totalSales, currency = SELECTED_CURRENCY, itemId = 0),
                                            size = TextType.Heading
                                        )
                                    }
                                ) {
                                    ChangeBadge(data.salesChangePercent)
                                }
                            },
                            {
                                StatCard(
                                    label = "Total Orders",
                                    value = "${data.totalOrders}"
                                ) {
                                    ChangeBadge(data.ordersChangePercent)
                                }
                            },
                            {
                                StatCard(
                                    label = "Avg. Order Value",
                                    valueContent = {
                                        PriceTag(
                                            Pricing(amount = data.avgOrderValue, currency = SELECTED_CURRENCY, itemId = 0),
                                            size = TextType.Heading
                                        )
                                    }
                                ) {
                                    AppText(
                                        "vs ${data.prevAvgOrderValue.formatAsAmount()} last period",
                                        variant = TextType.Small,
                                        color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                                    )
                                }
                            },
                            {
                                StatCard(
                                    label = "Profit",
                                    valueContent = {
                                        PriceTag(
                                            Pricing(amount = data.profit, currency = SELECTED_CURRENCY, itemId = 0),
                                            size = TextType.Heading
                                        )
                                    }
                                ) {
                                    AppText(
                                        "${"%.1f".format(data.profitMarginPercent)}% margin",
                                        variant = TextType.Small,
                                        color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                                    )
                                    if (data.missingBuyingPriceBatchCount > 0) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.XXS),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.warning),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(IconSize.SMALL)
                                            )
                                            AppText(
                                                "${data.missingBuyingPriceBatchCount} batch(es) missing cost price — margin may be overstated",
                                                variant = TextType.Small,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            },
                            {
                                StatCard(
                                    label = "Units Sold",
                                    value = data.unitsSold.formatQuantity(),
                                    centered = true
                                ) {
                                    ChangeBadge(data.unitsSoldChangePercent)
                                    AppText(
                                        "Quantity sold across all orders",
                                        variant = TextType.Small,
                                        color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                                    )
                                }
                            },
                            {
                                StatCard(
                                    label = "Stock Turn Over",
                                    value = "${"%.1f".format(data.stockTurnover)}x",
                                    centered = true
                                ) {
                                    AppText(
                                        "Units sold ÷ units in stock",
                                        variant = TextType.Small,
                                        color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                                    )
                                }
                            },
                        )
                    )
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(Rounding.MD)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.SM),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        AppText("Sales By Category")
                        if (data.salesByCategory.isEmpty()){
                            EmptyMessage("No sales recorded for this period.")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.SM)) {
                                data.salesByCategory.forEachIndexed { index, category ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        AppText(category.name, variant = TextType.Label)
                                        AppText("${"%.1f".format(category.percentage)}%", variant = TextType.Label, color = MaterialTheme.colorScheme.primary)
                                        AppText("$SELECTED_CURRENCY ${category.amount.formatQuantity()}", variant = TextType.Label, color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (index < data.salesByCategory.lastIndex) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(Rounding.MD)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.SM),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        AppText("Top Selling Products")
                        if (data.topProducts.isEmpty()){
                            EmptyMessage("No sales recorded for this period.")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.SM)) {
                                data.topProducts.forEachIndexed { index, product ->
                                    Row {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.secondary,
                                                    shape = RoundedCornerShape(Rounding.SM)
                                                )
                                                .size(40.dp)
                                                .align(Alignment.CenterVertically)
                                                .padding(Spacing.XXS),
                                        ){
                                            AppText(
                                                text = (index + 1).toString(),
                                                variant = TextType.Label,
                                                color = MaterialTheme.colorScheme.onSecondary,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                        Column(modifier = Modifier
                                            .padding(start = Spacing.SM)
                                            .weight(1f)) {
                                            AppText(
                                                text = product.name,
                                                variant = TextType.Label,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1
                                            )
                                            AppText(product.categoryName, variant = TextType.Small, color = MaterialTheme.colorScheme.onBackground.copy(.6f))
                                        }
                                        Column(modifier = Modifier
                                            .padding(start = Spacing.SM)
                                            .weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.XXS)) {
                                            AppText("$SELECTED_CURRENCY ${product.amount.formatQuantity()}", variant = TextType.Label)
                                            LinearProgressIndicator(
                                                progress = { product.progress },
                                                gapSize = 0.dp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        AppText("Inventory Health", variant = TextType.Label)

                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.MD)) {
                            listOf(
                                "Low Stock" to data.lowStockCount,
                                "Out of Stock" to data.outOfStockCount,
                                "Overstocked" to data.overstockedCount
                            ).forEach { (label, count) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(Rounding.SM)
                                        )
                                        .padding(Spacing.MD),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                                ) {
                                    AppText(
                                        label,
                                        variant = TextType.Small,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onBackground.copy(.7f)
                                    )
                                    AppText(
                                        "$count",
                                        variant = TextType.Heading,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.MD)) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(Rounding.SM)
                                    )
                                    .padding(Spacing.MD),
                                verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                            ) {
                                AppText(
                                    "Total Value",
                                    variant = TextType.Small,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onBackground.copy(.7f)
                                )
                                AppText(
                                    "$SELECTED_CURRENCY ${data.inventoryValue.formatQuantity()}",
                                    variant = TextType.Heading,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(Rounding.SM)
                                    )
                                    .padding(Spacing.MD),
                                verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                            ) {
                                AppText(
                                    "Turn Over",
                                    variant = TextType.Small,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onBackground.copy(.7f)
                                )
                                AppText(
                                    "${"%.1f".format(data.stockTurnover)}x",
                                    variant = TextType.Heading,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(Rounding.MD),
                        border = BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            AppText(
                                "Need Restocking",
                                variant = TextType.Label,
                                modifier = Modifier.padding(Spacing.MD)
                            )

                            if (data.needsRestocking.isEmpty()){
                                AppText(
                                    "Nothing running low right now.",
                                    color = MaterialTheme.colorScheme.onBackground.copy(.6f),
                                    modifier = Modifier.padding(horizontal = Spacing.MD, vertical = Spacing.SM)
                                )
                            } else {
                                data.needsRestocking.forEach { restock ->
                                    HorizontalDivider()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Spacing.MD)
                                    ) {
                                        AppText(
                                            restock.name,
                                            variant = TextType.Label,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.primary.copy(.7f)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AppText(
                                                "${restock.available.formatQuantity()} ${restock.units} left.",
                                                color = if (restock.available <= 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground.copy(.7f)
                                            )
                                            AppButton(
                                                "Reorder",
                                                onClick = { navController.navigate("${viewmodel.storeId}/inventory/restock/${restock.itemId}") },
                                                type = ButtonType.Primary,
                                                modifier = Modifier
                                                    .width(140.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(Rounding.MD),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(Spacing.MD),
                            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ai),
                                    contentDescription = "AI insights",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(IconSize.BIG)
                                )
                                AppText(
                                    "Performance Insights",
                                    variant = TextType.Heading,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                            ) {
                                InsightCard(
                                    label = "Credit Sales",
                                    value = "${"%.1f".format(data.creditSalesRatioPercent)}%",
                                    changePercent = data.creditSalesRatioChangePercent,
                                    modifier = Modifier.weight(1f)
                                )
                                InsightCard(
                                    label = "Avg. Cart Size",
                                    value = "${"%.1f".format(data.avgCartSize)} Items",
                                    changePercent = data.avgCartSizeChangePercent,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ResponsiveStatGrid(columns: Int, cards: List<@Composable () -> Unit>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.MD)) {
        cards.chunked(columns).forEach { rowCards ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.MD)) {
                rowCards.forEach { card ->
                    Box(modifier = Modifier.weight(1f)) { card() }
                }
                repeat(columns - rowCards.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    centered: Boolean = false,
    valueContent: (@Composable () -> Unit)? = null,
    footer: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(Spacing.XXS, shape = RoundedCornerShape(Rounding.MD))
            .background(
                MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(Rounding.MD)
            )
            .padding(Spacing.MD),
        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
    ) {
        AppText(
            text = label,
            variant = TextType.Label,
            color = MaterialTheme.colorScheme.onBackground.copy(.6f)
        )
        val alignment = if (centered) Modifier.align(Alignment.CenterHorizontally) else Modifier
        if (valueContent != null) {
            Box(alignment) { valueContent() }
        } else if (value != null) {
            AppText(
                text = value,
                variant = TextType.Heading,
                color = MaterialTheme.colorScheme.primary,
                modifier = alignment
            )
        }
        footer()
    }
}

@Composable
private fun ChangeBadge(percent: Double?) {
    if (percent == null) return
    val positive = percent >= 0
    Row(
        modifier = Modifier
            .background(
                if (positive) successContainerLight else MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(Rounding.FULL)
            )
            .padding(horizontal = Spacing.SM, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(if (positive) R.drawable.trending_up else R.drawable.trending_down),
            contentDescription = "change",
            tint = if (positive) successLight else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(IconSize.SMALL)
        )
        AppText(
            "${if (positive) "+" else ""}${"%.1f".format(percent)}%",
            variant = TextType.Small,
            color = if (positive) successLight else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun InsightCard(label: String, value: String, changePercent: Double?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(.5f)
        ),
        shape = RoundedCornerShape(Rounding.MD),
    ){
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.MD),
            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
        ) {
            AppText(
                label,
                variant = TextType.Small,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1
            )
            AppText(
                value,
                variant = TextType.Heading,
                color = MaterialTheme.colorScheme.onPrimary
            )
            if (changePercent != null) {
                val positive = changePercent >= 0
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.XXS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(if (positive) R.drawable.arrow_up else R.drawable.arrow_down),
                        contentDescription = if (positive) "increase" else "decrease",
                        modifier = Modifier.size(IconSize.SMALL),
                        tint = if (positive) successDark else MaterialTheme.colorScheme.error
                    )
                    AppText(
                        "${"%.1f".format(kotlin.math.abs(changePercent))}% vs last period",
                        variant = TextType.Small,
                        color = if (positive) successDark else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomPeriodSheet(
    open: Boolean,
    initialStart: LocalDate?,
    initialEnd: LocalDate?,
    onClose: () -> Unit,
    onApply: (start: LocalDate, end: LocalDate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var startDate by remember(open, initialStart) { mutableStateOf(initialStart) }
    var endDate by remember(open, initialEnd) { mutableStateOf(initialEnd) }
    var error by remember(open) { mutableStateOf("") }

    if (open) {
        ModalBottomSheet(
            onDismissRequest = onClose,
            sheetState = sheetState
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD, 0.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.MD)
            ) {
                AppText("Custom Period", variant = TextType.Heading)
                if (error.isNotBlank()) {
                    ErrorMessage(error)
                }
                DateFormField(
                    label = "Start date",
                    date = startDate,
                    onDateSelected = { startDate = it; error = "" }
                )
                DateFormField(
                    label = "End date",
                    date = endDate,
                    onDateSelected = { endDate = it; error = "" }
                )
                Spacer(Modifier.height(10.dp))
                AppButton(
                    text = "Apply",
                    onClick = {
                        val start = startDate
                        val end = endDate
                        if (start == null || end == null) {
                            error = "Please select both a start and an end date."
                            return@AppButton
                        }
                        if (end.isBefore(start)) {
                            error = "The end date can not be before the start date."
                            return@AppButton
                        }
                        onApply(start, end)
                    },
                    type = ButtonType.Primary
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFormField(label: String, date: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        AppText(label, variant = TextType.LabelSmall, color = MaterialTheme.colorScheme.onBackground.copy(.6f))
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(.5f), RoundedCornerShape(Rounding.SM))
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(.2f), RoundedCornerShape(Rounding.SM))
                .clickable { showPicker = true }
                .padding(Spacing.MD, Spacing.SM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                date?.toDateString() ?: "Select date",
                color = if (date != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(.6f)
            )
            Icon(
                painter = painterResource(R.drawable.calendar_today),
                contentDescription = null,
                modifier = Modifier.size(IconSize.NORMAL),
                tint = MaterialTheme.colorScheme.onBackground.copy(.6f)
            )
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDate = date ?: LocalDate.now())
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.getSelectedDate()?.let { onDateSelected(it) }
                    showPicker = false
                }) {
                    AppText("Ok", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    AppText("Cancel")
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

private fun Double.formatQuantity(): String = this.formatWithCommas()

private fun Double.formatAsAmount(): String = "$SELECTED_CURRENCY ${this.formatQuantity()}"

@HiltViewModel
class ReportsScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository
): ViewModel(){
    val storeId = savedStateHandle.get<Long>("storeId")

    private val _period = MutableStateFlow(ReportPeriod.THISWEEK)
    val period: StateFlow<ReportPeriod> = _period.asStateFlow()

    private val _customRange = MutableStateFlow<Pair<LocalDate, LocalDate>?>(null)
    val customRange: StateFlow<Pair<LocalDate, LocalDate>?> = _customRange.asStateFlow()

    private val _reportsData = MutableStateFlow(ReportsData())
    val reportsData: StateFlow<ReportsData> = _reportsData.asStateFlow()

    private var observing = false

    fun onPeriodSelected(p: ReportPeriod) {
        _period.value = p
    }

    fun onCustomPeriodSelected(start: LocalDate, end: LocalDate) {
        _customRange.value = start to end
        _period.value = ReportPeriod.CUSTOM
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun observeReportsData(storeId: Long?) {
        if (storeId == null || observing) return
        observing = true
        viewModelScope.launch {
            try {
                val sourceFlow = combine(
                    orderRepository.getAllOrders(storeId),
                    itemRepository.getAllItems(storeId),
                    categoryRepository.getAllCategories()
                ) { orders, items, categories -> ReportsSourceData(orders, items, categories) }

                combine(sourceFlow, _period, _customRange) { source, period, customRange ->
                    buildReportsData(source.orders, source.items, source.categories, period, customRange)
                }.collect {
                    _reportsData.value = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

enum class ReportPeriod {
   THISWEEK, THISMONTH, CUSTOM
}

private data class ReportsSourceData(
    val orders: List<Order>,
    val items: List<ItemWithBatches>,
    val categories: List<CategoryEntity>
)

data class CategorySales(val name: String, val amount: Double, val percentage: Double)
data class ProductSales(val name: String, val categoryName: String, val amount: Double, val progress: Float)
data class RestockItem(val itemId: Long, val name: String, val available: Double, val units: String)

data class ReportsData(
    val totalSales: Double = 0.0,
    val salesChangePercent: Double? = null,
    val totalOrders: Int = 0,
    val ordersChangePercent: Double? = null,
    val avgOrderValue: Double = 0.0,
    val prevAvgOrderValue: Double = 0.0,
    val profit: Double = 0.0,
    val profitMarginPercent: Double = 0.0,
    val missingBuyingPriceBatchCount: Int = 0,
    val unitsSold: Double = 0.0,
    val unitsSoldChangePercent: Double? = null,
    val stockTurnover: Double = 0.0,
    val trendLabels: List<String> = emptyList(),
    val trendValues: List<Double> = emptyList(),
    val trendRangeTooLong: Boolean = false,
    val salesByCategory: List<CategorySales> = emptyList(),
    val topProducts: List<ProductSales> = emptyList(),
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val overstockedCount: Int = 0,
    val inventoryValue: Double = 0.0,
    val needsRestocking: List<RestockItem> = emptyList(),
    val creditSalesRatioPercent: Double = 0.0,
    val creditSalesRatioChangePercent: Double? = null,
    val avgCartSize: Double = 0.0,
    val avgCartSizeChangePercent: Double? = null,
)

private data class DateRange(val start: LocalDate, val end: LocalDate)

@RequiresApi(Build.VERSION_CODES.O)
private fun resolveRange(period: ReportPeriod, customRange: Pair<LocalDate, LocalDate>?, zone: ZoneId): DateRange {
    val today = LocalDate.now(zone)
    return when (period) {
        ReportPeriod.THISWEEK -> DateRange(today.minusDays(6), today)
        ReportPeriod.THISMONTH -> DateRange(today.withDayOfMonth(1), today)
        ReportPeriod.CUSTOM -> {
            val range = customRange
            if (range != null) DateRange(range.first, range.second) else DateRange(today, today)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun DateRange.previous(): DateRange {
    val days = ChronoUnit.DAYS.between(start, end) + 1
    val prevEnd = start.minusDays(1)
    val prevStart = prevEnd.minusDays(days - 1)
    return DateRange(prevStart, prevEnd)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun DateRange.epochBounds(zone: ZoneId): Pair<Long, Long> {
    val s = start.atStartOfDay(zone).toInstant().toEpochMilli()
    val e = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return s to e
}

private fun percentChange(from: Double, to: Double): Double? {
    if (from <= 0.0) return null
    return ((to - from) / from) * 100
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildReportsData(
    orders: List<Order>,
    items: List<ItemWithBatches>,
    categories: List<CategoryEntity>,
    period: ReportPeriod,
    customRange: Pair<LocalDate, LocalDate>?
): ReportsData {
    val zone = ZoneId.systemDefault()
    val range = resolveRange(period, customRange, zone)
    val prevRange = range.previous()
    val (start, end) = range.epochBounds(zone)
    val (prevStart, prevEnd) = prevRange.epochBounds(zone)

    fun Order.revenue(): Double = this.items.sumOf { it.pricing.amount * it.fields.qty }
    fun Order.unitsSold(): Double = this.items.sumOf { it.fields.qty }

    val counted = orders.filter { it.fields.progress != OrderProgress.DRAFT && it.fields.progress != OrderProgress.CANCELLED }
    val periodOrders = counted.filter { it.fields.createdAt in start..<end }
    val prevPeriodOrders = counted.filter { it.fields.createdAt in prevStart..<prevEnd }

    val totalSales = periodOrders.sumOf { it.revenue() }
    val prevSales = prevPeriodOrders.sumOf { it.revenue() }

    val totalOrders = periodOrders.size
    val prevOrdersCount = prevPeriodOrders.size

    val avgOrderValue = if (totalOrders > 0) totalSales / totalOrders else 0.0
    val prevAvgOrderValue = if (prevOrdersCount > 0) prevSales / prevOrdersCount else 0.0

    val unitsSold = periodOrders.sumOf { it.unitsSold() }
    val prevUnitsSold = prevPeriodOrders.sumOf { it.unitsSold() }

    // buyingPrice on a batch is the total cost of the whole batch quantity, not a per-unit price,
    // so the per-unit cost has to be derived: total buying price ÷ total batch quantity.
    val unitCostByBatchId: Map<Long, Double> = items
        .flatMap { it.batches }
        .mapNotNull { b ->
            val buyingPrice = b.batch.buyingPrice
            val batchQuantity = b.batch.quantity
            if (buyingPrice != null && batchQuantity > 0) b.batch.id to (buyingPrice / batchQuantity) else null
        }
        .toMap()

    fun com.example.doline.data.OrderItem.cost(): Double = fields.batchesDetails.entries.sumOf { (key, qty) ->
        val batchId = key.removePrefix("BAT").toLongOrNull()
        val unitCost = batchId?.let { unitCostByBatchId[it] } ?: 0.0
        unitCost * qty
    }

    val cogs = periodOrders.flatMap { it.items }.sumOf { it.cost() }
    val profit = totalSales - cogs
    val profitMarginPercent = if (totalSales > 0) (profit / totalSales) * 100 else 0.0

    val missingBuyingPriceBatchCount = periodOrders
        .flatMap { it.items }
        .flatMap { it.fields.batchesDetails.keys }
        .mapNotNull { it.removePrefix("BAT").toLongOrNull() }
        .distinct()
        .count { batchId -> unitCostByBatchId[batchId] == null }

    val totalAvailableUnits = items.sumOf { iwb -> iwb.batches.sumOf { it.batch.available } }
    val stockTurnover = if (totalAvailableUnits > 0) unitsSold / totalAvailableUnits else 0.0

    val daysInRange = (ChronoUnit.DAYS.between(range.start, range.end) + 1).toInt()
    val trendLabels: List<String>
    val trendValues: List<Double>
    if (daysInRange > MAX_TREND_DAYS) {
        trendLabels = emptyList()
        trendValues = emptyList()
    } else {
        val sameCalendarMonth = range.start.year == range.end.year && range.start.month == range.end.month
        val pattern = when {
            daysInRange <= 7 -> "EEE"
            sameCalendarMonth -> "d"
            else -> "d MMM"
        }
        val formatter = DateTimeFormatter
            .ofPattern(pattern)
            .withLocale(Locale.getDefault())
        trendLabels = (0 until daysInRange).map { range.start.plusDays(it.toLong()).format(formatter) }
        trendValues = (0 until daysInRange).map { offset ->
            val day = range.start.plusDays(offset.toLong())
            val dayStart = day.atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEnd = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            periodOrders.filter { it.fields.createdAt in dayStart..<dayEnd }.sumOf { it.revenue() }
        }
    }

    val categoryNameBySlug = categories.associate { it.slug to it.name }
    fun categoryName(slug: String) = categoryNameBySlug[slug] ?: slug.replace("_", " ").capitalize()

    val salesByCategoryRaw = periodOrders
        .flatMap { it.items }
        .groupBy { it.item.categorySlug }
        .map { (slug, orderItems) -> slug to orderItems.sumOf { it.pricing.amount * it.fields.qty } }
        .sortedByDescending { it.second }
    val totalCategorySales = salesByCategoryRaw.sumOf { it.second }
    val salesByCategory = salesByCategoryRaw.take(5).map { (slug, amount) ->
        CategorySales(
            name = categoryName(slug),
            amount = amount,
            percentage = if (totalCategorySales > 0) (amount / totalCategorySales) * 100 else 0.0
        )
    }

    val topProductsRaw = periodOrders
        .flatMap { it.items }
        .groupBy { Pair(it.item.name, it.item.categorySlug) }
        .map { (key, orderItems) -> Triple(key.first, key.second, orderItems.sumOf { it.pricing.amount * it.fields.qty }) }
        .sortedByDescending { it.third }
        .take(5)
    val maxProductAmount = topProductsRaw.maxOfOrNull { it.third } ?: 0.0
    val topProducts = topProductsRaw.map { (name, slug, amount) ->
        ProductSales(
            name = name,
            categoryName = categoryName(slug),
            amount = amount,
            progress = if (maxProductAmount > 0) (amount / maxProductAmount).toFloat() else 0f
        )
    }

    fun ItemWithBatches.totalAvailable() = batches.sumOf { it.batch.available }
    val lowStockCount = items.count { it.totalAvailable() in 0.01..LOW_STOCK_THRESHOLD }
    val outOfStockCount = items.count { it.totalAvailable() <= 0.0 }
    val overstockedCount = items.count { it.totalAvailable() >= OVERSTOCK_THRESHOLD }
    val inventoryValue = items.sumOf { iwb -> iwb.batches.sumOf { it.batch.available * (unitCostByBatchId[it.batch.id] ?: 0.0) } }
    val needsRestocking = items
        .filter { it.totalAvailable() in 0.0..LOW_STOCK_THRESHOLD }
        .sortedBy { it.totalAvailable() }
        .take(5)
        .map { iwb ->
            RestockItem(
                itemId = iwb.item.id,
                name = iwb.item.name,
                available = iwb.totalAvailable(),
                units = iwb.batches.firstOrNull()?.batch?.units ?: "units"
            )
        }

    fun creditRatio(list: List<Order>): Double {
        if (list.isEmpty()) return 0.0
        return (list.count { it.fields.status == OrderStatus.CREDIT }.toDouble() / list.size) * 100
    }
    fun avgCart(list: List<Order>): Double {
        if (list.isEmpty()) return 0.0
        return list.sumOf { o -> o.items.sumOf { it.fields.qty } } / list.size
    }

    val creditSalesRatio = creditRatio(periodOrders)
    val prevCreditSalesRatio = creditRatio(prevPeriodOrders)
    val avgCartSize = avgCart(periodOrders)
    val prevAvgCartSize = avgCart(prevPeriodOrders)

    return ReportsData(
        totalSales = totalSales,
        salesChangePercent = percentChange(prevSales, totalSales),
        totalOrders = totalOrders,
        ordersChangePercent = percentChange(prevOrdersCount.toDouble(), totalOrders.toDouble()),
        avgOrderValue = avgOrderValue,
        prevAvgOrderValue = prevAvgOrderValue,
        profit = profit,
        profitMarginPercent = profitMarginPercent,
        missingBuyingPriceBatchCount = missingBuyingPriceBatchCount,
        unitsSold = unitsSold,
        unitsSoldChangePercent = percentChange(prevUnitsSold, unitsSold),
        stockTurnover = stockTurnover,
        trendLabels = trendLabels,
        trendValues = trendValues,
        trendRangeTooLong = daysInRange > MAX_TREND_DAYS,
        salesByCategory = salesByCategory,
        topProducts = topProducts,
        lowStockCount = lowStockCount,
        outOfStockCount = outOfStockCount,
        overstockedCount = overstockedCount,
        inventoryValue = inventoryValue,
        needsRestocking = needsRestocking,
        creditSalesRatioPercent = creditSalesRatio,
        creditSalesRatioChangePercent = percentChange(prevCreditSalesRatio, creditSalesRatio),
        avgCartSize = avgCartSize,
        avgCartSizeChangePercent = percentChange(prevAvgCartSize, avgCartSize)
    )
}
