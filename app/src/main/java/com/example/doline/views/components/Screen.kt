package com.example.doline.views.components



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.example.doline.DeviceConfiguration
import com.example.doline.ui.theme.Spacing


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen (
    topAppBar: @Composable ()-> Unit,
    floatingActionButton: @Composable (() -> Unit) = {},
    contents: @Composable () -> Unit
){
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfig = DeviceConfiguration.getWindowSizeClass(windowSizeClass)
    val paddingValues = when(deviceConfig){
        DeviceConfiguration.MOBILE_LANDSCAPE -> Spacing.MD
        else -> 0.dp
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = topAppBar,
        floatingActionButton = floatingActionButton,
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(colorScheme.background)
            .padding(paddingValues)
        ) {
            contents()
        }
    }
}