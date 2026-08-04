package com.example.doline.views.screens.store.home.settings

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.formatWithCommas
import com.example.doline.ui.theme.FontSize
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.ui.theme.slateContainer
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDeliveryZone(navController: NavController){
    var amount by remember {
        mutableFloatStateOf(0f)
    }

    var distance by remember {
        mutableFloatStateOf(0f)
    }

    var distanceUnits by remember {
        mutableStateOf("KM")
    }

    var deliveryTime by remember {
        mutableFloatStateOf(0f)
    }

    var deliveryTimeUnits by remember {
        mutableStateOf("Mins")
    }

    val distanceUnitsList = listOf("KM" to "Kilometers", "Meters" to "Meters", "Miles" to "Miles")
    val deliveryTimeUnitsList = listOf("Mins" to "Minutes", "Hrs" to "Hours", "Days" to "Days")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Create delivery zone", variant = TextType.Heading, maxLines = 1) },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    subtitleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                modifier = Modifier.shadow(Spacing.MD),
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
                }
            )
        }
    ) { p ->
        LazyColumn(
            modifier = Modifier.padding(Spacing.XL, p.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
        ) {
            item {
                Spacer(Modifier.height(0.dp))
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(Rounding.MD)
                        )
                        .border(
                            shape = RoundedCornerShape(Rounding.MD),
                            border = BorderStroke(
                                1.dp,
                                color = MaterialTheme.colorScheme.onBackground.copy(.5f)
                            )
                        )
                        .padding(Spacing.MD)
                        .clickable {

                        },
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    AppText(
                        "ENTER AMOUNT",
                        color = MaterialTheme.colorScheme.onBackground.copy(.6f),
                        variant = TextType.Label
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.XXS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(
                            "UGX",
                            color = MaterialTheme.colorScheme.primary,
                            variant = TextType.Heading
                        )
                        TextField(
                            value = if(amount > 0) amount.formatWithCommas() else "",
                            onValueChange = { amount =
                                if(it.isNotEmpty()) it.replace(",", "").toFloat() else 0f },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.primary,
                                unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = FontSize.XL,
                                fontWeight = FontWeight.W800
                            ),
                            placeholder = {
                                AppText(
                                    "0000",
                                    color = MaterialTheme.colorScheme.onBackground.copy(.4f),
                                    variant = TextType.Heading
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(Rounding.MD)
                        )
                        .border(
                            shape = RoundedCornerShape(Rounding.MD),
                            border = BorderStroke(
                                1.dp,
                                color = MaterialTheme.colorScheme.onBackground.copy(.5f)
                            )
                        )
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.XS)
                ) {
                    AppText(
                        "DELIVERY DISTANCE",
                        color = MaterialTheme.colorScheme.onBackground.copy(.6f),
                        variant = TextType.Label
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.XS)
                    ) {
                        distanceUnitsList.forEach {
                            (id, label) ->
                            Box(
                                modifier = Modifier
                                    .clickable { distanceUnits = id }
                                    .background(
                                        if (distanceUnits == id) MaterialTheme.colorScheme.primary else slateContainer,
                                        RoundedCornerShape(Rounding.FULL)
                                    )
                                    .padding(20.dp, 5.dp)
                                    .wrapContentSize()
                            ){
                                AppText(
                                    label,
                                    color = if (distanceUnits == id) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.XXS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = if(distance > 0) distance.toString() else "",
                            onValueChange = { distance = it.toFloatOrNull() ?: 0f },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.primary,
                                unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(.6f),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = FontSize.XL,
                                fontWeight = FontWeight.W800
                            ),
                            placeholder = { AppText("0", color = MaterialTheme.colorScheme.onBackground.copy(.4f), variant = TextType.Heading) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        AppText(
                            distanceUnits,
                            color = MaterialTheme.colorScheme.primary,
                            variant = TextType.Heading
                        )
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(Rounding.MD)
                        )
                        .border(
                            shape = RoundedCornerShape(Rounding.MD),
                            border = BorderStroke(
                                1.dp,
                                color = MaterialTheme.colorScheme.onBackground.copy(.5f)
                            )
                        )
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    AppText(
                        "DELIVERY TIME",
                        color = MaterialTheme.colorScheme.onBackground.copy(.6f),
                        variant = TextType.Label
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.XS)
                    ) {
                        deliveryTimeUnitsList.forEach { (id, label) ->
                            Box(
                                modifier = Modifier
                                    .clickable { deliveryTimeUnits = id }
                                    .background(
                                        if (deliveryTimeUnits == id) MaterialTheme.colorScheme.primary else slateContainer,
                                        RoundedCornerShape(Rounding.FULL)
                                    )
                                    .padding(20.dp, 5.dp)
                                    .wrapContentSize()
                            ) {
                                AppText(
                                    label,
                                    color = if (deliveryTimeUnits == id) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.XXS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = if(deliveryTime > 0) deliveryTime.toString() else "",
                            onValueChange = { deliveryTime = it.toFloatOrNull() ?: 0f },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.primary,
                                unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(.6f),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = FontSize.XL,
                                fontWeight = FontWeight.W800
                            ),
                            placeholder = { AppText("0", color = MaterialTheme.colorScheme.onBackground.copy(.4f), variant = TextType.Heading) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        AppText(
                            deliveryTimeUnits,
                            color = MaterialTheme.colorScheme.primary,
                            variant = TextType.Heading
                        )
                    }
                }
            }
            item {
                AppButton(
                    "Save",
                    onClick = {
                        navController.popBackStack()
                    },
                    type = ButtonType.Primary
                )
            }
            item {
                Spacer(Modifier.height(0.dp))
            }
        }
    }
}