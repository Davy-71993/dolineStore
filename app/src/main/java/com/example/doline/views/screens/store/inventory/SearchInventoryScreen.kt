package com.example.doline.views.screens.store.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchInventoryScreen(navController: NavController, viewModel: InventoryViewModel){
    val colorScheme = MaterialTheme.colorScheme
    val rect = MaterialTheme.shapes.medium

    val uiState by viewModel.uiState.collectAsState()
    val items by viewModel.items.collectAsState()
    val searchText by viewModel.searchText.collectAsState("")
    val filteredItems by viewModel.filteredItems.collectAsState(items)


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                        TextField(
                            value = searchText,
                            maxLines = 1,
                            onValueChange = { viewModel.handleSearch(it)},
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = {
                                AppText(
                                    "Search inventory",
                                    color = colorScheme.onBackground.copy(.6f),
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colorScheme.surface.copy(.4f), rect)
                        )
                        Spacer(Modifier.width(30.dp))
                    }
                },
                modifier = Modifier.shadow(10.dp),
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
    ){ innerPadding ->
        when (val state = uiState) {
            is InventoryScreenUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is InventoryScreenUiState.Success -> {
                if (filteredItems.isEmpty()) {
                    EmptyInventoryMessage()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        items(filteredItems.size) {
                            val entry = filteredItems[it]
                            InventoryItemCard(
                                record = entry,
                                onClick = { navController.navigate("stock/${entry.item.id}") }
                            )
                        }
                    }
                }
            }

            is InventoryScreenUiState.Error -> {
                ErrorMessage(message = state.message)
            }
        }
    }
}

@Composable
private fun EmptyInventoryMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppText("No items matched your search!")
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppText(text = "Error: $message", color = MaterialTheme.colorScheme.error)
    }
}