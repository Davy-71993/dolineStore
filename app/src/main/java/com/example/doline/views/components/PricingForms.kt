package com.example.doline.views.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.doline.R
import com.example.doline.capitalize
import com.example.doline.data.Currency
import com.example.doline.data.ItemEntity
import com.example.doline.data.Periods
import com.example.doline.data.PriceRangeDraft
import com.example.doline.data.PriceRangeFormError
import com.example.doline.data.Pricing
import com.example.doline.data.PricingDetails
import com.example.doline.data.PricingDraft
import com.example.doline.data.PricingScheme
import com.example.doline.data.RecurringPriceFormError
import com.example.doline.data.SELECTED_CURRENCY
import com.example.doline.data.SelectOption
import com.example.doline.data.UnitPriceFormError
import com.example.doline.formatWithCommas
import com.example.doline.ui.theme.FontSize
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing


@Composable
fun PricingForm(
    pricings: List<Pricing>,
    onChange: (prices: List<Pricing>) -> Unit,
    item: ItemEntity,
    prevPricings: List<Pricing>,
){
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
    ) {
        when(item.pricingScheme){
            PricingScheme.FIXED -> {
                FixedPriceForm(
                    pricings.firstOrNull(),
                    { p -> onChange(listOf(p)) },
                    item.id,
                    prevPricings
                )
            }
            PricingScheme.UNIT -> {
                UnitPriceForm(
                    itemId = item.id,
                    pricings = pricings,
                    onPriceChange = onChange,
                    prevPricings = prevPricings
                )
            }
            PricingScheme.RECURRING -> {
                RecurringPriceForm(
                    itemId = item.id,
                    pricings = pricings,
                    onPriceChange = onChange,
                    prevPricings = prevPricings
                )
            }
            PricingScheme.RANGE -> {
                PriceRangeForm(
                    itemId = item.id,
                    pricings = pricings,
                    onPriceChange = onChange,
                    prevPricings = prevPricings,
                    determinants= item.priceDeterminants ?: emptyMap()
                )
            }
            PricingScheme.MENU -> {}
        }
    }
}

