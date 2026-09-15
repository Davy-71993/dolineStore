package com.example.doline.views.components



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.doline.R
import com.example.doline.data.CartItem
import com.example.doline.data.ClientWithProfile
import com.example.doline.data.Currency
import com.example.doline.data.OrderProgress
import com.example.doline.data.Pricing
import com.example.doline.data.PricingDetails
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing

sealed class ClientSelection {
    data object None : ClientSelection()
    data class Existing(val client: ClientWithProfile) : ClientSelection()
    data class New(val name: String, val phone: String?, val address: String?) : ClientSelection()
}

@Composable
fun ShoppingCart(
    onCompleteSale: (progress: OrderProgress) -> Unit = {},
    onClose: () -> Unit,
    onCartItemChanged: (CartItem, Int) -> Unit,
    onDelete: (CartItem) -> Unit = {},
    onClear: () -> Unit = {},
    items: List<CartItem>,
    deviceSize: DeviceSize = DeviceSize.MOBILE,
    onReceivedAmountChanged: (Double?) -> Unit,
    receivedAmount: Double? = null,
    error: String? = null,
    onAddClient: (ClientSelection) -> Unit,
    clients: List<ClientWithProfile>,
    storeId: Long?,
    client: ClientWithProfile? = null
){
    storeId ?: return // Return nothing if the storeId is null.

    var cartTotal by remember { mutableDoubleStateOf(0.0) }
    var change by remember { mutableDoubleStateOf(0.0) }
    var expanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(items) {
        cartTotal = items.sumOf { i -> (i.cartItem.qty * i.pricing.amount) }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .consumeWindowInsets(PaddingValues(bottom = 80.dp))
            .imePadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Spacing.XXS, Spacing.XXS),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (deviceSize == DeviceSize.MOBILE){
                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_left),
                        contentDescription = "close shopping cart",
                        modifier = Modifier.size(IconSize.NORMAL),
                        tint = colorScheme.onBackground
                    )
                }
            }
            AppText(
                text= client?.profile?.fullNames ?: "Shopping cart",
                variant = TextType.Heading,
                maxLines = 1
            )
            Spacer(Modifier.weight(1f))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl){
                IconButton(onClick = {expanded = true}) {
                    Icon(
                        painter = painterResource(R.drawable.more),
                        contentDescription = "Cart Actions",
                        modifier = Modifier.size(IconSize.BIG)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(colorScheme.surface)
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr){
                        Spacer(Modifier.height(20.dp))
                        AppText("Cart Actions", variant = TextType.Label, modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp))
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(10.dp))
                        if (client != null){
                            DropdownMenuItem(
                                text = {
                                    TextButton(
                                        onClick = {
                                            onAddClient(ClientSelection.None)
                                            expanded = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorScheme.background
                                        ),
                                        contentPadding = PaddingValues(horizontal = 18.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Icon(painter = painterResource(R.drawable.user_minus), tint = colorScheme.onBackground, contentDescription = null)
                                        Spacer(Modifier.width(10.dp))
                                        AppText("Remove Client", color = colorScheme.onBackground)
                                    }
                                },
                                onClick = {}
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                AddClientForm(
                                    onClose = {expanded = false},
                                    onSelect = onAddClient,
                                    storeId = storeId,
                                    clients = clients,
                                    title = if (client == null) "Add Client" else "Change Client"
                                )
                            },
                            onClick = {}
                        )
                        DropdownMenuItem(
                            text = {
                                ClearCartDialog {
                                    expanded = false
                                    onClear()
                                }
                            },
                            onClick = {}
                        )

                    }
                }
            }

        }
        HorizontalDivider()
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = Spacing.MD)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.SM)
        ) {
            if (!error.isNullOrBlank()){
                AppText(error, color = colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            items.forEachIndexed { index, item ->
                CartItem(
                    { n ->
                        onCartItemChanged(n, index)
                    },
                    {
                        onDelete(item)
                    },
                    cartItemDraft = item
                )
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(colorScheme.primary.copy(.1f))
        ) {

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText("Total", color = colorScheme.primary, variant = TextType.Heading)
                PriceTag(
                    Pricing(itemId = 0, amount = cartTotal, currency = Currency.UGX),
                    size = TextType.Heading,
                )
            }
            HorizontalDivider()
            Column(Modifier
                .fillMaxWidth()
                .padding(Spacing.MD)) {
                PriceInputField(
                    currency = Currency.UGX,
                    amount = receivedAmount,
                    onChange = { am ->
                        onReceivedAmountChanged(am)
                        change = am?.minus(cartTotal) ?: 0.0
                    },
                    label = "",
                    placeholder = "Amount received"
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppText("Change:", color = colorScheme.onBackground.copy(.6f), variant = TextType.Heading)
                    PriceTag(
                        Pricing(itemId = 0, amount = change, currency = Currency.UGX),
                        textColor = colorScheme.onBackground.copy(.6f),
                        size = TextType.Heading,
                    )
                }
            }
            HorizontalDivider()
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        onCompleteSale(OrderProgress.DRAFT)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondary
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AppText("Save as draft", color = colorScheme.onSecondary)
                }
                Spacer(Modifier.width(10.dp))
                TextButton(
                    onClick = {
                        onCompleteSale(OrderProgress.COMPLETED)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    AppText("Complete Sale", color = colorScheme.onPrimary)
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun CartItem(onChange: (CartItem) -> Unit = {}, onDelete: () -> Unit, cartItemDraft: CartItem){
    val qtyUnits = when(val d = cartItemDraft.pricing.details){
        is PricingDetails.UnitPrice -> {
            "${d.units ?: "Unit"}s"
        }
        is PricingDetails.RecurringPrice -> {
            "${d.period}S"
        }
        else -> {
            cartItemDraft.item.sku ?: ""
        }
    }
    val p = cartItemDraft.pricing
    val qty = cartItemDraft.cartItem.qty

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.MD)
    ) {
        AppText(cartItemDraft.item.name, variant = TextType.Label)
        FlowRow(Modifier.fillMaxWidth()) {
            cartItemDraft.cartItem.specs?.forEach { pair->
                Row(Modifier.wrapContentSize()) {
                    AppText(" | ")
                    AppText(pair.key)
                    AppText(" - ${pair.value} ")
                }
            }
        }
        PriceTag(
            p,
            textColor = colorScheme.onBackground.copy(.6f),
            size = TextType.Small,
        )
        Spacer(Modifier.height(Spacing.MD))
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val newQty = (qty - 1).coerceAtLeast(0.0)
                        val nci = cartItemDraft.copy(cartItem = cartItemDraft.cartItem.copy(qty = newQty))
                        onChange(nci)
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.minus),
                        contentDescription = "Decrease quantity",
                        modifier = Modifier.size(IconSize.NORMAL),
                        tint = colorScheme.primary
                    )
                }
                AppText(
                    "${cartItemDraft.cartItem.qty} $qtyUnits",
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colorScheme.onBackground.copy(.1f))
                        .padding(10.dp, 4.dp)
                )
                IconButton(
                    onClick = {
                        val newQty = ( qty + 1).coerceAtMost(cartItemDraft.cartItem.maxQty)
                        val nci = cartItemDraft.copy(cartItem = cartItemDraft.cartItem.copy(qty = newQty))
                        onChange(nci)
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.plus),
                        contentDescription = "Increase quantity",
                        modifier = Modifier.size(IconSize.NORMAL),
                        tint = colorScheme.primary
                    )
                }
            }
            DeleteCartItemDialog(onDelete, cartItemDraft)
        }

        HorizontalDivider()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(Modifier.wrapContentSize()){
                PriceTag(
                    Pricing(
                        itemId = 0,
                        amount = qty * p.amount,
                        currency = Currency.UGX
                    ),
                    textColor = colorScheme.onBackground.copy(.8f),
                    size = TextType.Body,
                )
            }
        }
    }
}

