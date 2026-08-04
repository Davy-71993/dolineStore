package com.example.doline.views.screens.welcome



import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.window.core.layout.WindowSizeClass
import com.example.doline.DeviceConfiguration
import com.example.doline.ui.theme.FontSize
import com.example.doline.ui.theme.LetterSpacing
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.ButtonType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeScreen(navController: NavHostController) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfiguration = DeviceConfiguration.getWindowSizeClass(windowSizeClass)
    val height = when(deviceConfiguration){
        DeviceConfiguration.MOBILE_LANDSCAPE -> 0.dp
        else -> 100.dp
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .padding(Spacing.XXL),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(height))

                // EST. 2026
                Text(
                    text = "EST. 2026",
                    fontSize = FontSize.XXS,
                    letterSpacing = LetterSpacing.MD,
//                color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(Spacing.XL))

                // Main Title: DOLINE
                Text(
                    text = "DOLINE",
                    fontSize = FontSize.BRAND,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.W900,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = LetterSpacing.XXS
                )

                Spacer(modifier = Modifier.height(Spacing.XS))

                // Curated Market with lines
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.SM)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "CURATED MARKET",
                        fontSize = FontSize.SM,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )

                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.MD))

                // Tagline
                Text(
                    text = "The craft of local discovery.",
                    fontSize = FontSize.SM,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.XL))

                // Get Started Button
                AppButton(
                    "Get Started",
                    onClick = { navController.navigate("register") },
                    type = ButtonType.Primary
                )

                Spacer(modifier = Modifier.height(Spacing.XXL))

                // Login | Explore
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Login",
                        fontSize = FontSize.LG,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { navController.navigate("login") }
                    )

                    Text(
                        text = "|",
                        fontSize = FontSize.XL,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Explore",
                        fontSize = FontSize.LG,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.weight(1.5f))
            }
        }
    }
}

