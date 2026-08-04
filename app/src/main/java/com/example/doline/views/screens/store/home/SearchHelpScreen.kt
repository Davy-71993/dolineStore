package com.example.doline.views.screens.store.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.models.faqs
import com.example.doline.ui.theme.FontSize
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import com.example.doline.views.components.TextType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchHelpScreen(navController: NavController){
    val colorScheme = MaterialTheme.colorScheme
    val rect = MaterialTheme.shapes.medium

    var searchText by remember {
        mutableStateOf("")
    }

    var filteredList by remember {
        mutableStateOf(
            faqs
        )
    }

    var activeQuestion by remember {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.MD)
                    ){
                        TextField(
                            value = searchText,
                            onValueChange = {
                                searchText = it
                                filteredList = faqs.filter { q->
                                    q.qn.lowercase().contains(searchText.lowercase()) || q.category.lowercase().contains(searchText.lowercase())
                                }
                            },
                            textStyle = TextStyle(
                                fontSize = FontSize.SM
                            ),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = {
                                AppText(
                                    "Search FAQs or help articles",
                                    color = colorScheme.onBackground.copy(.6f),
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colorScheme.surface.copy(.4f), rect)
                        )
                    }
                },
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
            Modifier
                .fillMaxSize()
                .padding(Spacing.MD, ip.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
        ) {
            item {
                Spacer(Modifier.height(0.dp))
            }
            if(filteredList.isEmpty() && searchText.isNotEmpty()){
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 50.dp),
                        verticalArrangement = Arrangement.spacedBy(Spacing.SM),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.error),
                            contentDescription = "Back",
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(.3f)
                        )
                        AppText(
                            "No FAQs or articles matched your search.",
                            variant = TextType.Label,
                            color = colorScheme.onBackground.copy(.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            filteredList.forEach {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(rect)
                                .background(
                                    colorScheme.surface.copy(.5f),
                                    rect
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        activeQuestion = if (activeQuestion == it.qn) {
                                            null
                                        } else {
                                            it.qn
                                        }
                                    }
                                    .padding(Spacing.MD),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppText(
                                    it.qn,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    painter = painterResource(if(activeQuestion == it.qn) R.drawable.chev_up else R.drawable.chev_down),
                                    contentDescription = null,
                                    modifier = Modifier.size(IconSize.NORMAL)
                                )
                            }
                            if(activeQuestion == it.qn){
                                HorizontalDivider()
                                AppText(
                                    it.ans,
                                    modifier = Modifier
                                        .padding(Spacing.MD)
                                )
                            }
                        }
                    }
                }

        }
    }
}