@Composable
fun ClearCartDialog(action: () -> Unit){
    var openClearCartDialog by remember { mutableStateOf(false) }
    TextButton(
        onClick = {
            openClearCartDialog = true
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.errorContainer
        ),
        contentPadding = PaddingValues(horizontal = 18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(painter = painterResource(R.drawable.trash), tint = colorScheme.error, contentDescription = null)
        Spacer(Modifier.width(10.dp))
        AppText("Clear cart", color = colorScheme.error)
    }
    if (openClearCartDialog) {
        AlertDialog(
            onDismissRequest = {
            },
            title = {
                AppText(text = "Clear cart?")
            },
            text = {
                AppText(text = "Are you sure, you want to delete all the items in the cart? This action can not be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        action()
                        openClearCartDialog = false
                    }
                ) {
                    AppText(text = "Clear cart", color = colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { openClearCartDialog = false }
                ) {
                    AppText(text = "Cancel")
                }
            }
        )
    }
}

@Composable
fun DeleteCartItemDialog(action: () -> Unit, item: CartItem){
    var openDeleteCartItemDialog by remember { mutableStateOf(false) }
    IconButton(
        onClick = {openDeleteCartItemDialog = true },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = colorScheme.surface
        )
    ) {
        Icon(
            painter = painterResource(R.drawable.trash),
            contentDescription = "Delete cart item",
            modifier = Modifier.size(IconSize.SMALL),
            tint = colorScheme.onSurface
        )
    }
    if (openDeleteCartItemDialog) {
        AlertDialog(
            onDismissRequest = {
            },
            title = {
                AppText(text = "Delete Item?")
            },
            text = {
                AppText(text = "Are you sure, you want to delete ${item.item.name} from this cart?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        action()
                        openDeleteCartItemDialog = false
                    }
                ) {
                    AppText(text = "Delete", color = colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { openDeleteCartItemDialog = false }
                ) {
                    AppText(text = "Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClientForm(
    onClose: () -> Unit,
    onSelect: (ClientSelection) -> Unit,
    clients: List<ClientWithProfile> = emptyList(),
    storeId: Long,
    title: String = "Add Client"
){
    var open by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    TextButton(
        onClick = {
            open = true
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.background
        ),
        contentPadding = PaddingValues(horizontal = 18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(painter = painterResource(R.drawable.user_add), tint = colorScheme.onBackground, contentDescription = null)
        Spacer(Modifier.width(10.dp))
        AppText(title, color = colorScheme.onBackground)
    }

    var isNew by remember { mutableStateOf(clients.isEmpty()) }

    if (open){
        ModalBottomSheet(
            onDismissRequest = { open = false },
            sheetState = sheetState,
        ) {
            Row(
                Modifier.fillMaxWidth().padding(Spacing.MD, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText("Add Client", variant = TextType.Heading)
                IconButton(
                    onClick = {isNew = !isNew},
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.primary
                    )
                ){
                    Icon(
                        painter = painterResource(if (!isNew) R.drawable.plus else R.drawable.x),
                        contentDescription = null,
                        modifier = Modifier.size(IconSize.NORMAL)
                    )
                }
            }
            LazyColumn(
                Modifier.fillMaxWidth().padding(Spacing.MD),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if(isNew){
                    item {
                        NewClientForm(
                            onSubmit = { name, phone, address ->
                                onSelect(ClientSelection.New(name, phone, address))
                                onClose()
                            }
                        )
                    }
                }else{
                    if (clients.isEmpty()){
                        item {
                            AppText("Now saved clients!")
                            Spacer(Modifier.height(10.dp))
                            TextButton({
                                isNew = true
                            }) {
                                AppText("Create new client", color = colorScheme.primary)
                            }
                        }
                    }else{
                        clients.forEach { c ->
                            item {
                                TextButton(
                                    {
                                        onSelect(ClientSelection.Existing(c))
                                        onClose()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        AppText(c.profile.fullNames ?: "Unnamed client", maxLines = 1)
                                        AppText("${c.profile.defaultAddress} - ${c.profile.phone}", maxLines = 1, variant = TextType.Small, color = colorScheme.onBackground.copy(.6f))
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

@Composable
fun NewClientForm(onSubmit: (name: String, phone: String?, address: String?) -> Unit){
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    Column(Modifier.padding(Spacing.MD)) {
        TextInputField(
            value = name,
            onValueChange = {
                name = it
                nameError = ""
            },
            isError = nameError.isNotBlank(),
            errorMessage = nameError,
            label = "Name"
        )
        Spacer(Modifier.height(10.dp))
        TextInputField(
            value = phone,
            onValueChange = { phone = it },
            label = "Phone"
        )
        Spacer(Modifier.height(10.dp))
        TextInputField(
            value = address,
            onValueChange = { address = it },
            label = "Address"
        )
        Spacer(Modifier.height(Spacing.MD))
        TextButton(
            onClick = {
                if (name.isBlank()){
                    nameError = "The client name is required"
                    return@TextButton
                }
                onSubmit(name.trim(), phone.trim().ifBlank { null }, address.trim().ifBlank { null })
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            AppText("Save Client", color = colorScheme.onPrimary)
        }
    }
}

enum class DeviceSize{
    MOBILE, TABLET
}