package com.example.doline.views.screens.store.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.models.Currency
import com.example.doline.data.models.PaymentMethod
import com.example.doline.data.models.currencies
import com.example.doline.data.models.paymentMethods
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSettingsScreen(navController: NavController){
    val colorScheme = MaterialTheme.colorScheme
    val rect  = MaterialTheme.shapes.medium

    var accepted by remember {
        mutableStateOf(emptyList<PaymentMethod>())
    }

    var acceptedCurrencies by remember {
        mutableStateOf(emptyList<Currency>())
    }

    var isLoyaltyEnabled by remember {
        mutableStateOf(true)
    }

    var isTipsEnabled by remember {
        mutableStateOf(true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Payment & Tenders", variant = TextType.Heading, maxLines = 1) },
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
                },
                actions = {
                },
            )
        }
    ) {p ->
        LazyColumn(
            modifier = Modifier.padding(vertical = p.calculateTopPadding(), horizontal = Spacing.SM),
            verticalArrangement = Arrangement.spacedBy(Spacing.XL)
        ) {
            item {
                Spacer(Modifier.height(0.dp))
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            colorScheme.surface,
                            rect
                        )
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    AppText(
                        "Supported Currencies",
                        variant = TextType.Label
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
                        verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                    ) {
                        currencies.forEach { method ->
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = if (method in acceptedCurrencies) colorScheme.primary else colorScheme.background.copy(.5f),
                                modifier = Modifier
                                    .padding(end = Spacing.XS)
                                    .clickable  {
                                        acceptedCurrencies = if (method in acceptedCurrencies) {
                                            acceptedCurrencies - method
                                        } else {
                                            acceptedCurrencies + method
                                        }
                                    }
                            ) {
                                AppText(
                                    method.abbr,
                                    color = if (method in acceptedCurrencies) colorScheme.onPrimary else colorScheme.onBackground,
                                    modifier = Modifier
                                        .padding(horizontal = Spacing.SM, vertical = Spacing.XS)
                                )
                            }
                        }
                    }
                }

            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            colorScheme.surface,
                            rect
                        )
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    AppText(
                        "Accepted Payment Methods",
                        variant = TextType.Label
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
                        verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                    ) {
                        paymentMethods.forEach { method ->
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = if (method in accepted) colorScheme.primary else colorScheme.background.copy(.5f),
                                modifier = Modifier
                                    .padding(end = Spacing.XS)
                                    .fillMaxWidth(0.45f)
                                    .clickable  {
                                        accepted = if (method in accepted) {
                                            accepted - method
                                        } else {
                                            accepted + method
                                        }
                                    }
                            ) {
                                AppText(
                                    method.name,
                                    variant = TextType.Body,
                                    color = if (method in accepted) colorScheme.onPrimary else colorScheme.onBackground,
                                    modifier = Modifier
                                        .padding(horizontal = Spacing.SM, vertical = Spacing.XS)
                                )
                            }
                        }
                    }
                }

            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            colorScheme.surface,
                            rect
                        )
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.XXS),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText("Enable loyalty program", variant = TextType.Label)
                        Switch(checked = isLoyaltyEnabled, onCheckedChange = { isLoyaltyEnabled = it })
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if(isLoyaltyEnabled) 1f else .5f),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        TextInputField(
                            value = "",
                            onValueChange = {},
                            label = "Amount spent",
                            enabled = isLoyaltyEnabled,
                            placeHolder = "UGX 000",
                        )
                        TextInputField(
                            value = "",
                            onValueChange = {},
                            label = "Points",
                            enabled = isLoyaltyEnabled,
                            placeHolder = "O Points",
                        )
                        AppButton(
                            "Save loyalty rule",
                            onClick = {},
                            type = ButtonType.Primary,
                            enabled = isLoyaltyEnabled
                        )
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            colorScheme.surface,
                            rect
                        )
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.XXS),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText("Enable tips", variant = TextType.Label)
                        Switch(checked = isTipsEnabled, onCheckedChange = { isTipsEnabled = it })
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if(isTipsEnabled) 1f else .5f),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        TextInputField(
                            value = "",
                            onValueChange = {},
                            label = "Tippable amount",
                            enabled = isTipsEnabled,
                            placeHolder = "UGX 000",
                        )
                        TextInputField(
                            value = "",
                            onValueChange = {},
                            label = "Percentage tip",
                            enabled = isTipsEnabled,
                            placeHolder = "O%",
                        )
                        AppButton(
                            "Save tip rule",
                            onClick = {},
                            type = ButtonType.Primary,
                            enabled = isTipsEnabled
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(0.dp))
            }
        }
    }
}