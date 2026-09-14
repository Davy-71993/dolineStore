package com.example.doline.views.screens.store.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.DeviceConfiguration
import com.example.doline.R
import com.example.doline.capitalize
import com.example.doline.data.ClientRepository
import com.example.doline.data.Order
import com.example.doline.data.OrderProgress
import com.example.doline.data.OrderRepository
import com.example.doline.data.Pricing
import com.example.doline.data.SELECTED_CURRENCY
import com.example.doline.data.Store
import com.example.doline.data.StoreRepository
import com.example.doline.orderStausColors
import com.example.doline.timestampToDate
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.EmptyMessage
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.PriceTag
import com.example.doline.views.components.Screen
import com.example.doline.views.components.TextType
import com.example.doline.zeroed
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.Pie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDashBoardScreen(
    navController: NavController,
    viewModel: ScreenViewModel = hiltViewModel(),
    storeId: Long?
){
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentDestination?.route
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val uiState by viewModel.uiState.collectAsState()
    val stats by viewModel.dashboardStats.collectAsState()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfig = DeviceConfiguration.getWindowSizeClass(windowSizeClass)
    val singleColumnSummary = deviceConfig == DeviceConfiguration.MOBILE_PORTRAIT

    LaunchedEffect(storeId) {
        viewModel.getStore(storeId)
        viewModel.observeDashboardData(storeId)
    }

    val pieData = remember(stats.topSellingItems) { buildTopSellingPieData(stats.topSellingItems) }

    when(val state = uiState){
        is UiState.Loading -> {
            LoadingScreen()
        }

        is UiState.Error -> {
            ErrorMessage(message = state.message)
        }

        is UiState.Success -> {
            val store = state.store ?: return
            ModalNavigationDrawer(
                drawerState= drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.background,
                        drawerTonalElevation = Spacing.MD
                    ) {
                        AppText(
                            text= "Doline Store",
                            variant = TextType.Heading,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .fillMaxWidth()
                                .padding(Spacing.XL)
                        )
                        HorizontalDivider()
                        Column(
                            modifier = Modifier
                                .padding(Spacing.MD)
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 80.dp)
                        ) {
                            NavigationDrawerItem(
                                label = { AppText("Dashboard") },
                                onClick = {
                                    navController.navigate("store"){
                                        popUpTo("store")
                                    }
                                    scope.launch { drawerState.close() }
                                },
                                selected = currentRoute == "store",
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.dashboard ),
                                        contentDescription = "Dashboard",
                                        modifier = Modifier.size(IconSize.BIG)
                                    ) },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            NavigationDrawerItem(
                                label = { AppText("Inbox") },
                                onClick = {
                                    navController.navigate("store/$storeId/inbox"){
                                        popUpTo("store/{storeId}/inbox")
                                    }
                                    scope.launch { drawerState.close() }
                                },
                                selected = currentRoute == "store/$storeId/inbox",
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.mail ),
                                        contentDescription = "Inbox",
                                        modifier = Modifier.size(IconSize.BIG)
                                    )
                                }
                            )
                            NavigationDrawerItem(
                                label = { AppText("Reports") },
                                onClick = {navController.navigate("store/$storeId/reports"){popUpTo("store/$storeId/reports")}
                                    scope.launch { drawerState.close() }},
                                selected = currentRoute == "store/$storeId/reports",
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.reports),
                                        contentDescription = "Reports",
                                        modifier = Modifier.size(IconSize.BIG)
                                    )
                                }
                            )
                            NavigationDrawerItem(
                                label = { AppText("Expenses") },
                                onClick = { navController.navigate("store/$storeId/expenses"){popUpTo("store/$storeId/expenses")}
                                    scope.launch { drawerState.close() }},
                                selected = currentRoute == "store/$storeId/expenses",
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.expences),
                                        contentDescription = "Expenses",
                                        modifier = Modifier.size(IconSize.BIG)
                                    )
                                }
                            )
                            NavigationDrawerItem(
                                label = { AppText("Notes") },
                                onClick = { navController.navigate("store/$storeId/notes"){popUpTo("store/$storeId/expenses")}
                                    scope.launch { drawerState.close() }},
                                selected = currentRoute == "store/$storeId/notes",
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.notes),
                                        contentDescription = "Notes",
                                        modifier = Modifier.size(IconSize.BIG)
                                    )
                                }
                            )
                            NavigationDrawerItem(
                                label = { AppText("Clients") },
                                onClick = { navController.navigate("$storeId/clients"){popUpTo("$storeId/clients")}
                                    scope.launch { drawerState.close() }},
                                selected = currentRoute == "$storeId/clients",
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.user),
                                        contentDescription = "Clients",
                                        modifier = Modifier.size(IconSize.BIG)
                                    )
                                }
                            )
                            NavigationDrawerItem(
                                label = { AppText("Suppliers") },
                                onClick = { navController.navigate("$storeId/suppliers"){popUpTo("$storeId/suppliers")}
                                    scope.launch { drawerState.close() }},
                                selected = currentRoute == "$storeId/suppliers",
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.logistics),
                                        contentDescription = "Suppliers",
                                        modifier = Modifier.size(IconSize.BIG)
                                    )
                                }
                            )
                            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onBackground)
                            NavigationDrawerItem(
                                label = { AppText("Settings") },
                                onClick = { navController.navigate("store/$storeId/settings"){popUpTo("store/$storeId/settings")}
                                    scope.launch { drawerState.close() }},
                                selected = currentRoute == "store/$storeId/settings",
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.settings),
                                        contentDescription = "Settings",
                                        modifier = Modifier.size(IconSize.BIG)
                                    )
                                }
                            )
                            NavigationDrawerItem(
                                label = { AppText("Help and feedback") },
                                onClick = { navController.navigate("store/$storeId/help"){popUpTo("store/$storeId/settings")}
                                    scope.launch { drawerState.close() }},
                                selected = currentRoute == "store/$storeId/help",
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.help),
                                        contentDescription = "Help and feedback",
                                        modifier = Modifier.size(IconSize.BIG)
                                    )
                                }
                            )
                            NavigationDrawerItem(
                                label = { AppText("Lilli") },
                                onClick = { navController.navigate("$storeId/lilli"){popUpTo("$storeId/lilli")}
                                    scope.launch { drawerState.close() }},
                                selected = currentRoute == "$storeId/lilli",
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ai),
                                        contentDescription = "Ai assistant",
                                        modifier = Modifier.size(IconSize.BIG)
                                    )
                                }
                            )
                        }
                    }
                }
            ) {
                Screen (
                    topAppBar = {
                        TopAppBar(
                            title = { AppText(store.name, variant = TextType.Heading, maxLines = 1) },
                            modifier = Modifier.shadow(10.dp),
                            colors = TopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                scrolledContainerColor = MaterialTheme.colorScheme.background,
                                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                                subtitleContentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                            scrollBehavior = scrollBehavior,
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Menu,
                                        contentDescription = "Menu",
                                        modifier = Modifier.size(IconSize.NORMAL)
                                    )
                                }
                            }
                        )
                    }
                ) {
                    LazyColumn(modifier = Modifier
                        .fillMaxSize(),
                        contentPadding = PaddingValues(vertical = Spacing.MD),
                        verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.XXS, horizontal = Spacing.SM)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(Rounding.MD)
                                    )
                            ){
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.XL),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                                ) {
                                    AppText("TOTAL REVENUE", color = MaterialTheme.colorScheme.onPrimary)
                                    PriceTag(
                                        Pricing(amount = stats.totalRevenue, currency = SELECTED_CURRENCY, itemId = 0),
                                        textColor = MaterialTheme.colorScheme.onPrimary,
                                        size = TextType.Brand
                                    )
                                    val growth = stats.revenueGrowthPercent
                                    if (growth != null){
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(
                                                    id = if (growth >= 0) R.drawable.trending_up else R.drawable.trending_down
                                                ),
                                                contentDescription = "growth",
                                                modifier = Modifier.size(IconSize.SMALL),
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                            AppText(
                                                "${if (growth >= 0) "+" else ""}${"%.1f".format(growth)}% from last month",
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                    AppButton("View full report", onClick = {
                                        navController.navigate("store/$storeId/reports"){
                                            popUpTo("store/$storeId/reports")
                                        }
                                    })
                                }

                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.XXS, horizontal = Spacing.SM)
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
                                    AppText("PERFORMANCE TREND")
                                    AppText("Daily sales volume, past 7 days", color = MaterialTheme.colorScheme.onBackground.copy(.6f))
                                    if (stats.dailyRevenue.all { it == 0.0 }){
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(120.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppText("No sales recorded in the past week.", color = MaterialTheme.colorScheme.onBackground.copy(.6f))
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(300.dp)
                                        ){
                                            LineChart(
                                                modifier = Modifier.fillMaxSize(),
                                                data = remember(stats.dailyRevenue) {
                                                    listOf(
                                                        Line(
                                                            label = "Sales",
                                                            values = stats.dailyRevenue,
                                                            color = SolidColor(Color(0xFF9E4300)),
                                                            firstGradientFillColor = Color(0xFF9E4300).copy(alpha = .5f),
                                                            secondGradientFillColor = Color.Transparent,
                                                            strokeAnimationSpec = tween(
                                                                2000,
                                                                easing = EaseInOutCubic
                                                            ),
                                                            gradientAnimationDelay = 1000,
                                                            drawStyle = DrawStyle.Stroke(width = 2.dp),
                                                        )
                                                    )
                                                },
                                                animationMode = AnimationMode.Together(delayBuilder = {
                                                    it * 500L
                                                }),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            if (singleColumnSummary) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.SM),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                                ) {
                                    ActiveOrdersSummaryCard(stats, modifier = Modifier.fillMaxWidth())
                                    TotalClientsSummaryCard(stats, modifier = Modifier.fillMaxWidth())
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.SM),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.SM)
                                ) {
                                    ActiveOrdersSummaryCard(stats, modifier = Modifier.weight(1f))
                                    TotalClientsSummaryCard(stats, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.XXS, horizontal = Spacing.SM)
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
                                    AppText("TOP SELLING ITEMS")
                                    if (pieData.isEmpty()){
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(120.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppText("No sales recorded yet.", color = MaterialTheme.colorScheme.onBackground.copy(.6f))
                                        }
                                    } else {
                                        var pieState by remember(pieData) { mutableStateOf(pieData) }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(260.dp)
                                        ){
                                            PieChart(
                                                modifier = Modifier.fillMaxSize(),
                                                data = pieState,
                                                onPieClick = {
                                                    val pieIndex = pieState.indexOf(it)
                                                    pieState = pieState.mapIndexed { mapIndex, pie -> pie.copy(selected = pieIndex == mapIndex) }
                                                },
                                                selectedScale = 1.2f,
                                                scaleAnimEnterSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                ),
                                                colorAnimEnterSpec = tween(300),
                                                colorAnimExitSpec = tween(300),
                                                scaleAnimExitSpec = tween(300),
                                                spaceDegreeAnimExitSpec = tween(300),
                                                selectedPaddingDegree = 4f,
                                                style = Pie.Style.Stroke(width = 40.dp),
                                            )
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
                                            pieState.forEach { pie ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(10.dp)
                                                                .clip(CircleShape)
                                                                .background(pie.color)
                                                        )
                                                        Spacer(modifier = Modifier.width(Spacing.SM))
                                                        AppText(pie.label ?: "", variant = TextType.Small)
                                                    }
                                                    AppText(
                                                        pie.data.formatQuantity(),
                                                        variant = TextType.Small,
                                                        color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                                                    )
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
                                    .padding(vertical = Spacing.XXS, horizontal = Spacing.SM)
                            ){
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                                ) {
                                    AppText("RECENT ORDERS")
                                    if (stats.recentOrders.isEmpty()){
                                        EmptyMessage("No orders yet.")
                                    } else {
                                        stats.recentOrders.forEach { order ->
                                            RecentOrderCard(
                                                order = order,
                                                onClick = { navController.navigate("$storeId/orders/${order.fields.id}") }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(Modifier.height(60.dp))
                        }
                    }
                }
            }
        }

    }


}

