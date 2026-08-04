package com.example.doline.views.screens.store.home.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.models.DayStatus
import com.example.doline.data.models.workingDays
import com.example.doline.to12HourTimeString
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.ui.theme.onSuccessContainerLight
import com.example.doline.ui.theme.successContainerLight
import com.example.doline.views.components.AppText
import com.example.doline.views.components.TextType

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(navController: NavController){

    val colorScheme = MaterialTheme.colorScheme
    val circle = RoundedCornerShape(Rounding.FULL)


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { AppText("Working hours", variant = TextType.Heading, maxLines = 1) },
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
        contentWindowInsets = WindowInsets.navigationBars
    ) {p ->
        Column(
            modifier = Modifier
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.MD),
            verticalArrangement = Arrangement.spacedBy(Spacing.LG)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SM),
                modifier = Modifier
                    .fillMaxWidth()
                    .consumeWindowInsets(WindowInsets.navigationBars)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Rounding.MD))
                ) {
                    workingDays.forEachIndexed { index, (day, status, open24Hours, open, close) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if(index % 2 == 0)
                                        colorScheme.surface
                                    else
                                        colorScheme.surface.copy(.7f)
                                ).padding(Spacing.MD),
                            verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppText(day, variant = TextType.Label)
                                Switch(
                                    checked = status == DayStatus.OPEN,
                                    modifier = Modifier.height(10.dp),
                                    onCheckedChange = { isChecked ->
                                        workingDays[index] = workingDays[index].copy(
                                            status = if (isChecked) DayStatus.OPEN else DayStatus.CLOSED
                                        )
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(
                                        if (status == DayStatus.OPEN && !open24Hours) 1f else 0.4f
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppText(open.to12HourTimeString(), variant = TextType.Label)
                                AppText("-", variant = TextType.Heading)
                                AppText(close.to12HourTimeString(), variant = TextType.Label)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(
                                        if (status == DayStatus.OPEN) 1f else 0.4f
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppText("Open 24 Hours", variant = TextType.Label)
                                Switch(
                                    checked = open24Hours,
                                    enabled = status == DayStatus.OPEN,
                                    onCheckedChange = { isChecked ->
                                        workingDays[index] = workingDays[index].copy(open24Hours = isChecked)
                                    }
                                )
                            }

                        }
                    }
                }
                Spacer(Modifier.height(70.dp))
            }
        }
    }
}