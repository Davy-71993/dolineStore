package com.example.doline.views.screens.store.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.models.ActiveSession
import com.example.doline.data.models.Staff
import com.example.doline.formatWithCommas
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.TextType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffSettingsScreen(navController: NavController){

    val staffs = mutableListOf(Staff(
            "Egessa David Wafula",
            "EGWafula",
            "123456",
            "Director"
        ))
    val activeSessions = mutableListOf(
        ActiveSession(
            staffs[0],
            "Apple Macbook Pro",
            20f,
            "Mbuya"
        ),
        ActiveSession(
            staffs[0],
            "Samsung Galaxy NotesEntity 10",
            10f,
            "Kireka"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Staff & Access", variant = TextType.Heading, maxLines = 1) },
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
                .padding(vertical = p.calculateTopPadding(), horizontal = Spacing.SM)
                .padding(vertical = Spacing.XL),
            verticalArrangement = Arrangement.spacedBy(Spacing.XL)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.XL)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                ) {
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
                            AppText("Active Sessions", variant = TextType.Label)
                            activeSessions.forEach { session ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.background.copy(.5f),
                                            MaterialTheme.shapes.medium
                                        )
                                        .padding(Spacing.MD),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column{
                                        AppText(session.device)
                                        AppText(
                                           "${session.device} • ${session.duration.formatWithCommas()} Minutes ago",
                                           variant = TextType.Small,
                                           color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                                       )
                                    }
                                    IconButton(onClick = {}) {
                                        Icon(
                                            painter = painterResource(R.drawable.trash),
                                            contentDescription = "Delete session",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(IconSize.NORMAL)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(20.dp))
                        AppText("Staff members", variant = TextType.Label)
                    }
                    if(staffs.isEmpty()){
                        item {
                            AppText(
                                "No staff members added yet.",
                                variant = TextType.Label,
                                modifier = Modifier.wrapContentSize()
                            )
                        }
                    }
                    else{
                        items(staffs.size) { index ->
                            val staff = staffs[index]

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.shapes.medium
                                    )
                                    .padding(Spacing.MD)
                            ) {
                                AppText(staff.fullNames, variant = TextType.Label)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppText(
                                        "${staff.username} | ${staff.role}",
                                        variant = TextType.Body,
                                        color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                                    )
                                    Row(
                                        modifier = Modifier.wrapContentSize()
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
                    }
                }
            }
            AppButton(
                "Add new staff",
                { navController.navigate("store/settings/staff_&_security/create_staff")},
                type = ButtonType.Primary
            )
        }
    }

}