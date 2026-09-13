package com.example.doline.views.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.doline.R
import com.example.doline.data.ItemWithBatches
import com.example.doline.data.PricingDetails
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.screens.store.pos.CartItemDraft


@Composable
fun FixedPriceAddToCartForm(
    onChange: (cartItemDraft: CartItemDraft) -> Unit,
    record: ItemWithBatches,
    cartItem: CartItemDraft
){
    val batches = record.batches.filter { b -> b.batch.available != 0.0 }
    val currentBatch = batches.find { b -> b.batch.createdAt == batches.maxOf { r -> r.batch.createdAt } }
    val pricing = currentBatch?.pricings?.firstOrNull()
    val totalAvailable = batches.sumOf { b-> b.batch.available }
    if (pricing == null){
        AppText("No active pricing for this item", color = colorScheme.error)
        return
    }

    PriceTag(pricing)
    NumberInputField(
        onChange = { n ->
            if(n != null && n.toDouble() <= totalAvailable){
                val qty = n.toDouble()
                val updatedCartItemDraft = cartItem.copy(qty = qty)
                onChange(updatedCartItemDraft)
            }
        },
        number = cartItem.qty,
        label = "QUANTITY",
        textAlign = TextAlign.Center,
        leadingIcon = {
            IconButton({
                val qty = (cartItem.qty ?: 0.0).minus(1.0).coerceAtLeast(0.0)
                val updatedCartItemDraft= cartItem.copy(qty = qty)
                onChange(updatedCartItemDraft)
            }) {
                Icon(
                    painter = painterResource(R.drawable.minus),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            }
        },
        trailingIcon = {
            IconButton({
                val qty = (cartItem.qty?:0.0).plus(1.0).coerceAtMost(totalAvailable)
                val updatedCartItemDraft = cartItem.copy(qty = qty)
                onChange(updatedCartItemDraft)
            }) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            }
        }
    )
}
@Composable
fun UnitPriceAddToCartForm(
    onChange: (cartItemDraft: CartItemDraft) -> Unit,
    record: ItemWithBatches,
    cartItem: CartItemDraft
){
    val batches = record.batches.filter { rb -> rb.pricings.isNotEmpty() && rb.batch.quantity != 0.0 }
    val currentBatch = batches.find { b -> b.batch.createdAt == batches.maxOf { r -> r.batch.createdAt } }
    val pricings = currentBatch?.pricings ?: emptyList()
    var totalQty by remember { mutableDoubleStateOf(batches.sumOf { b-> b.batch.available }) }


    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
        verticalArrangement = Arrangement.spacedBy(Spacing.XS)
    ) {
        pricings.forEach { p ->
            val selected = p == cartItem.pricing
            TextButton(
                {
                    val factor = (p.details as PricingDetails.UnitPrice).conversionFactor ?: 1.0
                    val ntq = batches.sumOf { b-> b.batch.quantity } * factor
                    totalQty = ntq
                    val updatedCartItem = cartItem.copy(pricing = p, maxQty = ntq)
                    onChange(updatedCartItem)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(selected) colorScheme.primary else colorScheme.primaryContainer.copy(.6f)
                ),
                shape = RoundedCornerShape(Rounding.SM)
            ) {
                PriceTag(p, textColor = if(selected) colorScheme.onPrimary else colorScheme.primary)
            }
        }
    }

    Spacer(Modifier.height(Spacing.MD))
    NumberInputField(
            onChange = { n ->
                val qty = n?.toDouble()
                val updatedCartItemDraft = cartItem.copy(qty = qty)
                onChange(updatedCartItemDraft)
            },
            number = cartItem.qty,
            label = "QUANTITY",
            textAlign = TextAlign.Center,
            leadingIcon = {
                IconButton({
                    val qty = (cartItem.qty ?: 0.0).minus(1.0).coerceAtLeast(1.0)
                    val updatedCartItemDraft = cartItem.copy(qty = qty)
                    onChange(updatedCartItemDraft)
                }) {
                    Icon(
                        painter = painterResource(R.drawable.minus),
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(IconSize.NORMAL)
                    )
                }
            },
            trailingIcon = {
                IconButton({
                    val qty = (cartItem.qty ?: 0.0).plus(1.0).coerceAtMost(totalQty)
                    val updatedCartItemDraft = cartItem.copy(qty = qty)
                    onChange(updatedCartItemDraft)
                }) {
                    Icon(
                        painter = painterResource(R.drawable.plus),
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(IconSize.NORMAL)
                    )
                }
            }
        )
}