@Composable
fun PriceInputField(
    currency: Currency,
    onChange: (amount: Double?) -> Unit,
    amount: Double? = null,
    label: String = "PRICE",
    placeholder: String = "eg. 100,000",
    action: (@Composable ()-> Unit)? = null,
    fontSize: TextUnit = FontSize.XL,
    isError: Boolean = false,
    errorMessage: String? = null
){
    NumberInputField(
        onChange = { onChange(it?.toDouble())  },
        number = amount,
        label = label,
        placeholder = placeholder,
        leadingIcon = {
            AppText(
                currency.name.uppercase(),
                variant = TextType.Heading,
                modifier = Modifier.padding(start = 16.dp, end = 8.dp)
            )
        },
        action = action,
        fontSize = fontSize,
        isError = isError,
        errorMessage = errorMessage
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedPriceForm(
    pricing: Pricing? = null,
    onPriceChange: (price: Pricing) -> Unit,
    itemId: Long,
    prevPricings: List<Pricing> = emptyList()
){
    val sheetState = rememberModalBottomSheetState()
    var isDialogOpen by remember { mutableStateOf(false) }

    if (isDialogOpen){
        ModalBottomSheet(
            onDismissRequest = { isDialogOpen = false},
            sheetState = sheetState
        ) {
            AppText("Select Price", modifier = Modifier.fillMaxWidth(), variant = TextType.Heading, textAlign = TextAlign.Center)
            HorizontalDivider()
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD)
            ) {
                prevPricings.forEach { p->
                    val selected = pricing?.amount != null && p.amount == pricing.amount
                    val alfa = if(selected) 1f else .3f
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .clickable {
                                onPriceChange(p)
                                isDialogOpen = false
                            }
                            .background(colorScheme.primaryContainer.copy(alfa))
                            .padding(horizontal = Spacing.MD, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        PriceTag(p)
                        if (selected){
                            Icon(
                                painter = painterResource(R.drawable.tick),
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(IconSize.SMALL)
                            )
                        }
                    }
                }
            }
        }
    }
    PriceInputField(
        SELECTED_CURRENCY,
        {
            if (it != null){
                onPriceChange(Pricing(itemId = itemId, currency = SELECTED_CURRENCY, amount = it, details = PricingDetails.Fixed()))
            }
        },
        pricing?.amount,
        label = "SELLING PRICE",
        action = {
            if(prevPricings.isNotEmpty()){
                AppText(
                    "Use Previous Price",
                    color = colorScheme.primary,
                    variant = TextType.LabelSmall,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            isDialogOpen = true
                        }
                        .padding(4.dp, 1.dp)
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitPriceForm(
    itemId: Long,
    pricings: List<Pricing>,
    onPriceChange: (prices: List<Pricing>)-> Unit,
    prevPricings: List<Pricing> = emptyList()
){
    val sheetState = rememberModalBottomSheetState()
    var isDialogOpen by remember { mutableStateOf(false) }
    var isPrevPricesDialogOpen by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(UnitPriceFormError()) }

    var pricing by remember {
        mutableStateOf(
            PricingDraft(
                itemId,
            )
        )
    }

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppText("SELLING PRICES", variant = TextType.LabelSmall)
            if(prevPricings.isNotEmpty()){
                AppText(
                    "Add Previous Prices",
                    color = colorScheme.primary,
                    variant = TextType.LabelSmall,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            isPrevPricesDialogOpen = true
                        }
                        .padding(4.dp, 1.dp)
                )
            }
        }
        if (pricings.isEmpty()){
            AppText(
                "Define prices for various measuring units and state how the units scale with the stock keeping units.",
                textAlign = TextAlign.Center,
                color = colorScheme.onBackground.copy(.6f)
            )
        }else{
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Rounding.SM))
                    .border(
                        1.dp,
                        colorScheme.onBackground.copy(.1f),
                        RoundedCornerShape(Rounding.SM)
                    )
            ) {
                pricings.forEachIndexed { index,  p ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.MD, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        PriceTag(p)
                        IconButton(
                            {
                                val list  = pricings.toMutableList()
                                list.remove(p)
                                onPriceChange(list)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = colorScheme.errorContainer
                            ),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.x),
                                contentDescription = null,
                                tint = colorScheme.error,
                                modifier = Modifier.size(IconSize.SMALL)
                            )
                        }
                    }
                    if(index != pricings.size -1){
                        HorizontalDivider(color = colorScheme.onBackground.copy(.1f))
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                {
                    isDialogOpen = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.surface,
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 0.dp
                )
            ) {
                AppText("Add Unit Price", variant = TextType.LabelSmall, color = colorScheme.onSurface)
            }
        }
    }

    if (isDialogOpen){

        ModalBottomSheet(
            onDismissRequest = { isDialogOpen = false},
            sheetState = sheetState
        ) {
            AppText("Add a unit price", modifier = Modifier.fillMaxWidth(), variant = TextType.Heading, textAlign = TextAlign.Center)
            HorizontalDivider()
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD),
                verticalArrangement = Arrangement.spacedBy(Spacing.SM)
            ) {
                PriceInputField(
                    SELECTED_CURRENCY,
                    { amount ->
                        error = error.copy(price = null)
                        pricing = pricing.copy(amount= amount)
                    },
                    pricing.amount,
                    isError = !error.price.isNullOrEmpty(),
                    errorMessage = error.price
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    Box(Modifier.weight(1f)){
                        TextInputField(
                            (pricing.details as PricingDetails.UnitPrice?)?.units ?: "",
                            { u ->
                                error = error.copy(units = null, general = null)
                                val details = (pricing.details ?: PricingDetails.UnitPrice()) as PricingDetails.UnitPrice
                                pricing = pricing.copy(details= details.copy(units = u))
                            },
                            label = "SELLING UNITS",
                            placeHolder = "e.g kg, pcs",
                            isError = !error.units.isNullOrEmpty(),
                            errorMessage = error.units
                        )
                    }
                    Box(Modifier.weight(1f)){
                        NumberInputField(
                            label = "FACTOR",
                            number = (pricing.details as PricingDetails.UnitPrice?)?.conversionFactor,
                            placeholder = "e.g 12, 10, 0.01",
                            onChange = { f ->
                                error = error.copy(conversionFactor = null, general = null)
                                val details = (pricing.details ?: PricingDetails.UnitPrice()) as PricingDetails.UnitPrice
                                pricing = pricing.copy(details= details.copy(conversionFactor = f?.toDouble()))
                            },
                            isError = !error.conversionFactor.isNullOrEmpty(),
                            errorMessage = error.conversionFactor
                        )
                    }
                }
                val generalError = error.general
                if (!generalError.isNullOrEmpty()){
                    AppText(generalError, color = colorScheme.error)
                }
                TextButton(
                    {

                        val amount = pricing.amount
                        error = if (amount == null || amount <= 0){
                            error.copy(price = "The amount is required and must be a positive number")
                        }else{
                            error.copy(price = null)
                        }
                        val details = pricing.details as? PricingDetails.UnitPrice?

                        error = if (details == null){
                            error.copy(general = "Please provide the units and the conversion factor in relation to the item stock keeping units.")
                        }else{
                            error.copy(general = null)
                        }

                        error = if (details?.units.isNullOrEmpty()){
                            error.copy(units = "Specify the pricing units.")
                        }else{
                            error.copy(units = null)
                        }

                        error = if((details?.conversionFactor ?: 0.0) <= 0){
                            error.copy(conversionFactor = "Specify the conversion factor in relation to the item stock keeping units. This helps in updating stock")
                        }else{
                            error.copy(conversionFactor = null)
                        }

                        // Check if the new price is in the existing price list
                        val addedPricing = pricings.find { p ->
                            p.details != null
                                    && p.amount == amount
                                    && ( p.details as PricingDetails.UnitPrice ).units == (pricing.details as PricingDetails.UnitPrice?)?.units
                        }
                        error = if (addedPricing != null){
                            error.copy(general = "$SELECTED_CURRENCY ${addedPricing.amount.formatWithCommas()} per ${details?.units} already added.")
                        }else{
                            error.copy(general = null)
                        }

                        if(error.hasErrors()){
                            return@TextButton
                        }


                        // Check if new price is in the previous prices
                        val pricingsList = pricings.toMutableList()
                        val existingPricing = prevPricings.find { p ->
                            p.details != null
                                    && p.amount == amount
                                    && ( p.details as PricingDetails.UnitPrice ).units == (pricing.details as PricingDetails.UnitPrice?)?.units
                        }

                        if (existingPricing != null){
                            pricingsList.add(existingPricing)
                            onPriceChange(pricingsList)
                            pricing = PricingDraft(itemId = itemId)
                            return@TextButton
                        }

                        val p = Pricing(
                            amount = amount!!,
                            itemId = itemId,
                            details =  details,
                            currency = SELECTED_CURRENCY
                        )

                        pricingsList.add(p)
                        onPriceChange(pricingsList)

                        pricing = PricingDraft(itemId = itemId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppText("Add Pricing", variant = TextType.Label, color = colorScheme.onPrimaryContainer)
                }

            }
        }
    }

    if(isPrevPricesDialogOpen){
        ModalBottomSheet(
            onDismissRequest = { isPrevPricesDialogOpen = false},
            sheetState = sheetState
        ) {
            AppText("Select prices.", modifier = Modifier.fillMaxWidth(), variant = TextType.Heading, textAlign = TextAlign.Center)
            HorizontalDivider()
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD),
                verticalArrangement = Arrangement.spacedBy(Spacing.SM),
                horizontalAlignment = Alignment.End
            ) {
                prevPricings.forEach { p ->
                    val selected = pricings.contains(p)
                    val alfa = if(selected) 1f else .3f
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .clickable {
                                val l = pricings.toMutableList()
                                if (!selected) {
                                    l.add(p)
                                    onPriceChange(l)
                                }
                            }
                            .background(colorScheme.primaryContainer.copy(alfa))
                            .padding(horizontal = Spacing.MD, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        PriceTag(p)
                        if (selected){
                            Icon(
                                painter = painterResource(R.drawable.tick),
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(IconSize.SMALL)
                            )
                        }
                    }
                }
                TextButton(
                    { isPrevPricesDialogOpen = false},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.surfaceContainer
                    )
                ) {
                    AppText("Done", variant = TextType.Label, color = colorScheme.onSurface)
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringPriceForm(
    itemId: Long,
    pricings: List<Pricing>,
    onPriceChange: (prices: List<Pricing>)-> Unit,
    prevPricings: List<Pricing> = emptyList()
){
    val sheetState = rememberModalBottomSheetState()
    var isDialogOpen by remember { mutableStateOf(false) }
    var isPrevPricesDialogOpen by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(RecurringPriceFormError()) }

    var pricing by remember {
        mutableStateOf(
            PricingDraft(
                itemId,
            )
        )
    }

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppText("Pricing", variant = TextType.LabelSmall)
            if(prevPricings.isNotEmpty()){
                AppText(
                    "Add Previous Prices",
                    color = colorScheme.primary,
                    variant = TextType.LabelSmall,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            isPrevPricesDialogOpen = true
                        }
                        .padding(4.dp, 1.dp)
                )
            }
        }
        if (pricings.isEmpty()){
            AppText(
                "Define prices for various pricing periods.",
                textAlign = TextAlign.Center,
                color = colorScheme.onBackground.copy(.6f)
            )
        }else{
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Rounding.SM))
                    .border(
                        1.dp,
                        colorScheme.onBackground.copy(.1f),
                        RoundedCornerShape(Rounding.SM)
                    )
            ) {
                pricings.forEachIndexed { index,  p ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.MD, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        PriceTag(p)
                        IconButton(
                            {
                                val list  = pricings.toMutableList()
                                list.remove(p)
                                onPriceChange(list)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = colorScheme.errorContainer
                            ),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.x),
                                contentDescription = null,
                                tint = colorScheme.error,
                                modifier = Modifier.size(IconSize.SMALL)
                            )
                        }
                    }
                    if(index != pricings.size -1){
                        HorizontalDivider(color = colorScheme.onBackground.copy(.1f))
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                {
                    isDialogOpen = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.surface,
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 0.dp
                )
            ) {
                AppText("Add Pricing", variant = TextType.LabelSmall, color = colorScheme.onSurface)
            }
        }
    }

    if (isDialogOpen){

        ModalBottomSheet(
            onDismissRequest = { isDialogOpen = false},
            sheetState = sheetState
        ) {
            AppText("Add a recurring price", modifier = Modifier.fillMaxWidth(), variant = TextType.Heading, textAlign = TextAlign.Center)
            HorizontalDivider()
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD),
                verticalArrangement = Arrangement.spacedBy(Spacing.SM)
            ) {
                SelectField(
                    value = (pricing.details as PricingDetails.RecurringPrice?)?.period ?: "",
                    options = Periods.entries.map { pr -> SelectOption(label = pr.name.capitalize(), value = pr.name ) },
                    onSelect = { op ->
                        pricing = pricing.copy(details = PricingDetails.RecurringPrice(
                            period = op.value
                        ))
                    },
                    label = "BILLING PERIOD",
                    placeholder = "Select the pricing period.",
                    isError = !error.period.isNullOrEmpty(),
                    errorMessage = error.period
                )
                PriceInputField(
                    SELECTED_CURRENCY,
                    { amount ->
                        error = error.copy(price = null)
                        pricing = pricing.copy(amount= amount)
                    },
                    pricing.amount,
                    isError = !error.price.isNullOrEmpty(),
                    errorMessage = error.price
                )

                val generalError = error.general
                if (!generalError.isNullOrEmpty()){
                    AppText(generalError, color = colorScheme.error)
                }
                TextButton(
                    {

                        val amount = pricing.amount
                        error = if (amount == null || amount <= 0){
                            error.copy(price = "The amount is required and must be greater than 0.0")
                        }else{ error.copy(price = null) }

                        val details = pricing.details as? PricingDetails.RecurringPrice?
                        error = if (details == null){
                            error.copy(period = "Specify the billing period.")
                        }else{ error.copy(period = null) }

                        error = if (details?.period.isNullOrEmpty()){
                            error.copy(period = "Specify the billing period.")
                        }else{ error.copy(period = null) }

                        // Check if the new price is in the existing price list
                        val addedPricing = pricings.find { p ->
                           p.details != null &&
                                   ( p.details as PricingDetails.RecurringPrice? )?.period == (pricing.details as PricingDetails.RecurringPrice?)?.period
                        }
                        error = if (addedPricing != null){
                            error.copy(general = "Pricing per ${details?.period} already added. If you intend to change it, first delete it from the list.")
                        }else{
                            error.copy(general = null)
                        }

                        if(error.hasErrors()){
                            return@TextButton
                        }

                        val pricingsList = pricings.toMutableList()
                        // Check if new price is in the previous prices
                        val existingPricing = prevPricings.find { p ->
                            p.details != null &&
                                    p.amount == amount &&
                                    ( p.details as PricingDetails.RecurringPrice? )?.period == (pricing.details as PricingDetails.RecurringPrice?)?.period
                        }

                        if (existingPricing != null){
                            pricingsList.add(existingPricing)
                            onPriceChange(pricingsList)
                            pricing = PricingDraft(itemId = itemId)
                            return@TextButton
                        }

                        val p = Pricing(
                            amount = amount!!,
                            itemId = itemId,
                            details =  details,
                            currency = SELECTED_CURRENCY
                        )

                        pricingsList.add(p)
                        onPriceChange(pricingsList)

                        pricing = PricingDraft(itemId = itemId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppText("Add Pricing", variant = TextType.Label, color = colorScheme.onPrimaryContainer)
                }

            }
        }
    }

    if(isPrevPricesDialogOpen){
        ModalBottomSheet(
            onDismissRequest = { isPrevPricesDialogOpen = false},
            sheetState = sheetState
        ) {
            AppText("Select prices.", modifier = Modifier.fillMaxWidth(), variant = TextType.Heading, textAlign = TextAlign.Center)
            HorizontalDivider()
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD),
                verticalArrangement = Arrangement.spacedBy(Spacing.SM),
                horizontalAlignment = Alignment.End
            ) {
                prevPricings.forEach { p ->
                    val selected = pricings.contains(p)
                    val alfa = if(selected) 1f else .3f
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .clickable {
                                val l = pricings.toMutableList()
                                if (!selected) {
                                    l.add(p)
                                    onPriceChange(l)
                                }
                            }
                            .background(colorScheme.primaryContainer.copy(alfa))
                            .padding(horizontal = Spacing.MD, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        PriceTag(p)
                        if (selected){
                            Icon(
                                painter = painterResource(R.drawable.tick),
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(IconSize.SMALL)
                            )
                        }
                    }
                }
//                Spacer(Modifier.height(0.dp))
                TextButton(
                    { isPrevPricesDialogOpen = false},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.surfaceContainer
                    )
                ) {
                    AppText("Done", variant = TextType.Label, color = colorScheme.onSurface)
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceRangeForm(
    itemId: Long,
    pricings: List<Pricing>,
    onPriceChange: (prices: List<Pricing>)-> Unit,
    prevPricings: List<Pricing> = emptyList(),
    determinants: Map<String, List<String>?>
){
    val sheetState = rememberModalBottomSheetState()
    var isDialogOpen by remember { mutableStateOf(false) }
    var isPrevPricesDialogOpen by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(PriceRangeFormError()) }
    var details by remember { mutableStateOf<PriceRangeDraft?>(null) }
    var pricing by remember { mutableStateOf(PricingDraft(itemId)) }

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
    ) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppText("Pricing", variant = TextType.LabelSmall)
            if(prevPricings.isNotEmpty()){
                AppText(
                    "Add Previous Prices",
                    color = colorScheme.primary,
                    variant = TextType.LabelSmall,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            isPrevPricesDialogOpen = true
                        }
                        .padding(4.dp, 1.dp)
                )
            }
        }
        if (pricings.isEmpty()){
            AppText(
                "Define prices based on the item specifications.",
                textAlign = TextAlign.Center,
                color = colorScheme.onBackground.copy(.6f)
            )
        }else{
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Rounding.SM))
                    .border(
                        1.dp,
                        colorScheme.onBackground.copy(.1f),
                        RoundedCornerShape(Rounding.SM)
                    )
            ) {
                pricings.forEachIndexed { index,  p ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.MD, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        PriceTag(p)
                        IconButton(
                            {
                                val list  = pricings.toMutableList()
                                list.remove(p)
                                onPriceChange(list)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = colorScheme.errorContainer
                            ),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.x),
                                contentDescription = null,
                                tint = colorScheme.error,
                                modifier = Modifier.size(IconSize.SMALL)
                            )
                        }
                    }
                    if(index != pricings.size -1){
                        HorizontalDivider(color = colorScheme.onBackground.copy(.1f))
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                {
                    isDialogOpen = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.surface,
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 0.dp
                )
            ) {
                AppText("Add Pricing", variant = TextType.LabelSmall, color = colorScheme.onSurface)
            }
        }
    }

    if (isDialogOpen){

        ModalBottomSheet(
            onDismissRequest = { isDialogOpen = false},
            sheetState = sheetState
        ) {
            AppText("Add price", modifier = Modifier.fillMaxWidth(), variant = TextType.Heading, textAlign = TextAlign.Center)
            HorizontalDivider()
            if(determinants.isEmpty()){
                error = error.copy(general = "There are no price determinants specified on this item.")
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.SM)
            ) {

                determinants.forEach { dt ->
                   val options = dt.value?.map { v -> SelectOption(v.trim(), v.trim()) } ?: emptyList()
                    if (options.isNotEmpty()){
                        SelectField(
                            value = details?.specs?.get(dt.key) ?: "",
                            options = options,
                            onSelect = { op ->
                                val currentDetails = details ?: PriceRangeDraft()
                                val currentSpecs = currentDetails.specs ?: emptyMap()
                                val updatedSpecs = currentSpecs + (dt.key to op.value)

                                val updatedDetails = currentDetails.copy(specs = updatedSpecs)
                                 details = updatedDetails
                            },
                            label = dt.key.uppercase(),
                            placeholder = "Select ${dt.key}"
                        )
                    }
                }
                if(!error.specs.isNullOrEmpty()){
                    AppText(error.specs?: "", color = colorScheme.error)
                }
                PriceInputField(
                    Currency.UGX,
                    { bp->
                        pricing = pricing.copy(amount = bp)
                    },
                    pricing.amount,
                    label = "PRICE",
                    placeholder = "eg. 100,000",
                    isError = !error.price.isNullOrBlank(),
                    errorMessage = error.price
                )
                NumberInputField(
                    onChange = { qty ->
                        val currentDetails = details ?: PriceRangeDraft()
                        val updatedDetails = currentDetails.copy(qty = qty as Double?)
                        details = updatedDetails
                    },
                    number = details?.qty,
                    label = "QUANTITY AVAILABLE",
                    placeholder = "Enter quantity",
                    isError = !error.qty.isNullOrBlank(),
                    errorMessage = error.qty
                )
                val generalError = error.general
                if (!generalError.isNullOrEmpty()){
                    AppText(generalError, color = colorScheme.error)
                }
                TextButton(
                    {
                        val specs = details?.specs
                        val qty = details?.qty
                        val amount = pricing.amount
                        error = if (specs.isNullOrEmpty()){
                            error.copy(specs="You must specify the values for all price determinants.")
                        }else{
                            error.copy(specs=null)
                        }
                        error = if (qty == null || qty <= 0){
                            error.copy(qty="Please specify the quantity available based on the selected specs.")
                        }else{
                            error.copy(qty=null)
                        }
                        error = if (amount == null || amount <= 0){
                            error.copy(price = "Provide a valid price for the selection.")
                        }else{
                            error.copy(price=null)
                        }

                        if(specs == null || qty == null || qty <= 0 || amount == null || amount <= 0){
                            return@TextButton
                        }
                        val d = PricingDetails.PriceRange(specs = specs, qty = qty)

                        val exists = pricings.find { p ->
                            (p.details as PricingDetails.PriceRange?)?.specs == d.specs
                        }
                        error = if(exists != null){
                            error.copy(general =  "Pricing for ${d.specs?.map { d -> "${d.key}: ${d.value}" }.toString()} already exists. If you intend to change, first delete it from the price list.")
                        }else{
                            error.copy(general = null)
                        }

                        val inPrev = prevPricings.find { p ->
                            (p.details as PricingDetails.PriceRange?)?.specs == d.specs
                        }

                        if(inPrev != null){
                            val q = (inPrev.details as PricingDetails.PriceRange?)?.qty?.plus(qty) ?: qty
                            val d = (inPrev.details as PricingDetails.PriceRange).copy(qty = q)
                            val p = inPrev.copy(details = d)
                            val list = pricings.toMutableList()
                            list.add(p)
                            onPriceChange(list)
                            pricing = PricingDraft(itemId = itemId)
                            details = null

                            return@TextButton
                        }

                        if(error.hasErrors()){
                            return@TextButton
                        }
                        val p = Pricing(itemId = itemId, amount = amount, currency = SELECTED_CURRENCY, details = d)
                        val list = pricings.toMutableList()
                        list.add(p)
                        onPriceChange(list)
                        pricing = PricingDraft(itemId = itemId)
                        details = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryContainer,
                    ),
                    enabled = determinants.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppText("Add Pricing", variant = TextType.Label, color = colorScheme.onPrimaryContainer)
                }

            }
        }
    }

    if(isPrevPricesDialogOpen){
        ModalBottomSheet(
            onDismissRequest = { isPrevPricesDialogOpen = false},
            sheetState = sheetState
        ) {
            AppText("Select prices.", modifier = Modifier.fillMaxWidth(), variant = TextType.Heading, textAlign = TextAlign.Center)
            HorizontalDivider()
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD),
                verticalArrangement = Arrangement.spacedBy(Spacing.SM),
                horizontalAlignment = Alignment.End
            ) {
                prevPricings.forEach { p ->
                    val selected = pricings.contains(p)
                    val alfa = if(selected) 1f else .3f
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .clickable {
                                val l = pricings.toMutableList()
                                if (!selected) {
                                    l.add(p)
                                    onPriceChange(l)
                                }
                            }
                            .background(colorScheme.primaryContainer.copy(alfa))
                            .padding(horizontal = Spacing.MD, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        PriceTag(p)
                        if (selected){
                            Icon(
                                painter = painterResource(R.drawable.tick),
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(IconSize.SMALL)
                            )
                        }
                    }
                }
//                Spacer(Modifier.height(0.dp))
                TextButton(
                    { isPrevPricesDialogOpen = false},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.surfaceContainer
                    )
                ) {
                    AppText("Done", variant = TextType.Label, color = colorScheme.onSurface)
                }
            }
        }
    }

}

