package com.example.doline.views.screens.store.orders

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.capitalize
import com.example.doline.data.Order
import com.example.doline.data.OrderProgress
import com.example.doline.data.OrderRepository
import com.example.doline.data.OrderStatus
import com.example.doline.data.Pricing
import com.example.doline.data.SELECTED_CURRENCY
import com.example.doline.data.Store
import com.example.doline.data.StoreRepository
import com.example.doline.groupOrdersByDate
import com.example.doline.orderStausColors
import com.example.doline.timestampToTime
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.ui.theme.successLight
import com.example.doline.views.components.AppText
import com.example.doline.views.components.DolineDatePicker
import com.example.doline.views.components.EmptyMessage
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.PriceTag
import com.example.doline.views.components.Screen
import com.example.doline.views.components.TextType
import com.example.doline.zeroed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(navController: NavController, viewModel: OrdersScreenViewModel){
    val storeId = viewModel.storeId
    val uiState by viewModel.uiState.collectAsState()
    val orders by viewModel.filteredOrders.collectAsState()
    val filter by viewModel.filterBy.collectAsState()
    val status by viewModel.orderStatus.collectAsState()

    var groupedOrders by remember { mutableStateOf<Map<String, List<Order>>?>(null)}

    LaunchedEffect(orders) {
        groupedOrders = groupOrdersByDate(orders)
    }

    Screen(
        topAppBar = {
            TopAppBar(
                title = { AppText("Orders", variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier
                    .padding(vertical = 0.dp)
                    .shadow(10.dp),
                colors = TopAppBarColors(
                    containerColor = colorScheme.background,
                    scrolledContainerColor = colorScheme.background,
                    navigationIconContentColor = colorScheme.onBackground,
                    titleContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground,
                    subtitleContentColor = colorScheme.onBackground,
                ),
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = "Search",
                            modifier = Modifier.size(IconSize.BIG),
                            tint = colorScheme.onBackground
                        )
                    }
                    DolineDatePicker(onDatePicked = {
                        viewModel.onDateSelected(it)
                    })
                }
            )
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
        ) {
            FilterBtn(
                onClick = {
                    viewModel.onFilterChanged(null)
                },
                option = "All",
                selected = filter == null
            )
            FilterBtn(
                onClick = {
                    viewModel.onStatusSelected(OrderStatus.PAID)
                },
                option = "Paid",
                selected = status == OrderStatus.PAID
            )
            FilterBtn(
                onClick = {
                    viewModel.onStatusSelected(OrderStatus.CREDIT)
                },
                option = "Credit",
                selected = status == OrderStatus.CREDIT
            )
            OrderProgress.entries.forEach { entry ->
                FilterBtn(
                    onClick = {
                        viewModel.onFilterChanged(entry)
                    },
                    option = entry.name,
                    selected = filter == entry
                )
            }
        }
        when (val state = uiState) {
            is OrdersScreenUiState.Loading -> {
                LoadingScreen()
            }

            is OrdersScreenUiState.Success -> {
                if (orders.isEmpty()) {
                    EmptyMessage("No ${filter?.name?.replace("_", " ")?.lowercase() ?: ""} orders found.")
                } else {
                    LazyColumn(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                    ) {
                        groupedOrders?.forEach { (label, orders) ->
                            item {
                                AppText(label, modifier = Modifier.padding(horizontal = Spacing.MD))
                                Spacer(Modifier.height(5.dp))
                                orders.forEach { order ->
                                    OrderItemCard(onClick = {navController.navigate("$storeId/orders/${order.fields.id}")}, order = order)
                                    Spacer(Modifier.height(2.dp))
                                }
                            }
                        }
                        item { Spacer(Modifier.height(70.dp)) }
                    }
                }
            }

            is OrdersScreenUiState.Error -> {
                ErrorMessage(message = state.message)
            }
        }
    }
}

