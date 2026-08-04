package com.example.doline.views.screens.store.home



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.ui.theme.successContainerLight
import com.example.doline.ui.theme.successDark
import com.example.doline.ui.theme.successLight
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.TextType
import com.example.doline.views.components.WeeklyPerformanceChart
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController, viewmodel: ReportsScreenViewModel){
    var period by remember {
        mutableStateOf(
            ReportPeriod.THISWEEK
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Reports", variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier.padding(vertical = 0.dp),
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    subtitleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
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
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.share),
                            contentDescription = "Share",
                            Modifier.size(IconSize.NORMAL)
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.download),
                            contentDescription = "Download",
                            Modifier.size(IconSize.NORMAL)
                        )
                    }
                }
            )
        }
    ) {
            innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.MD)
                ) {
                    item {
                        AppText("This Week", variant = TextType.Label,
                            modifier= Modifier
                                .padding(start = Spacing.SM)
                                .background(
                                    if (period == ReportPeriod.THISWEEK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.FULL)
                                )
                                .padding(horizontal = Spacing.MD, vertical = Spacing.XXS)
                                .clickable { period = ReportPeriod.THISWEEK },
                            color = if(period == ReportPeriod.THISWEEK) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    item {
                        AppText("This Month", variant = TextType.Label,
                            modifier= Modifier
                                .padding(start = Spacing.SM)
                                .background(
                                    if (period == ReportPeriod.THISMONTH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.FULL)
                                )
                                .padding(horizontal = Spacing.MD, vertical = Spacing.XXS)
                                .clickable { period = ReportPeriod.THISMONTH },
                            color = if(period == ReportPeriod.THISMONTH) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    item {
                        AppText("Custom Period", variant = TextType.Label,
                            modifier= Modifier
                                .padding(start = Spacing.SM)
                                .background(
                                    if (period == ReportPeriod.CUSTOM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.FULL)
                                )
                                .padding(horizontal = Spacing.MD, vertical = Spacing.XXS)
                                .clickable { period = ReportPeriod.CUSTOM },
                            color = if(period == ReportPeriod.CUSTOM) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(Rounding.MD)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.SM),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        AppText("Sales Trend")
                        WeeklyPerformanceChart()
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding()
                                .shadow(Spacing.XXS, shape = RoundedCornerShape(Rounding.MD))
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.MD)
                                )
                                .padding(Spacing.MD)
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                        ) {
                            AppText(
                                text = "Total Sales",
                                variant = TextType.Label,
                                color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                            )
                            AppText(
                                text = "UGX 45.8M",
                                variant = TextType.Heading,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier
                                    .padding()
                                    .background(
                                        successContainerLight,
                                        shape = RoundedCornerShape(Rounding.FULL)
                                    )
                                    .padding(horizontal = Spacing.SM, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.trending_up),
                                    contentDescription = "change",
                                    tint = successLight,
                                    modifier = Modifier.size(IconSize.SMALL)
                                )
                                AppText("12%", variant = TextType.Small, color = successLight)
                            }
                        }

                        Column(
                            modifier = Modifier
                                .padding()
                                .shadow(Spacing.XXS, shape = RoundedCornerShape(Rounding.MD))
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.MD)
                                )
                                .padding(Spacing.MD)
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                        ) {
                            AppText(
                                text = "Total Orders",
                                variant = TextType.Label,
                                color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                            )
                            AppText(
                                text = "1,284",
                                variant = TextType.Heading,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier
                                    .padding()
                                    .background(
                                        successContainerLight,
                                        shape = RoundedCornerShape(Rounding.FULL)
                                    )
                                    .padding(horizontal = Spacing.SM, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.trending_up),
                                    contentDescription = "change",
                                    tint = successLight,
                                    modifier = Modifier.size(IconSize.SMALL)
                                )
                                AppText("5%", variant = TextType.Small, color = successLight)
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding()
                                .shadow(Spacing.XXS, shape = RoundedCornerShape(Rounding.MD))
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.MD)
                                )
                                .padding(Spacing.MD)
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                        ) {
                            AppText(
                                text = "Avg. Order Value",
                                variant = TextType.Label,
                                color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                            )
                            AppText(
                                text = "UGX 35,600",
                                variant = TextType.Heading,
                                color = MaterialTheme.colorScheme.primary
                            )
                            AppText("vs 32,100 last week", variant = TextType.Small, color = MaterialTheme.colorScheme.onBackground.copy(.6f))
                        }

                        Column(
                            modifier = Modifier
                                .padding()
                                .shadow(Spacing.XXS, shape = RoundedCornerShape(Rounding.MD))
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.MD)
                                )
                                .padding(Spacing.MD)
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                        ) {
                            AppText(
                                text = "Profit Margin",
                                variant = TextType.Label,
                                color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                            )
                            AppText(
                                text = "28.4%",
                                variant = TextType.Heading,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(
                                modifier = Modifier.height(2.dp)
                            )
                            LinearProgressIndicator(
                                progress = { 0.284f },
                                gapSize = 0.dp,
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding()
                                .shadow(Spacing.XXS, shape = RoundedCornerShape(Rounding.MD))
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.MD)
                                )
                                .padding(Spacing.MD)
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                        ) {
                            AppText(
                                text = "Foot Traffic",
                                variant = TextType.Label,
                                color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                            )
                            AppText(
                                text = "892",
                                variant = TextType.Heading,
                                color = MaterialTheme.colorScheme.primary,
                                modifier= Modifier.align(Alignment.CenterHorizontally)
                            )
                            Row(
                                modifier = Modifier
                                    .padding()
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(Rounding.FULL)
                                    )
                                    .padding(horizontal = Spacing.SM, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.trending_down),
                                    contentDescription = "change",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(IconSize.SMALL)
                                )
                                AppText("2.1%", variant = TextType.Small, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        Column(
                            modifier = Modifier
                                .padding()
                                .shadow(Spacing.XXS, shape = RoundedCornerShape(Rounding.MD))
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.MD)
                                )
                                .padding(Spacing.MD)
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                        ) {
                            AppText(
                                text = "Stock Turn Over",
                                variant = TextType.Label,
                                color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                            )
                            AppText(
                                text = "4.2x",
                                variant = TextType.Heading,
                                color = MaterialTheme.colorScheme.primary,
                                modifier= Modifier.align(Alignment.CenterHorizontally)
                            )

                            AppText("Industry avg: 3.8x", variant = TextType.Small, color = MaterialTheme.colorScheme.onBackground.copy(.6f))

                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(Rounding.MD)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.SM),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        AppText("Sales By Category")
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.SM)) {
                            listOf(
                                SalesByCategory("Electronics", 20f, "598K"),
                                SalesByCategory("Clothing", 19.6f, "450K"),
                                SalesByCategory("Groceries", 60.4f, "1.2M")
                            ).forEachIndexed { index, category ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    AppText(category.name, variant = TextType.Label)
                                    AppText("${category.percentage}%", variant = TextType.Label, color = MaterialTheme.colorScheme.primary)
                                    AppText("UGX ${category.amount}", variant = TextType.Label, color = MaterialTheme.colorScheme.primary)
                                }
                                if (index < 2) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(Rounding.MD)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.SM),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        AppText("Top 5 Products")
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.SM)) {
                            listOf(
                                TopProduct("Smart TV 55\"", "Electronics", "2.56M", 0.8f),
                                TopProduct("Wireless Earbuds", "Electronics", "1.2M", 0.6f),
                                TopProduct("Running Shoes", "Clothing", "950K", 0.5f),
                                TopProduct("Organic Honey", "Groceries", "800K", 0.4f),
                                TopProduct("Leather Wallet", "Accessories", "600K", 0.3f)
                            ).forEachIndexed { index, product ->
                                Row() {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.secondary,
                                                shape = RoundedCornerShape(Rounding.SM)
                                            )
                                            .size(40.dp)
                                            .align(Alignment.CenterVertically)
                                            .padding(Spacing.XXS),
                                    ){
                                        AppText(
                                            text = (index + 1).toString(),
                                            variant = TextType.Label,
                                            color = MaterialTheme.colorScheme.onSecondary,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                    Column(modifier = Modifier
                                        .padding(start = Spacing.SM)
                                        .weight(1f)) {
                                        AppText(
                                            text = product.productName,
                                            variant = TextType.Label,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )
                                        AppText(product.category, variant = TextType.Small, color = MaterialTheme.colorScheme.onBackground.copy(.6f))
                                    }
                                    Column(modifier = Modifier
                                        .padding(start = Spacing.SM)
                                        .weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.XXS)) {
                                        AppText("UGX ${product.sales}", variant = TextType.Label,
                                        )
                                        LinearProgressIndicator(
                                            progress = { product.percentage },
                                            gapSize = 0.dp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        AppText("Inventory Health", variant = TextType.Label)

                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.MD)) {
                            listOf(
                                "Low Stock" to "12",
                                "Out of Stock" to "3",
                                "Overstocked" to "5"
                            ).forEach { (label, count) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(Rounding.SM)
                                        )
                                        .padding(Spacing.MD),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                                ) {
                                    AppText(
                                        label,
                                        variant = TextType.Small,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onBackground.copy(.7f)
                                    )
                                    AppText(
                                        count,
                                        variant = TextType.Heading,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.MD)) {
                            listOf(
                                "Total Value" to "102.58M",
                                "Turn Over" to "4.2x",
                            ).forEach { (label, count) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(Rounding.SM)
                                        )
                                        .padding(Spacing.MD),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                                ) {
                                    AppText(
                                        label,
                                        variant = TextType.Small,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onBackground.copy(.7f)
                                    )
                                    AppText(
                                        count,
                                        variant = TextType.Heading,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(Rounding.MD),
                        border = BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            AppText(
                                "Need Restocking",
                                variant = TextType.Label,
                                modifier = Modifier.padding(Spacing.MD)
                            )

                            listOf(
                                "Arabic Coffee Beans 1Kg" to "3 Packets",
                                "Electric Kettle 1.5L" to "2 Units",
                            ).forEach { (label, count) ->
                                HorizontalDivider()
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.MD)
                                ) {
                                    AppText(
                                        label,
                                        variant = TextType.Label,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.primary.copy(.7f)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AppText(
                                            "$count left.",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        AppButton(
                                            "Reorder",
                                            onClick = { /* TODO: Implement reorder logic */ },
                                            type = ButtonType.Primary,
                                            modifier = Modifier
                                                .width(140.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.MD, horizontal = Spacing.SM)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(Rounding.MD),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(Spacing.MD),
                            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ai),
                                    contentDescription = "AI insights",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(IconSize.BIG)
                                )
                                AppText(
                                    "Performance Insights",
                                    variant = TextType.Heading,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                            ) {
                                Card(
                                        modifier = Modifier
                                            .weight(1f),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primary.copy(.5f)
                                        ),
                                        shape = RoundedCornerShape(Rounding.MD),
                                ){
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(Spacing.MD),
                                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                                    ) {
                                        AppText(
                                            "Customer Conversion",
                                            variant = TextType.Small,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            maxLines = 1
                                        )
                                        AppText(
                                            "18.4%",
                                            variant = TextType.Heading,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.XXS),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.arrow_up),
                                                contentDescription = "increase",
                                                modifier = Modifier.size(IconSize.SMALL),
                                                tint = successDark
                                            )
                                            AppText(
                                                "2.4% vs Avg.",
                                                variant = TextType.Small,
                                                color = successDark
                                            )
                                        }
                                    }
                                }
                                Card(
                                    modifier = Modifier
                                        .weight(1f),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(.5f)
                                    ),
                                    shape = RoundedCornerShape(Rounding.MD),
                                ){
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(Spacing.MD),
                                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                                    ) {
                                        AppText(
                                            "Avg. Cart Size",
                                            variant = TextType.Small,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            maxLines = 1
                                        )
                                        AppText(
                                            "4.2 Items",
                                            variant = TextType.Heading,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.XXS),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.arrow_up),
                                                contentDescription = "increase",
                                                modifier = Modifier.size(IconSize.SMALL),
                                                tint = successDark
                                            )
                                            AppText(
                                                "0.8 Items",
                                                variant = TextType.Small,
                                                color = successDark
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@HiltViewModel
class ReportsScreenViewModel @Inject constructor(): ViewModel(){

}

enum class ReportPeriod {
   THISWEEK, THISMONTH, CUSTOM
}

data class SalesByCategory(
    val name: String,
    val percentage: Float,
    val amount: String
)

data class TopProduct(
    val productName: String,
    val category: String,
    val sales: String,
    val percentage: Float
)