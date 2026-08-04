package com.example.doline.views.screens.store.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.models.DeliveryZone
import com.example.doline.formatWithCommas
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.ui.theme.onSlateContainer
import com.example.doline.ui.theme.onSuccessContainerLight
import com.example.doline.ui.theme.slateContainer
import com.example.doline.ui.theme.successContainerLight
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.TextType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippingSettingsScreen(navController: NavController){

    val colorScheme = MaterialTheme.colorScheme
    val rect  = MaterialTheme.shapes.medium

    var offerDelivery by remember {
        mutableStateOf(true)
    }

    val deliveryZones = remember { mutableStateListOf(
        DeliveryZone(
            5f,
            0f,
            deliveryTime = 30
        ),
        DeliveryZone(
            25f,
            3000f,
            deliveryTime = 60
        ),
        DeliveryZone(
            45f,
            10000f,
            deliveryTime = 2,
            timeUnits = "Hrs"
        )
    ) }


    val freeDeliveryZone = deliveryZones.find { z -> z.price == 0f }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Shipping & Delivery", variant = TextType.Heading, maxLines = 1) },
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
        },
    ) {p -> 
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = p.calculateTopPadding(), horizontal = Spacing.SM)
                .padding(vertical = Spacing.XL),
            verticalArrangement = Arrangement.spacedBy(Spacing.XL)
        ) {
         
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
                AppText("Do you offer delivery?", variant = TextType.Label)
                Row(
                    modifier = Modifier.fillMaxWidth(1f)
                ) {
                    val options = listOf("yes" to "YES", "no" to "NO")
                    options.forEach { (id, label) ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(rect)
                                .clickable { offerDelivery = id == "yes" }
                                .padding(Spacing.XS),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = if (id == "yes") offerDelivery else !offerDelivery, onClick = null)
                            AppText(label, variant = TextType.Label)
                        }
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .alpha(if (offerDelivery) 1f else .5f)
                    .background(
                        colorScheme.surface,
                        rect
                    )
                    .padding(Spacing.MD),
                verticalArrangement = Arrangement.spacedBy(Spacing.MD)
            ) {
                AppText(
                    "Delivery zone Pricing",
                    variant = TextType.Label
                )
                Column(
                    Modifier.weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                    ) {
                        if (deliveryZones.isEmpty()){
                            item {
                                AppText(
                                    "No delivery zones specified. \nAdd free or priced delivery zones here.",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(slateContainer, rect)
                                        .padding(Spacing.MD)
                                        .align(Alignment.CenterHorizontally),
                                    color = onSlateContainer
                                )
                            }
                        }
                        if(freeDeliveryZone != null){
                            val (distance, _, distanceUnits, deliveryTime, timeUnits) = freeDeliveryZone
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(successContainerLight, rect)
                                        .padding(Spacing.MD),
                                ) {
                                    AppText(
                                        "Free delivery up to ${distance.formatWithCommas()} $distanceUnits in $deliveryTime $timeUnits",
                                        variant = TextType.Label,
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        color = onSuccessContainerLight
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(onClick = {}) {
                                            Icon(
                                                painter = painterResource(R.drawable.edit),
                                                contentDescription = "Edit delivery zone",
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.size(IconSize.NORMAL)
                                            )
                                        }
                                        IconButton(onClick = {}) {
                                            Icon(
                                                painter = painterResource(R.drawable.trash),
                                                contentDescription = "Delete delivery zone",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(IconSize.NORMAL)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        val pricedZones = deliveryZones.filter { it.price > 0f }
                        items(pricedZones.size) { index ->
                            DeliveryZoneCard(zone = pricedZones[index], navController = navController)
                        }
                    }
                }
                AppButton(
                    "Add delivery zone",
                    onClick = { navController.navigate("store/settings/shipping_&_deliveries/create_zone") },
                    type = ButtonType.Primary,
                    enabled = offerDelivery
                )
            }
        }
    }
}

@Composable
fun DeliveryZoneCard(zone: DeliveryZone, navController: NavController){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(.5f), MaterialTheme.shapes.medium)
            .padding(Spacing.MD),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
        ) {
            AppText("Up to ${zone.distance.formatWithCommas()} ${zone.distanceUnits}", variant = TextType.Label)
            AppText(
                "${zone.deliveryTime} ${zone.timeUnits}",
                variant = TextType.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            AppText("UGX ${zone.price.formatWithCommas()}", variant = TextType.Label)
            Row(
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(R.drawable.edit),
                        contentDescription = "Edit delivery zone",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(IconSize.NORMAL)
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(R.drawable.trash),
                        contentDescription = "Delete delivery zone",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(IconSize.NORMAL)
                    )
                }
            }
        }
    }
}