@Composable
private fun ActiveOrdersSummaryCard(stats: DashboardStats, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(Rounding.MD)
            )
            .padding(Spacing.XL),
        verticalArrangement = Arrangement.spacedBy(Spacing.XS)
    ) {
        AppText("ACTIVE ORDERS")
        AppText(
            text = "${stats.activeOrdersCount}",
            variant = TextType.Brand,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        AppText(
            text = "${stats.readyOrdersCount} Ready for pickup",
            color = MaterialTheme.colorScheme.onBackground.copy(.6f),
            variant = TextType.Small
        )
    }
}

@Composable
private fun TotalClientsSummaryCard(stats: DashboardStats, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(Rounding.MD)
            )
            .padding(Spacing.XL),
        verticalArrangement = Arrangement.spacedBy(Spacing.XS)
    ) {
        AppText("TOTAL CLIENTS")
        AppText(
            text = "${stats.totalClients}",
            variant = TextType.Brand,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        AppText(
            text = "${stats.newClientsThisWeek} new this week",
            color = MaterialTheme.colorScheme.onBackground.copy(.6f),
            variant = TextType.Small
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun RecentOrderCard(order: Order, onClick: () -> Unit) {
    val orderAmount = order.items.sumOf { it.pricing.amount * it.fields.qty }
    val (containerColor, textColor) = orderStausColors(order.fields.progress)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(Rounding.MD)
            )
            .padding(Spacing.MD)
            .clickable { onClick() },
        verticalArrangement = Arrangement.spacedBy(Spacing.XS)
    ) {
        AppText(order.client?.name ?: "Walk-in customer", variant = TextType.Label)
        PriceTag(
            Pricing(amount = orderAmount, currency = SELECTED_CURRENCY, itemId = 0),
            textColor = MaterialTheme.colorScheme.primary,
            size = TextType.Heading
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                "#${order.fields.id.zeroed()} • ${timestampToDate(order.fields.createdAt)}",
                variant = TextType.Small,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            AppText(
                order.fields.progress.name.replace("_", " ").capitalize(),
                variant = TextType.LabelSmall,
                color = textColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(Rounding.SM))
                    .background(containerColor)
                    .padding(Spacing.SM, Spacing.XXS)
            )
        }
    }
}

