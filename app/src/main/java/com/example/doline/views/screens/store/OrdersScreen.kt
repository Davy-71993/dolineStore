package com.example.doline.views.screens.store

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.doline.views.components.AppText

@Composable
fun OrdersScreen(navController: NavController){
    Scaffold() { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                Column {
                    AppText(text = "Store Orders")
                }
            }
        }
    }
}