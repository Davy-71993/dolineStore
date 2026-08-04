package com.example.doline.views.screens.store.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
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
fun CreateStaffScreen(navController: NavController){
    var fullNames by remember {
        mutableStateOf("")
    }
    var username by remember {
        mutableStateOf("")
    }

    var role by remember {
        mutableStateOf("")
    }

    var passKey by remember {
        mutableStateOf("")
    }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Add new Staff", variant = TextType.Heading, maxLines = 1) },
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
                .fillMaxSize()
                .padding(vertical = p.calculateTopPadding(), horizontal = Spacing.SM),
            verticalArrangement = Arrangement.spacedBy(Spacing.XL)
        ) {

            Column(
                modifier = Modifier
                    .background(
                        colorScheme.surface,
                        MaterialTheme.shapes.medium
                    )
                    .padding(Spacing.XL),
                verticalArrangement = Arrangement.spacedBy(Spacing.XL)
            ) {
                TextInputField(
                    value = fullNames,
                    onValueChange = { fullNames = it },
                    label = "Full names",
                    placeHolder = "Staff's full names",
                )
                TextInputField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username",
                    placeHolder = "Staff's full username",
                )
                TextInputField(
                    value = role,
                    onValueChange = { role = it },
                    label = "Role",
                    placeHolder = "Staff's role",
                )
                TextInputField(
                    value = passKey,
                    onValueChange = { passKey = it },
                    label = "Pass key",
                    isPassword = true,
                    placeHolder = "Staff's pass key",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                )
                Spacer(Modifier.height(10.dp))
                AppButton(
                    "Save",
                    {},
                    Modifier,
                    ButtonType.Primary
                )
            }
        }
    }
}