private fun Double.formatQuantity(): String {
    return if (this == this.toLong().toDouble()) this.toLong().toString() else "%.1f".format(this)
}

private val TOP_SELLING_PALETTE = listOf(
    Color(0xFF2A78D6) to Color(0xFF2260AB), // blue
    Color(0xFFEB6834) to Color(0xFFBC532A), // orange
    Color(0xFF1BAF7A) to Color(0xFF168C62), // aqua
    Color(0xFFEDA100) to Color(0xFFBE8100), // yellow
)
private val TOP_SELLING_OTHERS_COLOR = Color(0xFF898781) to Color(0xFF6E6C67)

private fun buildTopSellingPieData(items: List<TopSellingItem>): List<Pie> {
    if (items.isEmpty()) return emptyList()
    val top = items.take(TOP_SELLING_PALETTE.size)
    val rest = items.drop(TOP_SELLING_PALETTE.size)

    val pies = top.mapIndexed { index, item ->
        val (color, selectedColor) = TOP_SELLING_PALETTE[index]
        Pie(label = item.name, data = item.quantity, color = color, selectedColor = selectedColor)
    }.toMutableList()

    if (rest.isNotEmpty()) {
        val (color, selectedColor) = TOP_SELLING_OTHERS_COLOR
        pies += Pie(
            label = "Others",
            data = rest.sumOf { it.quantity },
            color = color,
            selectedColor = selectedColor
        )
    }
    return pies
}

