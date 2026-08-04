package com.example.doline.views.screens.store.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.models.BarcodeScanner
import com.example.doline.data.models.CashDrawer
import com.example.doline.data.models.ReceiptPrinter
import com.example.doline.data.models.WeighingScale
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.TextType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosSettingsScreen(navController: NavController){
    val colorScheme = MaterialTheme.colorScheme
    val rect = MaterialTheme.shapes.medium

    val printers = mutableListOf(
        ReceiptPrinter(
            "Zebra GK420"
        ),
        ReceiptPrinter(
            "Star TSP143",
            false
        )
    )
    val drawers = mutableListOf<CashDrawer>()
    val scales = mutableListOf<WeighingScale>()
    val scanners = mutableListOf<BarcodeScanner>()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("POS Hardware devices", variant = TextType.Heading, maxLines = 1) },
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
                        .background(colorScheme.surface, rect)
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.XS),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(
                            "Receipt Printers",
                            variant = TextType.Label
                        )
                        Spacer(Modifier.width(10.dp))
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.plus),
                                contentDescription = null,
                                modifier = Modifier.size(IconSize.BIG),
                                tint = colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    if(printers.isEmpty()){
                        AppText(
                            "No printers connected!",
                            modifier = Modifier.wrapContentSize(),
                            color = colorScheme.onBackground.copy(.6f)
                        )
                    }else{
                        printers.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        colorScheme.background.copy(.4f),
                                        rect
                                    )
                                    .padding(Spacing.MD, Spacing.XXS),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppText(
                                    p.name,
                                    color = if (p.isActive) colorScheme.onBackground else colorScheme.onBackground.copy(.5f)
                                )
                                IconButton(onClick = {}) {
                                    Icon(
                                        painter = painterResource(R.drawable.more),
                                        contentDescription = null,
                                        modifier = Modifier.size(IconSize.NORMAL),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surface, rect)
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.XS),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(
                            "Cash Drawers",
                            variant = TextType.Label
                        )
                        Spacer(Modifier.width(10.dp))
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.plus),
                                contentDescription = null,
                                modifier = Modifier.size(IconSize.BIG),
                                tint = colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    if(drawers.isEmpty()){
                        AppText(
                            "No drawers connected!",
                            modifier = Modifier.wrapContentSize(),
                            color = colorScheme.onBackground.copy(.6f)
                        )
                    }else{
                        drawers.forEach { d ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        colorScheme.background.copy(.4f),
                                        rect
                                    )
                                    .padding(Spacing.MD, Spacing.XXS),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppText(
                                    d.name,
                                    color = if (d.isActive) colorScheme.onBackground else colorScheme.onBackground.copy(.5f)
                                )
                                IconButton(onClick = {}) {
                                    Icon(
                                        painter = painterResource(R.drawable.more),
                                        contentDescription = null,
                                        modifier = Modifier.size(IconSize.NORMAL),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surface, rect)
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.XS),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(
                            "Weighing Scales",
                            variant = TextType.Label
                        )
                        Spacer(Modifier.width(10.dp))
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.plus),
                                contentDescription = null,
                                modifier = Modifier.size(IconSize.BIG),
                                tint = colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    if(scales.isEmpty()){
                        AppText(
                            "No weighing scales connected!",
                            modifier = Modifier.wrapContentSize(),
                            color = colorScheme.onBackground.copy(.6f)
                        )
                    }else{
                        scales.forEach { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        colorScheme.background.copy(.4f),
                                        rect
                                    )
                                    .padding(Spacing.MD, Spacing.XXS),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppText(
                                    s.name,
                                    color = if (s.isActive) colorScheme.onBackground else colorScheme.onBackground.copy(.5f)
                                )
                                IconButton(onClick = {}) {
                                    Icon(
                                        painter = painterResource(R.drawable.more),
                                        contentDescription = null,
                                        modifier = Modifier.size(IconSize.NORMAL),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surface, rect)
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.XS),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(
                            "Barcode Scanners",
                            variant = TextType.Label
                        )
                        Spacer(Modifier.width(10.dp))
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.plus),
                                contentDescription = null,
                                modifier = Modifier.size(IconSize.BIG),
                                tint = colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    if(scanners.isEmpty()){
                        AppText(
                            "No barcode scanners connected!",
                            modifier = Modifier.wrapContentSize(),
                            color = colorScheme.onBackground.copy(.6f)
                        )
                    }else{
                        scanners.forEach { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        colorScheme.background.copy(.4f),
                                        rect
                                    )
                                    .padding(Spacing.MD, Spacing.XXS),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppText(
                                    s.name,
                                    color = if (s.isActive) colorScheme.onBackground else colorScheme.onBackground.copy(.5f)
                                )
                                IconButton(onClick = {}) {
                                    Icon(
                                        painter = painterResource(R.drawable.more),
                                        contentDescription = null,
                                        modifier = Modifier.size(IconSize.NORMAL),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(0.dp))
            }
        }
    }
}