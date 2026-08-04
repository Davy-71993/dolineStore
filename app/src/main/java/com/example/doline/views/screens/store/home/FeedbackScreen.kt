package com.example.doline.views.screens.store.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.models.feedbackCategories
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.TextType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(navController: NavController){
    val colorScheme = MaterialTheme.colorScheme
    val rect = MaterialTheme.shapes.medium
    val circle = RoundedCornerShape(Rounding.FULL)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    var feedbackCategory by remember {
        mutableStateOf<String?>(null)
    }
    var feedbackBody by remember {
        mutableStateOf("")
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Send Feedback", variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier.padding(vertical = 0.dp).shadow(Spacing.MD),
                scrollBehavior = scrollBehavior,
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(Spacing.MD, ip.calculateTopPadding())
                .padding(vertical = Spacing.XL),
            verticalArrangement = Arrangement.spacedBy(Spacing.XL)
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surface, rect)
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.XL)
                ) {
                    item {
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.XS)
                        ) {
                            AppText("What's on your mind?")
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                                verticalArrangement = Arrangement.spacedBy(Spacing.XS)
                            ) {
                                feedbackCategories.forEach { (id, label) ->
                                    AppText(
                                        label,
                                        variant = TextType.Small,
                                        color = if(feedbackCategory == id) colorScheme.onPrimary else colorScheme.onBackground,
                                        modifier = Modifier
                                            .wrapContentSize()
                                            .background(
                                                if (feedbackCategory == id) colorScheme.primary else colorScheme.surface,
                                                circle
                                            )
                                            .border(1.dp, colorScheme.primary, circle)
                                            .clip(circle)
                                            .clickable { feedbackCategory = id }
                                            .padding(10.dp, 5.dp)

                                    )
                                }
                            }
                        }

                    }
                    item {
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.XS)
                        ) {
                            AppText("Details", variant = TextType.Label)
                            TextField(
                                value = feedbackBody,
                                onValueChange = { feedbackBody = it },
                                minLines = 3,
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                placeholder = {
                                    AppText(
                                        "Describe your feedback",
                                        color = MaterialTheme.colorScheme.onBackground.copy(.6f),
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(150.dp, 250.dp)
                                    .background(colorScheme.background.copy(.3f))
                                    .border(1.dp, colorScheme.onBackground, rect)
                            )
                        }
                    }
                    item {
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.XS)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppText("Attachments", variant = TextType.Label)
                                AppText("Optional", variant = TextType.Small, color = colorScheme.onBackground.copy(.7f))
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
                                verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                            ) {
                                Box(
                                    Modifier
                                        .height(100.dp)
                                        .weight(.5f)
                                        .background(colorScheme.background.copy(.4f), rect)

                                ){
                                    Column(
                                        Modifier.wrapContentSize().align(Alignment.Center),
                                        verticalArrangement = Arrangement.spacedBy(Spacing.XXS),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.camera),
                                            contentDescription = null,
                                            Modifier.size(IconSize.BIG)
                                        )
                                        AppText("Add photo", variant = TextType.Small, color = colorScheme.onBackground.copy(.7f))
                                    }
                                }
                                Box(
                                    Modifier
                                        .height(100.dp)
                                        .weight(.5f)
                                        .background(colorScheme.background.copy(.4f), rect)
                                ){
                                    IconButton(
                                        onClick = {},
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.x),
                                            contentDescription = null,
                                            Modifier.size(IconSize.NORMAL)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colorScheme.background.copy(.6f), rect)
                                .padding(Spacing.MD),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText("@", variant = TextType.Heading)
                            Column{
                                AppText("Allow follow-up via email")
                                AppText("manager@store.com")
                            }
                            Switch(true, {})
                        }
                    }
                }
            }
            AppButton(
                "Submit Feedback",
                {},
                type = ButtonType.Primary
            )
        }
    }
}