package com.example.doline.views.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.doline.data.Pricing
import com.example.doline.data.PricingDetails
import com.example.doline.formatWithCommas

@Composable
fun PricingsTag(pricings: List<Pricing>, textColor: Color = colorScheme.primary){
    val firstPricing = pricings.firstOrNull() ?: return
    if (pricings.size == 1){
        PriceTag(firstPricing, textColor = textColor,)
    }else{
        val minPrice = pricings.find { it.amount == pricings.minOf { p -> p.amount } } ?: return
        Row(
            Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PricePrefix(color = textColor)
            PriceTag(minPrice, textColor = textColor,)
        }
    }
}

@Composable
fun PriceTag(
    pricing: Pricing,
    modifier: Modifier = Modifier,
    textColor: Color = colorScheme.primary,
    size: TextType = TextType.Label,
){
    when(val details = pricing.details){
        is PricingDetails.UnitPrice -> {
            Row(
                modifier = modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Price(pricing, color = textColor, size= size)
                PriceSuffix("Per ${details.units?.lowercase() ?: "unit"}", color = textColor)
            }
        }
        is PricingDetails.RecurringPrice -> {
            Row(
                modifier = modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Price(pricing, color = textColor, size=size)
                PriceSuffix("Per ${details.period.lowercase()}", color = textColor)
            }
        }

        else -> { Price(pricing, color = textColor, size=size) }
    }
}

@Composable
fun Price(pricing: Pricing, color: Color = colorScheme.primary, size: TextType){
    Column(Modifier.wrapContentSize()) {
        Row(
            Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            AppText(pricing.currency.name.uppercase(), variant = size, color = color, maxLines = 1)
            AppText(pricing.amount.formatWithCommas(), variant = size, color = color, maxLines = 1)
        }
        if(pricing.details is PricingDetails.PriceRange){
            AppText(
                pricing
                    .details
                    .specs
                    .toString()
                    .replace(" ", "")
                    .replace("=", ": ")
                    .replace("{", "")
                    .replace("}", "")
                    .replace(",", ", "),
                color = color.copy(.6f),
                variant = TextType.ExtraSMall)
        }
    }

}

@Composable
fun PriceSuffix(suffix: String, color: Color = colorScheme.primary){
    AppText(suffix, variant = TextType.LabelSmall, color = color, maxLines = 1)
}

@Composable
fun PricePrefix(color: Color = colorScheme.primary){
    AppText("From", variant = TextType.LabelSmall, color = color, maxLines = 1)
}