@Composable
fun PriceRangeAddToCartForm(
    onChange: (cartItemDraft: CartItemDraft) -> Unit,
    record: ItemWithBatches,
    cartItem: CartItemDraft
){
    val batches = record.batches.filter { rb -> rb.pricings.isNotEmpty() && rb.batch.available != 0.0 }
    val currentBatch = batches.find { b -> b.batch.createdAt == batches.maxOf { r -> r.batch.createdAt } }
    val pricings = currentBatch?.pricings ?: emptyList()


    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
        verticalArrangement = Arrangement.spacedBy(Spacing.XS)
    ) {
        pricings.forEach { p ->
            val selected = p == cartItem.pricing
            TextButton(
                {
                    val qty = (p.details as PricingDetails.PriceRange).qty
                    val updatedCartItem = cartItem.copy(pricing = p, maxQty = qty)
                    onChange(updatedCartItem)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(selected) colorScheme.primary else colorScheme.primaryContainer.copy(.6f)
                ),
                shape = RoundedCornerShape(Rounding.SM)
            ) {
                PriceTag(p, textColor = if(selected) colorScheme.onPrimary else colorScheme.primary)
            }
        }
    }

    Spacer(Modifier.height(Spacing.MD))
    NumberInputField(
        onChange = { n ->
            val qty = n?.toDouble()
            val updatedCartItemDraft = cartItem.copy(qty = qty)
            onChange(updatedCartItemDraft)
        },
        number = cartItem.qty,
        label = "QUANTITY",
        textAlign = TextAlign.Center,
        leadingIcon = {
            IconButton({
                val qty = (cartItem.qty ?: 0.0).minus(1.0).coerceAtLeast(1.0)
                val updatedCartItemDraft = cartItem.copy(qty = qty)
                onChange(updatedCartItemDraft)
            }) {
                Icon(
                    painter = painterResource(R.drawable.minus),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            }
        },
        trailingIcon = {
            IconButton({
                val qty = (cartItem.qty ?: 0.0).plus(1.0).coerceAtMost(cartItem.maxQty)
                val updatedCartItemDraft = cartItem.copy(qty = qty)
                onChange(updatedCartItemDraft)
            }) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            }
        }
    )
}

@Composable
fun RecurringAddToCartForm(
    onChange: (cartItemDraft: CartItemDraft) -> Unit,
    record: ItemWithBatches,
    cartItem: CartItemDraft
){
    val batches = record.batches.filter { rb -> rb.pricings.isNotEmpty() && rb.batch.quantity > 0 }
    val currentBatch = batches.find { b -> b.batch.createdAt == batches.maxOf { r -> r.batch.createdAt } }
    val pricings = currentBatch?.pricings ?: emptyList()

    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
        verticalArrangement = Arrangement.spacedBy(Spacing.XS)
    ) {
        pricings.forEach { p ->
            val selected = p == cartItem.pricing
            TextButton(
                {
                    val updatedCartItem = cartItem.copy(pricing = p)
                    onChange(updatedCartItem)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(selected) colorScheme.primary else colorScheme.primaryContainer.copy(.6f)
                ),
                shape = RoundedCornerShape(Rounding.SM)
            ) {
                PriceTag(p, textColor = if(selected) colorScheme.onPrimary else colorScheme.primary)
            }
        }
    }

    Spacer(Modifier.height(Spacing.MD))
    NumberInputField(
        onChange = { n ->
            val qty = n?.toDouble()
            val updatedCartItemDraft = cartItem.copy(qty = qty)
            onChange(updatedCartItemDraft)
        },
        number = cartItem.qty,
        label = "${(cartItem.pricing?.details as PricingDetails.RecurringPrice?)?.period ?: "PERIOD"}S",
        textAlign = TextAlign.Center,
        leadingIcon = {
            IconButton({
                val qty = (cartItem.qty ?: 0.0).minus(1.0).coerceAtLeast(1.0)
                val updatedCartItemDraft = cartItem.copy(qty = qty)
                onChange(updatedCartItemDraft)
            }) {
                Icon(
                    painter = painterResource(R.drawable.minus),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            }
        },
        trailingIcon = {
            IconButton({
                val qty = (cartItem.qty ?: 0.0).plus(1.0)
                val updatedCartItemDraft = cartItem.copy(qty = qty)
                onChange(updatedCartItemDraft)
            }) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            }
        }
    )
}