@HiltViewModel
class ScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storeRepository: StoreRepository,
    private val orderRepository: OrderRepository,
    private val clientRepository: ClientRepository
): ViewModel(){
    val storeId = savedStateHandle.get<Long>("storeId")
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()

    private var dashboardObserved = false

    fun getStore(storeId: Long?){
        if (storeId == null){
            _uiState.value = UiState.Error("The Store ID is null.")
            return
        }

        viewModelScope.launch {
            try {
                storeRepository.getStoreById(storeId).collect {
                    _uiState.value = UiState.Success(it)
                }
            }catch (e: Exception){
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred!")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun observeDashboardData(storeId: Long?){
        if (storeId == null || dashboardObserved) return
        dashboardObserved = true
        viewModelScope.launch {
            try {
                combine(
                    orderRepository.getAllOrders(storeId),
                    clientRepository.getClients(storeId)
                ) { orders, clients ->
                    val zone = ZoneId.systemDefault()
                    val weekAgo = LocalDate.now(zone).minusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
                    val newClientsThisWeek = clients.count { it.createdAt >= weekAgo }
                    buildDashboardStats(orders, clients.size, newClientsThisWeek)
                }.collect {
                    _dashboardStats.value = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class TopSellingItem(
    val name: String,
    val quantity: Double
)

data class DashboardStats(
    val totalRevenue: Double = 0.0,
    val revenueGrowthPercent: Double? = null,
    val dailyRevenue: List<Double> = List(7) { 0.0 },
    val activeOrdersCount: Int = 0,
    val readyOrdersCount: Int = 0,
    val totalClients: Int = 0,
    val newClientsThisWeek: Int = 0,
    val topSellingItems: List<TopSellingItem> = emptyList(),
    val recentOrders: List<Order> = emptyList()
)

@RequiresApi(Build.VERSION_CODES.O)
private fun buildDashboardStats(orders: List<Order>, totalClients: Int, newClientsThisWeek: Int): DashboardStats {
    fun Order.revenue(): Double = items.sumOf { it.pricing.amount * it.fields.qty }

    val countedOrders = orders.filter {
        it.fields.progress != OrderProgress.DRAFT && it.fields.progress != OrderProgress.CANCELLED
    }

    val totalRevenue = countedOrders.sumOf { it.revenue() }

    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val startOfThisMonth = today.withDayOfMonth(1)
    val startOfLastMonth = startOfThisMonth.minusMonths(1)

    fun epochOf(date: LocalDate) = date.atStartOfDay(zone).toInstant().toEpochMilli()

    val thisMonthRevenue = countedOrders
        .filter { it.fields.createdAt >= epochOf(startOfThisMonth) }
        .sumOf { it.revenue() }
    val lastMonthRevenue = countedOrders
        .filter { it.fields.createdAt >= epochOf(startOfLastMonth) && it.fields.createdAt < epochOf(startOfThisMonth) }
        .sumOf { it.revenue() }

    val growth = if (lastMonthRevenue > 0) ((thisMonthRevenue - lastMonthRevenue) / lastMonthRevenue) * 100 else null

    val dailyRevenue = (6 downTo 0).map { offset ->
        val day = today.minusDays(offset.toLong())
        val dayStart = epochOf(day)
        val dayEnd = epochOf(day.plusDays(1))
        countedOrders.filter { it.fields.createdAt in dayStart..<dayEnd }.sumOf { it.revenue() }
    }

    val activeOrdersCount = orders.count {
        it.fields.progress == OrderProgress.PENDING ||
            it.fields.progress == OrderProgress.READY ||
            it.fields.progress == OrderProgress.SHIPPING
    }
    val readyOrdersCount = orders.count { it.fields.progress == OrderProgress.READY }

    val topSellingItems = countedOrders
        .flatMap { it.items }
        .groupBy { it.item.name }
        .map { (name, orderItems) -> TopSellingItem(name, orderItems.sumOf { it.fields.qty }) }
        .sortedByDescending { it.quantity }

    val recentOrders = orders.sortedByDescending { it.fields.createdAt }.take(5)

    return DashboardStats(
        totalRevenue = totalRevenue,
        revenueGrowthPercent = growth,
        dailyRevenue = dailyRevenue,
        activeOrdersCount = activeOrdersCount,
        readyOrdersCount = readyOrdersCount,
        totalClients = totalClients,
        newClientsThisWeek = newClientsThisWeek,
        topSellingItems = topSellingItems,
        recentOrders = recentOrders
    )
}

sealed class UiState{
    data object Loading: UiState()
    data class Success(val store: Store?): UiState()
    data class Error(val message: String): UiState()
}
