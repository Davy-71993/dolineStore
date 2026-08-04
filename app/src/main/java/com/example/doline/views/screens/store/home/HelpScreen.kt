package com.example.doline.views.screens.store.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.capitalize
import com.example.doline.data.models.faqCategories
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.ui.theme.onSuccessContainerLight
import com.example.doline.ui.theme.successContainerLight
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.TextType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavController){
    val colorScheme = MaterialTheme.colorScheme
    val rect = MaterialTheme.shapes.medium
    val circle = RoundedCornerShape(Rounding.FULL)

    var activeFaqCategory by remember {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Help & Feedback", variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier.padding(vertical = 0.dp).shadow(Spacing.MD),
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
            )
        }
    ){ ip ->
        LazyColumn(
            Modifier.padding(Spacing.MD,ip.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(Spacing.XL)
        ) {
            item {
                Spacer(Modifier.height(0.dp))
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            colorScheme.surface.copy(.5f),
                            rect
                        )
                        .border(1.5.dp, colorScheme.onBackground.copy(.1f), rect)
                        .clip(rect)
                        .clickable{ navController.navigate("store/help/search")}
                        .padding(Spacing.MD),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = "Search",
                        tint = colorScheme.onBackground.copy(.5f),
                        modifier = Modifier.size(IconSize.NORMAL)
                    )
                    AppText("Search help articles", color = colorScheme.onBackground.copy(.5f))
                }
            }
            item {
                AppText("Quick support", variant = TextType.Label)
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(rect)
                            .background(colorScheme.surface.copy(.6f), rect)
                            .clickable {}
                            .padding(Spacing.MD),
                        verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
                    ){
                       Box(
                           Modifier
                               .background(colorScheme.primaryContainer, rect)
                               .padding(Spacing.MD)
                               .wrapContentSize()
                               .align(Alignment.CenterHorizontally)
                       ) {
                           Icon(
                               painter = painterResource(R.drawable.ai),
                               contentDescription = "Ask Lilli",
                               tint = colorScheme.primary,
                               modifier = Modifier.size(IconSize.NORMAL)
                           )
                       }
                        AppText("AI Assistant", variant = TextType.Small, modifier = Modifier.align(Alignment.CenterHorizontally))
                        AppText("Ask Lilli", variant = TextType.Label, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .clip(rect)
                            .background(colorScheme.surface.copy(.6f), rect)
                            .clickable {}
                            .padding(Spacing.MD),
                        verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
                    ){
                        Box(
                            Modifier
                                .background(successContainerLight, rect)
                                .padding(Spacing.MD)
                                .wrapContentSize()
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.call),
                                contentDescription = "Call Us",
                                tint = onSuccessContainerLight,
                                modifier = Modifier.size(IconSize.NORMAL)
                            )
                        }
                        AppText("Call Us", variant = TextType.Small, modifier = Modifier.align(Alignment.CenterHorizontally))
                        AppText("+256300790380", variant = TextType.Label, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
            item {
                AppText("Feedback", variant = TextType.Label)
                Spacer(Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surface.copy(.6f), rect)
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
                ) {
                  Row(
                      modifier= Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
                      verticalAlignment = Alignment.CenterVertically
                  ) {
                      Box(
                          modifier = Modifier
                              .size(60.dp)
                              .background(
                                  colorScheme.primary,
                                  circle
                              )
                      ){
                          Icon(
                              painter = painterResource(R.drawable.marketing),
                              contentDescription = null,
                              tint = colorScheme.onPrimary,
                              modifier = Modifier.size(IconSize.BIG).align(Alignment.Center)
                          )
                      }

                      Column(
                          modifier = Modifier.weight(1f)
                      ) {
                          AppText("Help us improve", variant = TextType.Label, color = colorScheme.primary)
                          AppText("Suggest a feature or report a bug.", variant = TextType.Small)
                      }
                    }
                    Spacer(Modifier.height(10.dp))
                    AppButton("Submit Feedback", {navController.navigate("store/help/feedback")}, type = ButtonType.Primary)
                }
            }
            item {
                AppText("Frequently Asked Questions", variant = TextType.Label)
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                ) {
                    faqCategories.forEach {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(rect)
                                .background(
                                    colorScheme.surface.copy(.5f),
                                    rect
                                ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        activeFaqCategory = if (activeFaqCategory == it.name) {
                                            null
                                        } else {
                                            it.name
                                        }
                                    }
                                    .padding(Spacing.MD),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(it.icon),
                                    contentDescription = null,
                                    tint = colorScheme.onBackground,
                                    modifier = Modifier.size(IconSize.NORMAL)
                                )
                                AppText(
                                    it.name.capitalize(),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    painter = painterResource(if(activeFaqCategory == it.name) R.drawable.chev_up else R.drawable.chev_down),
                                    contentDescription = null,
                                    modifier = Modifier.size(IconSize.NORMAL)
                                )
                            }
                            if(activeFaqCategory == it.name){
                                HorizontalDivider()
                                AppText(
                                    "${it.description}  >",
                                    modifier = Modifier
                                        .clickable { navController.navigate("store/help/faqs/${it.name}")}
                                        .padding(Spacing.MD)
                                )
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