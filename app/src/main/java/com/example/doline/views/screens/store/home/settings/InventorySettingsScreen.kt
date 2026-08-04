package com.example.doline.views.screens.store.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import com.example.doline.views.components.TextType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventorySettingsScreen(navController: NavController){

    val rect = MaterialTheme.shapes.medium

    var autoReOrder by remember {
        mutableStateOf(false)
    }
    var negativeStockPolicy by remember {
        mutableStateOf("ask")
    }
    var autoGenerateBarcodes by remember { mutableStateOf(false) }
    var allowBarcodeScanning by remember { mutableStateOf(true) }
    var printBarcodeLabels by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Inventory Settings", variant = TextType.Heading, maxLines = 1) },
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
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.shapes.medium
                        )
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    AppText(
                        "Negative stock policy",
                        variant = TextType.Label
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.XXS),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { negativeStockPolicy = "allow"}
                            .padding(Spacing.XXS)
                            .clip(rect)
                    ) {
                        RadioButton(selected = negativeStockPolicy == "allow", onClick = null)
                        AppText("Allow negative stock")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.XXS),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { negativeStockPolicy = "deny"}
                            .padding(Spacing.XXS)
                            .clip(rect)
                    ) {
                        RadioButton(selected = negativeStockPolicy == "deny", onClick = null)
                        AppText("Deny negative stock")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.XXS),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { negativeStockPolicy = "ask"}
                            .padding(Spacing.XXS)
                            .clip(rect)
                    ) {
                        RadioButton(selected = negativeStockPolicy == "ask", onClick = null)
                        AppText("Ask manager's approval")
                    }

                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.shapes.medium
                        )
                        .padding(Spacing.MD),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppText(
                        text = "Auto re-order",
                        variant = TextType.Label
                    )
                    Switch(checked = autoReOrder, onCheckedChange = { checked -> autoReOrder = checked})
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.shapes.medium
                        )
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    AppText(
                        "Barcode settings",
                        variant = TextType.Label
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.XXS)
                            .clip(rect)
                    ) {
                        AppText("Auto generate barcodes")
                        Switch(checked = autoGenerateBarcodes, onCheckedChange = { autoGenerateBarcodes = it })
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.XXS)
                            .clip(rect)
                    ) {
                        AppText("Allow barcode scanning")
                        Switch(checked = allowBarcodeScanning, onCheckedChange = { allowBarcodeScanning = it })
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.XXS)
                            .clip(rect)
                    ) {
                        AppText("Print barcode labels")
                        Switch(checked = printBarcodeLabels, onCheckedChange = { printBarcodeLabels = it })
                    }

                }
            }
            item {
                Spacer(Modifier.height(0.dp))
            }
        }
    }

}