@Composable
fun FilterBtn(onClick: () -> Unit, option: String, selected: Boolean ){
    TextButton(
        onClick = onClick,
        colors = ButtonColors(
            contentColor = if(selected) colorScheme.onPrimary else colorScheme.onSurface,
            containerColor = if(selected) colorScheme.primary else colorScheme.surface,
            disabledContentColor = colorScheme.onSurface,
            disabledContainerColor = colorScheme.surface
        )
    ) {
        AppText(
            option.replace("_", " ").capitalize(),
            variant = TextType.Small,
            color = if(selected) colorScheme.onPrimary else colorScheme.onSurface
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun OrderItemCard(order: Order, onClick: () -> Unit){
    val orderAmount = order.items.sumOf { oi -> oi.pricing.amount * oi.fields.qty }
    val creditBalance = order.fields.creditBalance
    val (containerColor, textColor) = orderStausColors(order.fields.progress)
    val statusColor = when(order.fields.status){
       OrderStatus.PAID ->{
                successLight
        }
        OrderStatus.CREDIT -> {
             colorScheme.error
        }

        OrderStatus.DRAFT -> {
            colorScheme.secondary
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(colorScheme.surface)
            .padding(Spacing.MD, Spacing.SM)

    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                AppText("#${order.fields.id.zeroed()}")
                Spacer(Modifier.width(15.dp))
                AppText(
                    order.fields.progress.name,
                    variant = TextType.Small,
                    color = textColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(Rounding.SM))
                        .background(containerColor)
                        .padding(Spacing.SM, Spacing.XXS)
                )
            }
            Spacer(Modifier.width(5.dp))
            AppText(timestampToTime(order.fields.createdAt))
        }
        Spacer(Modifier.height(5.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PriceTag(Pricing(amount = orderAmount, currency = SELECTED_CURRENCY, itemId = 0))
            Spacer(Modifier.width(5.dp))
            when (creditBalance) {
                null -> {
                    AppText(order.fields.status.name, color = statusColor, variant = TextType.LabelSmall)
                }
                0.0 -> {
                    AppText("CLEARED", color = successLight, variant = TextType.LabelSmall)
                }
                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText("Balance: ", variant = TextType.LabelSmall, color = colorScheme.error)
                        PriceTag(
                            Pricing(
                                amount = creditBalance,
                                itemId = 0,
                                currency = SELECTED_CURRENCY
                            ),
                            size = TextType.LabelSmall,
                            textColor = colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@HiltViewModel
class OrdersScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storeRepository: StoreRepository,
    private val ordersRepository: OrderRepository
): ViewModel(){
    val storeId = savedStateHandle.get<Long>("storeId")
    private val _uiState = MutableStateFlow<OrdersScreenUiState>(OrdersScreenUiState.Loading)
    val uiState: StateFlow<OrdersScreenUiState> = _uiState.asStateFlow()

    private val _store = MutableStateFlow<Store?>(null)
    val store: StateFlow<Store?> = _store.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())

    private val _filteredBy = MutableStateFlow<OrderProgress?>(null)
    val filterBy: StateFlow<OrderProgress?> = _filteredBy
    private val _orderStatus = MutableStateFlow<OrderStatus?>(null)
    val orderStatus: StateFlow<OrderStatus?> = _orderStatus

    private val _filteredOrders = MutableStateFlow(_orders.value)
    val filteredOrders: StateFlow<List<Order>> = _filteredOrders

    @RequiresApi(Build.VERSION_CODES.O)
    fun onDateSelected(date: LocalDate?){
        if (date == null){
            filter(_filteredBy.value)
            return
        }
        val dayStart = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val dayEnd = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        if (_filteredBy.value == null){
            _filteredOrders.value = _orders.value.filter { or -> or.fields.createdAt in (dayStart + 1)..<dayEnd }
        }else{
            _filteredOrders.value = _orders.value.filter { or ->
                or.fields.createdAt in (dayStart + 1)..<dayEnd && or.fields.progress == _filteredBy.value
            }
        }
    }

    init {
        viewModelScope.launch {
            launch { fetchStore() }
            launch { fetchOrders() }
        }
    }

    private fun filter(f: OrderProgress?) {
        if (_orderStatus.value == null){
            if (f == null){
                _filteredOrders.value = _orders.value
            }else{
                _filteredOrders.value = _orders.value.filter { o -> o.fields.progress == f }
            }
        }else{
            if (f == null){
                _orderStatus.value = null
                _filteredOrders.value = _orders.value
            }else{
                _filteredOrders.value = _orders.value.filter { o -> o.fields.progress == f && o.fields.status == _orderStatus.value }
            }
        }
    }

    fun onStatusSelected(status: OrderStatus?){
        _orderStatus.value = status
        if (_filteredBy.value == null){
            _filteredOrders.value = _orders.value.filter { or -> or.fields.status == status }
        }else{
            _filteredOrders.value = _orders.value.filter { or ->
                or.fields.status == status && or.fields.progress == _filteredBy.value
            }
        }
    }

    fun onFilterChanged(choice: OrderProgress?){
        _filteredBy.value = choice
        filter(choice)
    }
    suspend fun fetchStore() {
        if (storeId == null){
            _uiState.value = OrdersScreenUiState.Error("The store ID can not be null.")
            return
        }
        try {
            storeRepository.getStoreWithItems(storeId).collect {
                if(it == null){
                    _uiState.value = OrdersScreenUiState.Error("There was no data found for storeId, $storeId")
                    return@collect
                }
                _store.emit(it.store)

                _uiState.value = OrdersScreenUiState.Success
            }
        }catch (e: Exception){
            _uiState.value = OrdersScreenUiState.Error(e.message ?: "Failed to fetch store items")
        }
    }
    suspend fun fetchOrders(){
        if (storeId == null){
            return
        }
        try {
            ordersRepository.getAllOrders(storeId).collect {
                _orders.value = it
                _filteredOrders.value = it
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}

sealed class OrdersScreenUiState {
    data object Loading: OrdersScreenUiState()
    data object Success: OrdersScreenUiState()
    data class Error(val message: String): OrdersScreenUiState()
}