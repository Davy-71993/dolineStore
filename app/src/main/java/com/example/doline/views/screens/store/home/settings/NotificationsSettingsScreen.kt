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
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.models.notificationTypes
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import com.example.doline.views.components.TextType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(navController: NavController){

    val colorScheme = MaterialTheme.colorScheme
    val rect = RoundedCornerShape(Rounding.MD)
    val circle = RoundedCornerShape(Rounding.FULL)

    var selected by remember {
        mutableStateOf("none")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Alerts & Notifications", variant = TextType.Heading, maxLines = 1) },
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
                        .padding(Spacing.MD)
                ) {
                    AppText("Preferences", variant = TextType.Heading)
                    AppText(
                        "Choose notifications to receive.",
                        color = colorScheme.onBackground.copy(.6f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    notificationTypes.forEachIndexed { _, entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.XXS),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText(entry.type, variant = TextType.Label)
                            Switch(checked = entry.enabled, onCheckedChange = {})
                        }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surface, rect)
                        .padding(Spacing.MD)
                ) {
                    AppText("Push notifications", variant = TextType.Heading)
                    AppText(
                        "Control push notifications.",
                        color = colorScheme.onBackground.copy(.6f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(rect)
                            .clickable {selected = "all"}
                            .padding(Spacing.XS),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected == "all", onClick = null)
                        AppText("All notifications", variant = TextType.Label)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(rect)
                            .clickable {selected = "urgent"}
                            .padding(Spacing.XS),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected == "urgent", onClick = null)
                        AppText("Urgent only", variant = TextType.Label)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(rect)
                            .clickable {selected = "none"}
                            .padding(Spacing.XS),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected == "none", onClick = null)
                        AppText("Off", variant = TextType.Label)
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surface, rect)
                        .padding(Spacing.MD)
                ) {
                    AppText("Alert sound", variant = TextType.Heading)
                    AppText(
                        "Choose the alert tone",
                        color = colorScheme.onBackground.copy(.6f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.XXS),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText("Enable vibration", variant = TextType.Label)
                        Switch(checked = false, onCheckedChange = {})
                    }
                }
            }
            item {
                Spacer(Modifier.height(0.dp))
            }
        }
    }

}