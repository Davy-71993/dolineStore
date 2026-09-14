package com.example.doline.views.screens.store.home

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.Store
import com.example.doline.data.StoreRepository
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.Screen
import com.example.doline.views.components.TextType
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
import kotlinx.coroutines.launch
import javax.inject.Inject


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

    LaunchedEffect(storeId) {
        viewModel.getStore(storeId)
    }

    var pieData by remember {
        mutableStateOf(
            listOf(
                Pie(label = "Yarket water", data = 18.0, color = Color.Red, selectedColor = Color.Green),
                Pie(label = "Lato milk", data = 35.0, color = Color.Cyan, selectedColor = Color.Blue),
                Pie(label = "Sugar", data = 32.0, color = Color.Gray, selectedColor = Color.Yellow),
                Pie(label = "Others", data = 15.0, color = Color.Blue, selectedColor = Color.Magenta),
            )
        )
    }
    val recentOrders by remember {
        mutableStateOf(
            listOf(
                Order(
                    "Wandera Deo",
                    "UGX 120,000",
                    1008,
                    "completed"
                ),
                Order(
                    "Okello Smith",
                    "UGX 70,000",
                    1408,
                    "ready"
                ),
                Order(
                    "Wafula David",
                    "UGX 390,000",
                    1078,
                    "pending"
                ),
                Order(
                    "Ssenyonga Joseph",
                    "UGX 620,000",
                    1299,
                    "cancelled"
                ),
                Order(
                    "Nekessa Joan",
                    "UGX 1,900,000",
                    2022,
                    "completed"
                )
            )
        )
    }

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
                                    )
                                },
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
                                    AppText("UGX 240,000", variant = TextType.Brand, color = MaterialTheme.colorScheme.onPrimary)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.trending_up ),
                                            contentDescription = "growth",
                                            modifier = Modifier.size(IconSize.SMALL),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                        AppText("12.5% from last month", color = MaterialTheme.colorScheme.onPrimary)
                                    }
                                    AppButton("View full report", onClick = {
                                        navController.navigate("store/reports"){
                                            popUpTo("store/reports")
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
                                    AppText("Daily sales volume", color = MaterialTheme.colorScheme.onBackground.copy(.6f))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                    ){
                                        LineChart(
                                            modifier = Modifier.fillMaxSize(),
                                            data = remember {
                                                listOf(
                                                    Line(
                                                        label = "Sales",
                                                        values = listOf(28.0, 41.0, 5.0, 10.0, 35.0),
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
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.SM),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.SM)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(Rounding.MD)
                                        )
                                        .padding(Spacing.XL),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.XS)
                                ) {
                                    AppText("ACTIVE ORDERS")
                                    AppText(
                                        text= "8",
                                        variant = TextType.Brand,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    AppText(
                                        text= "2 Ready for pickup",
                                        color = MaterialTheme.colorScheme.onBackground.copy(.6f),
                                        variant = TextType.Small
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(Rounding.MD)
                                        )
                                        .padding(Spacing.XL),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.XS)
                                ) {
                                    AppText("TOTAL VIEWS")
                                    AppText(
                                        text = "1.8K",
                                        variant = TextType.Brand,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    AppText(
                                        text= "Peak 4:00PM today",
                                        color = MaterialTheme.colorScheme.onBackground.copy(.6f),
                                        variant = TextType.Small
                                    )
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
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                    ){
                                        PieChart(
                                            modifier = Modifier.fillMaxSize(),
                                            data = pieData,
                                            onPieClick = {
                                                println("${it.label} Clicked")
                                                val pieIndex = pieData.indexOf(it)
                                                pieData = pieData.mapIndexed { mapIndex, pie -> pie.copy(selected = pieIndex == mapIndex) }
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
                                    recentOrders.forEach { order ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surface,
                                                    shape = RoundedCornerShape(Rounding.MD)
                                                )
                                                .padding(Spacing.MD)
                                                .clickable {},
                                            verticalArrangement = Arrangement.spacedBy(Spacing.XS)
                                        ) {
                                            AppText(order.client, variant = TextType.Label)
                                            AppText(order.amount, variant = TextType.Heading, color = MaterialTheme.colorScheme.primary)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                AppText("#${order.id}", variant = TextType.Small, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                                AppText(order.status.uppercase(), variant = TextType.Small, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }


}

@HiltViewModel
class ScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storeRepository: StoreRepository
): ViewModel(){
    val storeId = savedStateHandle.get<Long>("storeId")
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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
}

sealed class UiState{
    data object Loading: UiState()
    data class Success(val store: Store?): UiState()
    data class Error(val message: String): UiState()
}

data class Order(
    val client: String,
    val amount: String,
    val id: Int,
    val status: String
)