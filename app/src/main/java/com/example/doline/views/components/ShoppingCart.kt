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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.doline.R
import com.example.doline.data.CartItemEntity
import com.example.doline.data.Currency
import com.example.doline.data.Pricing
import com.example.doline.data.PricingDetails
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing

@Composable
fun ShoppingCart(
    onCompleteSale: () -> Unit = {},
    onClose: ()-> Unit,
    onCartItemChanged: (CartItemEntity, index: Int) -> Unit,
    onDelete: (item: CartItemEntity) -> Unit = {},
    onClear: () -> Unit = {},
    items: List<CartItemEntity>,
    deviceSize: DeviceSize = DeviceSize.MOBILE
){
    var cartTotal by remember { mutableDoubleStateOf(0.0) }
    var amountReceived by remember { mutableStateOf<Double?>(null) }
    var change by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(items) {
        cartTotal = items.sumOf { i -> ((i.qty ?: 0.0) * (i.pricing.amount ?: 0.0)) }
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
                text= "Shopping Cart",
                variant = TextType.Heading
            )
            Spacer(Modifier.weight(1f))
            ClearCartDialog { onClear() }
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
                    amount = amountReceived,
                    onChange = { am ->
                        amountReceived = am
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
                    {
                        // Complete sale
                        onCompleteSale()
                        // Clear cart
                        onClear()
                        // Close cart
                        onClose()
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
fun CartItem(onChange: (CartItemEntity) -> Unit = {}, onDelete: () -> Unit, cartItemDraft: CartItemEntity){
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
    val qty = cartItemDraft.qty

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.MD)
    ) {
        AppText(cartItemDraft.item.name, variant = TextType.Label)
        FlowRow(Modifier.fillMaxWidth()) {
            cartItemDraft.specs?.forEach { pair->
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
                        val nci = cartItemDraft.copy(qty = newQty)
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
                    "${cartItemDraft.qty} $qtyUnits",
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colorScheme.onBackground.copy(.1f))
                        .padding(10.dp, 4.dp)
                )
                IconButton(
                    onClick = {
                        val newQty = ( qty + 1).coerceAtMost(cartItemDraft.maxQty)
                        val nci = cartItemDraft.copy(qty = newQty)
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
            containerColor = colorScheme.inverseSurface
        ),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        AppText("Clear cart", color = colorScheme.surface)
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
                    AppText(text = "Clear Cart", color = colorScheme.primary)
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
fun DeleteCartItemDialog(action: () -> Unit, item: CartItemEntity){
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

enum class DeviceSize{
    MOBILE, TABLET
}