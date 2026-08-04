package com.example.doline.views.screens.store.home.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.to12HourTimeString
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportingSettingsScreen(navController: NavController){

    val rect = MaterialTheme.shapes.medium
    val rectXS = MaterialTheme.shapes.extraSmall
    val colorScheme = MaterialTheme.colorScheme
    val circle = RoundedCornerShape(Rounding.FULL)

    val timePickerState = rememberTimePickerState(
        initialHour = 18,
        initialMinute = 0,
        is24Hour = false,
    )
    var isDailyReportEnabled by rememberSaveable {
        mutableStateOf(true)
    }
    var isWeeklyReportEnabled by rememberSaveable {
        mutableStateOf(false)
    }
    var isMonthlyReportEnabled by rememberSaveable {
        mutableStateOf(true)
    }
    var isTimePickerOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var selectedDay by remember {
        mutableStateOf("Sat")
    }
    var selectedMonthDay by remember {
        mutableStateOf("last")
    }

    var emailRecipients = mutableListOf("admin@business.ug", "manager@business.ug")


    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Reports & Analytics", variant = TextType.Heading, maxLines = 1) },
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
                            colorScheme.surface,
                            rect
                        )
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    AppText(
                        "Automatic reporting schedule",
                        variant = TextType.Label
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                colorScheme.background.copy(.7f),
                                rect
                            )
                            .padding(Spacing.XL),
                        verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText("Daily close-out report")
                            Switch(checked = isDailyReportEnabled, onCheckedChange = { isDailyReportEnabled = it })
                        }
                        Spacer(Modifier.height(10.dp))
                        AppText("Time to send", modifier = Modifier.alpha(if(isDailyReportEnabled) 1f else .5f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isDailyReportEnabled) { isTimePickerOpen = true }
                                .border(
                                    .5.dp,
                                    colorScheme.onBackground.copy(if(isDailyReportEnabled) 1f else .5f),
                                    rect
                                )
                                .padding(Spacing.MD)
                                .alpha(if(isDailyReportEnabled) 1f else .5f),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText(LocalTime.of(timePickerState.hour, timePickerState.minute).to12HourTimeString())
                            Icon(
                                painter = painterResource(R.drawable.clock),
                                contentDescription = "Select time",
                                tint = colorScheme.onBackground,
                                modifier = Modifier
                                    .size(IconSize.NORMAL)
                            )
                        }
                        if(isTimePickerOpen){
                            TimePickerDialog(
                                onDismissRequest = { isTimePickerOpen = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        isTimePickerOpen = false
                                    }) {
                                        AppText("Ok", color = colorScheme.primary)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {isTimePickerOpen = false}) {
                                        AppText("Cancel")
                                    }
                                },
                                title = { AppText("Select time", modifier = Modifier.padding(20.dp)) },
                                content = {
                                    TimePicker(timePickerState, modifier = Modifier.fillMaxWidth())
                                },
                                modifier = Modifier.padding(40.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                colorScheme.background.copy(.7f),
                                rect
                            )
                            .padding(Spacing.XL),
                        verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText("Weekly summary report")
                            Switch(checked = isWeeklyReportEnabled, onCheckedChange = { isWeeklyReportEnabled = it })
                        }
                        Spacer(Modifier.height(10.dp))
                        AppText("Send on", modifier = Modifier.alpha(if(isWeeklyReportEnabled) 1f else .5f))
                       FlowRow(
                           modifier = Modifier
                               .fillMaxWidth()
                               .alpha(if(isWeeklyReportEnabled) 1f else .5f),
                           horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
                           verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                       ) {
                           val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                           days.forEach { day ->
                               val isSelected = selectedDay == day
                               AppText(
                                   day,
                                   color = if (isSelected) colorScheme.onPrimary else colorScheme.onBackground,
                                   modifier = Modifier
                                       .alpha(if(isWeeklyReportEnabled) 1f else .5f)
                                       .widthIn(min=50.dp)
                                       .background(
                                           if (isSelected) colorScheme.primary else colorScheme.background,
                                           rectXS
                                       )
                                       .clickable(enabled = isWeeklyReportEnabled){ selectedDay = day }
                                       .padding(Spacing.XS)

                               )
                           }
                       }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                colorScheme.background.copy(.7f),
                                rect
                            )
                            .padding(Spacing.XL),
                        verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText("Monthly report")
                            Switch(checked = isMonthlyReportEnabled, onCheckedChange = { isMonthlyReportEnabled = it })
                        }
                        Spacer(Modifier.height(10.dp))
                        AppText("Send on Day", modifier = Modifier.alpha(if(isMonthlyReportEnabled) 1f else .5f))
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if(isMonthlyReportEnabled) 1f else .5f),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
                            verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                        ) {
                            AppText(
                                "First Day",
                                color = if (selectedMonthDay == "first") colorScheme.onPrimary else colorScheme.onBackground,
                                modifier = Modifier
                                    .widthIn(min=35.dp)
                                    .background(
                                        if (selectedMonthDay == "first") colorScheme.primary else colorScheme.background,
                                        rectXS
                                    )
                                    .clickable(enabled = isMonthlyReportEnabled) { selectedMonthDay = "first" }
                                    .padding(Spacing.XS)
                            )
                            AppText(
                                "Last Day",
                                color = if (selectedMonthDay == "last") colorScheme.onPrimary else colorScheme.onBackground,
                                modifier = Modifier
                                    .widthIn(min=35.dp)
                                    .background(
                                        if (selectedMonthDay == "last") colorScheme.primary else colorScheme.background,
                                        rectXS
                                    )
                                    .clickable(enabled = isMonthlyReportEnabled) { selectedMonthDay = "last" }
                                    .padding(Spacing.XS)
                            )
                            TextInputField(
                                value = if(selectedMonthDay != "last" && selectedMonthDay != "first" && selectedMonthDay.isNotEmpty())
                                    selectedMonthDay
                                else "",
                                onValueChange = { selectedMonthDay = it },
                                enabled = isMonthlyReportEnabled,
                                placeHolder = "Or specify Date",
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                            )
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
                        "Email Recipients",
                        variant = TextType.Label
                    )
                    emailRecipients.forEach { recipient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colorScheme.background.copy(.5f), circle)
                                .padding(Spacing.MD, 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText(
                                recipient,
                                color = colorScheme.onBackground
                            )
                            IconButton(onClick = {}) {
                                Icon(
                                    painter = painterResource(R.drawable.trash),
                                    contentDescription = "Remove recipient",
                                    modifier = Modifier.size(IconSize.NORMAL),
                                    tint = colorScheme.onBackground
                                )
                            }
                        }

                    }
                    Spacer(Modifier.height(10.dp))
                    TextInputField(
                        value = "",
                        onValueChange = {},
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                        ),
                    )
                    AppButton("Add Recipient", onClick = {}, type = ButtonType.Primary)
                }
            }
            item {
                Spacer(Modifier.height(0.dp))
            }
        }
    }

}