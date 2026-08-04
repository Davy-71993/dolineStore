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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxationSettingsScreen(navController: NavController){
    val colorScheme = MaterialTheme.colorScheme
    val rect  = MaterialTheme.shapes.medium

    var isEFRISEnabled by remember {
        mutableStateOf(true)
    }
    var selected by remember {
        mutableStateOf("inclusive")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Taxation & Compliance", variant = TextType.Heading, maxLines = 1) },
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
                actions = {}
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
                        "EFRIS Integration (Uganda)",
                        variant = TextType.Label
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.XXS),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText("Enable EFRIS integration", variant = TextType.Label)
                        Switch(checked = isEFRISEnabled, onCheckedChange = { isEFRISEnabled = it })
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if(isEFRISEnabled) 1f else .5f)
                    ) {
                        TextInputField(
                            value = "",
                            onValueChange = {},
                            label = "EFRIS API Key",
                            enabled = isEFRISEnabled,
                            placeHolder = "Enter your EFRIS API key",
                        )
                        AppText(
                            "Get your API key from the EFRIS portal at https://ura.go.ug/en/efris/",
                            variant = TextType.Small,
                            color = colorScheme.onBackground.copy(.6f)
                        )

                        Spacer(Modifier.height(10.dp))

                        AppButton(
                            "Save EFRIS settings",
                            onClick = {},
                            type = ButtonType.Primary,
                            enabled = isEFRISEnabled
                        )
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
                        "VAT/Sales tax configuration",
                        variant = TextType.Label
                    )
                    TextInputField(
                        value = "",
                        onValueChange = {},
                        label = "VAT (%)",
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(1f)
                    ) {
                        val options = listOf("inclusive" to "Inclusive", "exclusive" to "Exclusive")
                        options.forEach { (id, label) ->
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(rect)
                                    .clickable { selected = id }
                                    .padding(Spacing.XS),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selected == id, onClick = null)
                                AppText(label, variant = TextType.Label)
                            }
                        }
                    }

                    AppButton(
                        "Save VAT settings",
                        onClick = {},
                        type = ButtonType.Primary,
                    )
                }
            }
            item {
                Spacer(Modifier.height(0.dp))
            }
        }
    }

}