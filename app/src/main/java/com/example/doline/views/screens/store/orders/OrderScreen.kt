package com.example.doline.views.screens.store.orders


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.ClientRepository
import com.example.doline.data.ClientWithProfile
import com.example.doline.data.CreditPayment
import com.example.doline.data.Order
import com.example.doline.data.OrderEntity
import com.example.doline.data.OrderItem
import com.example.doline.data.OrderItemEntity
import com.example.doline.data.OrderProgress
import com.example.doline.data.OrderRepository
import com.example.doline.data.OrderStatus
import com.example.doline.data.Pricing
import com.example.doline.data.PricingDetails
import com.example.doline.data.SELECTED_CURRENCY
import com.example.doline.timestampToDateTime
import com.example.doline.ui.theme.FontSize
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.ui.theme.successLight
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.NumberInputField
import com.example.doline.views.components.PriceInputField
import com.example.doline.views.components.PriceTag
import com.example.doline.views.components.Screen
import com.example.doline.views.components.TextType
import com.example.doline.zeroed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(navController: NavController, viewModel: OrderScreenViewModel){
    val orderId = viewModel.orderId
    val storeId = viewModel.storeId
    val uiState by viewModel.uiState.collectAsState()
    val order by viewModel.order.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val openPaymentSheet by viewModel.openPaymentSheet.collectAsState()
    val completeRequested by viewModel.completeRequested.collectAsState()
    val clients by viewModel.clients.collectAsState()

    var expanded by remember {
        mutableStateOf(false)
    }
    var cancel by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf(false) }
    var returning by remember { mutableStateOf(false) }
    var returns by remember { mutableStateOf<Map<Long, OrderItemEntity>>(emptyMap()) }
    var changingClient by remember { mutableStateOf(false) }
    var viewingPayments by remember { mutableStateOf(false) }

    order?.let {
        MakePaymentSheet(
            onClose = {
                viewModel.onPaymentRequested(false)
                expanded = false
            },
            onPay = { amount ->
                viewModel.makePayment(amount)
            },
            open = openPaymentSheet,
            error = errorMessage,
            maxAmount = it.fields.creditBalance ?: 0.0
        )
        CompletedOrderSheet(
            onClose = {
                viewModel.onCompletedRequested(false)
                expanded = false
            },
            onComplete = {ord ->
                viewModel.completeOrder(ord)
            },
            order = it,
            open = completeRequested
        )
        SelectClientSheet(
            open = changingClient,
            clients = clients,
            onClose = { changingClient = false },
            onSelect = { clientId ->
                viewModel.changeClient(clientId)
                changingClient = false
            }
        )
        PaymentsSheet(
            open = viewingPayments,
            payments = it.creditPayments,
            onClose = { viewingPayments = false }
        )
        if (cancel){
            AlertDialog(
                title = {AppText("Cancel order!")},
                onDismissRequest = {
                    cancel = false
                    expanded = false
                },
                text = {AppText("Are you sure you want to cancel order #${it.fields.id.zeroed()}")},
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.cancelOrder(it.fields.copy(progress = OrderProgress.CANCELLED))
                        cancel = false
                        expanded = false
                    }) {
                        AppText("Yes", color = colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        cancel = false
                        expanded = false
                    }) {
                        AppText("Cancel")
                    }
                }
            )
        }
        if (delete){
            AlertDialog(
                title = {AppText("Delete order!")},
                onDismissRequest = {
                    delete = false
                    expanded = false
                },
                text = {AppText("Are you sure you want to delete order #${it.fields.id.zeroed()}")},
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteOrder(it.fields)
                        delete = false
                        expanded = false
                    }) {
                        AppText("Yes", color = colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        delete = false
                        expanded = false
                    }) {
                        AppText("Cancel")
                    }
                }
            )
        }
    }

    Screen(
        topAppBar = {
            TopAppBar(
                title = { AppText("Order - #${orderId?.zeroed()}", variant = TextType.Heading, maxLines = 1) },
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
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_left),
                            contentDescription = "Back",
                            modifier = Modifier.size(IconSize.BIG),
                            tint = colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (uiState is UiState.Success){
                        val order = (uiState as UiState.Success).order
                        val status = order.fields.status
                        val progress = order.fields.progress
                        val hasCreditBalance = status == OrderStatus.CREDIT && (order.fields.creditBalance ?: 0.0) > 0
                        IconButton(onClick = {expanded = true}) {
                            Icon(
                                painter = painterResource(R.drawable.more),
                                contentDescription = "Menu",
                                modifier = Modifier.size(IconSize.BIG)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(colorScheme.surface),
                            offset = DpOffset(10)
                        ) {
                            Spacer(Modifier.height(20.dp))
                            AppText("Actions", variant = TextType.Label, modifier = Modifier.padding(horizontal = 16.dp))

                            if (progress == OrderProgress.PENDING || progress == OrderProgress.DRAFT || progress == OrderProgress.READY){
                                DropdownMenuItem(
                                    text = { AppText("Complete Order") },
                                    onClick = {
                                        viewModel.onCompletedRequested(true)
                                    },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.tick), contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { AppText("Mark as Ready") },
                                    onClick = {
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.ready), contentDescription = null)
                                    }
                                )
                            }

                            if(hasCreditBalance){
                                DropdownMenuItem(
                                    text = { AppText("Make payment") },
                                    onClick = {
                                        viewModel.onPaymentRequested(true)
                                    },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.tag), contentDescription = null)
                                    }
                                )
                            }
                            if (order.fields.progress == OrderProgress.PENDING){
                                DropdownMenuItem(
                                    text = { AppText("Mark as Ready") },
                                    onClick = {
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.credit_card), contentDescription = null)
                                    }
                                )
                            }

                            if (order.fields.progress == OrderProgress.SHIPPING){
                                DropdownMenuItem(
                                    text = { AppText("Deliver order") },
                                    onClick = {
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.tag), contentDescription = null)
                                    }
                                )
                            }
                            if (order.client != null){
                                DropdownMenuItem(
                                    text = { AppText("View client") },
                                    onClick = {
                                        navController.navigate("$storeId/clients/${order.client.client.id}")
                                    },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.user), modifier = Modifier.size(IconSize.NORMAL), contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { AppText("Change client") },
                                    onClick = {
                                        changingClient = true
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.user_pen), modifier = Modifier.size(IconSize.NORMAL), contentDescription = null)
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { AppText("Add client") },
                                    onClick = {
                                        changingClient = true
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.user_add), modifier = Modifier.size(IconSize.NORMAL), contentDescription = null)
                                    }
                                )
                            }
                            if (status == OrderStatus.CREDIT){
                                DropdownMenuItem(
                                    text = { AppText("Payments") },
                                    onClick = {
                                        viewingPayments = true
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.tag), modifier = Modifier.size(IconSize.NORMAL), contentDescription = null)
                                    }
                                )
                            }

                            if (progress == OrderProgress.PENDING || progress == OrderProgress.DRAFT || progress == OrderProgress.READY){
                                DropdownMenuItem(
                                    text = { AppText("Return order") },
                                    onClick = {
                                        returning = true
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.resource_return), contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { AppText("Cancel order") },
                                    onClick = {
                                        cancel = true
                                    },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.x), modifier = Modifier.size(IconSize.NORMAL), contentDescription = null)
                                    }
                                )
                            }
                            if (progress == OrderProgress.PENDING
                                || progress == OrderProgress.DRAFT
                                || progress == OrderProgress.RETURNED
                                || progress == OrderProgress.READY
                                ||  progress == OrderProgress.CANCELLED){
                                DropdownMenuItem(
                                    text = { AppText("Delete order") },
                                    onClick = {
                                        delete = true
                                    },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.trash), contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) {
        when(val state = uiState){
            is UiState.Error ->{
                ErrorMessage(state.message)
            }
            is UiState.Loading -> {
                LoadingScreen()
            }
            is UiState.Success -> {
                val order = state.order
                Column(Modifier.fillMaxSize()) {
                    if (returning){
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.MD),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                    viewModel.returnOrder(
                                        onSuccess = { returning = false; returns = emptyMap() },
                                        onError = { Log.e("OrderScreen", it) },
                                        items = returns.values.toList()
                                    )
                            }) {
                                AppText("Return", color = colorScheme.primary)
                            }
                            TextButton(onClick = {returning = false; returns = emptyMap() }) {
                                AppText("Cancel")
                            }
                        }
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize().weight(1f).padding(0.dp, Spacing.MD)) {
                        items(order.items.size) { index ->
                            val item = order.items[index]
                            OrderItemCard(
                                onAdjust = {qty ->
                                    val id = item.fields.id
                                    returns = if (qty == 0.0){
                                        returns.minus(item.fields.id)
                                    } else {
                                        returns.plus(id to item.fields.copy(qty = qty))
                                    }
                                },
                                orderItem = item,
                                selectable = returning
                            )
                        }
                    }
                    Column(
                        Modifier.fillMaxWidth()
                            .background(colorScheme.onBackground.copy(.05f))
                            .padding(Spacing.MD)
                    ) {
                            val orderTotal = order.items.sumOf { oi -> oi.pricing.amount * oi.fields.qty }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText("Order Amount: ", variant = TextType.Heading)
                            PriceTag(Pricing(amount = orderTotal, currency = SELECTED_CURRENCY, itemId = 0), textColor = colorScheme.onBackground, size = TextType.Heading)
                        }
                        if (order.fields.status == OrderStatus.CREDIT){
                            val creditBalance = order.fields.creditBalance ?: orderTotal
                            HorizontalDivider()

                            Row(
                                Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (creditBalance > 0){
                                    AppText("Credit balance: ", variant = TextType.LabelSmall, color = colorScheme.error)
                                    PriceTag(Pricing(amount = creditBalance, currency = SELECTED_CURRENCY, itemId = 0), textColor = colorScheme.error, size = TextType.LabelSmall)
                                }else {
                                    AppText("Credit balance cleared ", variant = TextType.LabelSmall, color = successLight)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(onAdjust: (qty: Double) -> Unit, orderItem: OrderItem, selectable: Boolean = false){
    val units = when(val details = orderItem.pricing.details){
        is PricingDetails.UnitPrice -> {
            details.units
        }
        is PricingDetails.RecurringPrice -> {
            details.period
        }
        else -> {
            "Units"
        }
    }
    var selected by remember { mutableStateOf(false) }
    var returnedQty by remember { mutableDoubleStateOf(orderItem.fields.qty) }

    var modifier = Modifier.fillMaxWidth()
    if (selectable){
        modifier = modifier.clickable{
            selected = !selected;
            onAdjust(orderItem.fields.qty)
        }
    }
    if(selected){ modifier = modifier.background(colorScheme.onBackground.copy(.05f))}

    Row(
        modifier = modifier.padding(Spacing.MD, 0.dp)
    ) {
        if (selectable){
            RadioButton(selected = selected, onClick = {selected = !selected})
        }
        Column(Modifier.weight(1f)) {
            AppText(orderItem.item.name, variant = TextType.Label)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PriceTag(
                    orderItem.pricing,
                    textColor = colorScheme.onBackground.copy(.6f),
                    size = TextType.Small,
                )
                AppText("${orderItem.fields.qty} $units")
            }

            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f)) {
                    if (selected){
                        NumberInputField(
                            onChange = {qty ->
                                returnedQty = qty?.toDouble() ?: 0.0
                                onAdjust(returnedQty)
                            },
                            number = returnedQty,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        returnedQty = ( returnedQty + 1).coerceAtMost(orderItem.fields.qty)
                                        onAdjust(returnedQty)
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.plus),
                                        contentDescription = "Increase quantity",
                                        modifier = Modifier.size(IconSize.SMALL),
                                        tint = colorScheme.primary
                                    )
                                }
                            },
                            leadingIcon = {
                                IconButton(
                                    onClick = {
                                        returnedQty = ( returnedQty - 1).coerceAtLeast(0.0)
                                        onAdjust(returnedQty)
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.minus),
                                        contentDescription = "Decrease quantity",
                                        modifier = Modifier.size(IconSize.SMALL),
                                        tint = colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.height(30.dp).padding(0.dp),
                            textAlign = TextAlign.Center,
                            fontSize = FontSize.SM,
                            contentPadding = PaddingValues(16.dp, 2.dp)
                        )
                    }
                }
                Spacer(Modifier.width(Spacing.LG))
                PriceTag(
                    Pricing(
                        itemId = 0,
                        amount = orderItem.fields.qty * orderItem.pricing.amount,
                        currency = SELECTED_CURRENCY,
                    ),
                    textColor = colorScheme.onBackground,
                    size = TextType.LabelSmall,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedOrderSheet(
    onClose: () -> Unit,
    onComplete: (order: OrderEntity) -> Unit,
    open: Boolean = false,
    order: Order
){
    val sheetState = rememberModalBottomSheetState()
    var amount by remember { mutableStateOf<Double?>(null) }
    var err by remember { mutableStateOf("") }
    var alert by remember { mutableStateOf(false) }
    var cancel by remember { mutableStateOf(false) }

    val orderAmount = order.items.sumOf { i -> i.pricing.amount * (i.fields.qty - i.fields.returned) }
    var newOrder by remember { mutableStateOf(order.fields.copy(progress = OrderProgress.COMPLETED)) }

    if (alert){
        val receivedAmount = amount
        if (receivedAmount == null){
            AlertDialog(
                onDismissRequest = {
                    alert = false
                },
                title = {
                    AppText(text = "Alert")
                },
                text = {
                    AppText(text = "There is no amount received. Do you want to completed this order as a credit?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val ord = newOrder.copy(
                                creditBalance = orderAmount,
                                status = OrderStatus.CREDIT
                            )
                            onComplete(ord)
                            onClose()
                        }
                    ) {
                        AppText(text = "Yes", color = colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            alert = false
                            onClose()
                        }
                    ) {
                        AppText(text = "No")
                    }
                }
            )
        }else if (receivedAmount < orderAmount){
            val balance = orderAmount - receivedAmount
            AlertDialog(
                onDismissRequest = {
                    alert = false
                },
                title = {
                    AppText(text = "Alert")
                },
                text = {
                    AppText(text = "The amount received is less than the order amount. Do you want to offer the balance ($SELECTED_CURRENCY $balance) as a credit?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val ord = newOrder.copy(
                                creditBalance = balance,
                                status = OrderStatus.CREDIT,
                                amountReceived = receivedAmount
                            )
                            onComplete(ord)
                            onClose()
                        }
                    ) {
                        AppText(text = "Yes", color = colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            alert = false
                            onClose()
                        }
                    ) {
                        AppText(text = "No")
                    }
                }
            )
        }
    }

    if (open){
        ModalBottomSheet(
            onDismissRequest = {onClose()},
            sheetState = sheetState
        ) {
            Column(Modifier.fillMaxWidth()) {
                LazyRow(
                    Modifier.fillMaxWidth().padding(horizontal = Spacing.MD),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        AppText(
                            "Complete order",
                            variant = TextType.Heading
                        )
                    }
                    item {
                        AppText(
                            "-",
                            variant = TextType.Heading
                        )
                    }

                    item {
                        PriceTag(
                            Pricing(
                                amount = orderAmount,
                                currency = SELECTED_CURRENCY,
                                itemId = 0
                            )
                        )
                    }

                }

                HorizontalDivider()
            }
            Column(Modifier.fillMaxWidth().padding(Spacing.MD, 0.dp)) {
                Spacer(Modifier.height(20.dp))
                if (err.isNotBlank()){
                    ErrorMessage(err)
                }
                PriceInputField(
                    amount = amount,
                    currency = SELECTED_CURRENCY,
                    onChange = {
                        amount = it
                        err = ""
                    },
                    label = "Amount received"
                )
                Spacer(Modifier.height(20.dp))
                AppButton(
                    text = "Completer order",
                    onClick = {
                        val amountReceived = amount

                        if(amountReceived == null || amountReceived < orderAmount){
                            alert = true
                            return@AppButton
                        }

                        newOrder = newOrder.copy(status = OrderStatus.PAID, amountReceived = amountReceived)
                        onComplete(newOrder)
                        onClose()
                    },
                    type = ButtonType.Primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakePaymentSheet(
    onClose: () -> Unit,
    onPay: (amount: Double) -> Unit,
    open: Boolean = false,
    maxAmount: Double,
    error: String = ""
){
    val sheetState = rememberModalBottomSheetState()
    var amount by remember { mutableStateOf<Double?>(null) }
    var err by remember { mutableStateOf("") }

    LaunchedEffect(error) {
        err = error
    }
    if (open){
        ModalBottomSheet(
            onDismissRequest = {onClose()},
            sheetState = sheetState
        ) {
            Column(Modifier.fillMaxWidth()) {
                AppText(
                    "Balance: $SELECTED_CURRENCY $maxAmount",
                    variant = TextType.Heading,
                    modifier = Modifier.padding(horizontal = Spacing.MD)
                )
                HorizontalDivider()
            }
            Column(Modifier.fillMaxWidth().padding(Spacing.MD, 0.dp)) {
                Spacer(Modifier.height(20.dp))
                if (err.isNotBlank()){
                    ErrorMessage(err)
                }
                PriceInputField(
                    amount = amount,
                    currency = SELECTED_CURRENCY,
                    onChange = {
                        amount = it
                        err = ""
                    },
                    label = "Amount received"
                )
                Spacer(Modifier.height(20.dp))
                AppButton(
                    text = "Save",
                    onClick = {
                        val amountReceived = amount

                        if(amountReceived == null){
                            err = "The amount received is required."
                            return@AppButton
                        }

                        if (amountReceived > maxAmount){
                            err = "You can not pay more than the credit balance!"
                            return@AppButton
                        }

                        onPay(amountReceived)
                        if (err.isBlank()){
                            onClose()
                        }
                    },
                    type = ButtonType.Primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectClientSheet(
    open: Boolean,
    clients: List<ClientWithProfile>,
    onClose: () -> Unit,
    onSelect: (clientId: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

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
                AppText("Select client", variant = TextType.Heading)
                if (clients.isEmpty()) {
                    AppText(
                        "No clients found. Add one from the Clients screen first.",
                        color = colorScheme.onBackground.copy(.6f)
                    )
                    Spacer(Modifier.height(20.dp))
                } else {
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    ) {
                        items(clients, key = { it.client.id }) { client ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(client.client.id) }
                                    .padding(Spacing.MD, Spacing.SM),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    AppText(client.profile.fullNames ?: "Unnamed client", variant = TextType.Label)
                                    if (client.profile.phone != null) {
                                        AppText(
                                            client.profile.phone,
                                            variant = TextType.Small,
                                            color = colorScheme.onBackground.copy(.6f)
                                        )
                                    }
                                }
                                Icon(
                                    painter = painterResource(R.drawable.arrow_right),
                                    contentDescription = null,
                                    modifier = Modifier.size(IconSize.NORMAL),
                                    tint = colorScheme.onBackground.copy(.4f)
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsSheet(
    open: Boolean,
    payments: List<CreditPayment>,
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

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
                AppText("Payments", variant = TextType.Heading)
                if (payments.isEmpty()) {
                    AppText(
                        "No payments recorded yet.",
                        color = colorScheme.onBackground.copy(.6f)
                    )
                    Spacer(Modifier.height(20.dp))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.SM)) {
                        payments.sortedByDescending { it.createdAt }.forEach { payment ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(colorScheme.surface, RoundedCornerShape(Rounding.SM))
                                    .padding(Spacing.MD),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PriceTag(Pricing(amount = payment.amount, currency = SELECTED_CURRENCY, itemId = 0))
                                AppText(
                                    timestampToDateTime(payment.createdAt),
                                    variant = TextType.Small,
                                    color = colorScheme.onBackground.copy(.6f)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@HiltViewModel
class OrderScreenViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val orderRepository: OrderRepository,
    private val clientRepository: ClientRepository
): ViewModel(){
    val orderId = savedState.get<Long>("orderId")
    val storeId = savedState.get<Long>("storeId")
    private  val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    private val _order = MutableStateFlow<Order?>(null)
    private val _errorMessage = MutableStateFlow("")
    private val _openPaymentSheet = MutableStateFlow(false)
    private val _completeRequested = MutableStateFlow(false)
    private val _clients = MutableStateFlow<List<ClientWithProfile>>(emptyList())



    val order: StateFlow<Order?> = _order
    val uiState: StateFlow<UiState> = _uiState
    val errorMessage: StateFlow<String> = _errorMessage
    val openPaymentSheet: StateFlow<Boolean> = _openPaymentSheet
    val completeRequested: StateFlow<Boolean> = _completeRequested
    val clients: StateFlow<List<ClientWithProfile>> = _clients

    private suspend fun fetchOrderDetails(){
        if (orderId == null){
            _uiState.value = UiState.Error("The order ID is undefined")
            return
        }
         orderRepository.getOrderById(orderId).collect { ord ->
            _order.value = ord
            _uiState.value = UiState.Success(ord)
         }
    }

    private suspend fun fetchClients(){
        if (storeId == null) return
        try {
            clientRepository.getClients(storeId).collect { _clients.value = it }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun changeClient(clientId: Long){
        val order = _order.value?.fields ?: return
        viewModelScope.launch {
            try {
                orderRepository.editOrder(order.copy(clientId = clientId))
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    init {
        viewModelScope.launch {
            launch { fetchOrderDetails() }
            launch { fetchClients() }
        }
    }

    fun createError(str: String){
        _errorMessage.value = str
    }

    fun makePayment(amount: Double) {
        val order = _order.value?.fields

        if (order == null){
            createError("Invalid order!")
            return
        }

        viewModelScope.launch {
            try {
                orderRepository.updateCreditBalance(
                    amount = amount,
                    orderId = order.id
                )

                createError("")
            }catch (e: Exception){
                createError("An error occurred! ${e.message ?: ""}")
            }
        }
    }

    fun onPaymentRequested(open: Boolean){
        _openPaymentSheet.value = open
    }
    fun onCompletedRequested(open: Boolean){
        _completeRequested.value = open
    }

    fun completeOrder(order: OrderEntity){
        viewModelScope.launch {
            try {
                orderRepository.editOrder(order)
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    fun cancelOrder(order: OrderEntity){
        viewModelScope.launch {
            try{
                orderRepository.editOrder(order)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
    fun deleteOrder(order: OrderEntity){
        viewModelScope.launch {
            try{
                orderRepository.delete(order.id)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun returnOrder(onSuccess: () -> Unit, onError: (message: String) -> Unit, items: List<OrderItemEntity>, ){
        if (items.isEmpty()){
            return
        }
        viewModelScope.launch {
            try {
                orderRepository.returnItems(items)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Failed to process return: ${e.message}")
            }
        }
    }
}

sealed class UiState {
    data object Loading: UiState()
    data class Success(val order: Order): UiState()
    data class Error(val message: String): UiState()
}