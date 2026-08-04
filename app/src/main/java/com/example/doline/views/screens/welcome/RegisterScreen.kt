package com.example.doline.views.screens.welcome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.doline.R
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.IconSide
import com.example.doline.views.components.SocialButton
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.FormScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var fullNames by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }


    FormScreen(
        appBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    AppText(
                        "Register",
                        color = MaterialTheme.colorScheme.onBackground,
                        variant = TextType.Heading
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("welcome") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        AppText("Welcome, Create your account")

        // Social Login Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.SM)
        ) {
            SocialButton(
                text = "GOOGLE",
                onClick = {},
                modifier = Modifier.weight(1f),
                icon = R.drawable.google_favicon_2025
            )
            SocialButton(
                text = "APPLE",
                onClick = {},
                modifier = Modifier.weight(1f),
                icon = R.drawable.apple_logo_black
            )
        }

        // Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(.6f)
            )
            AppText(
                text = " OR  CONTINUE  WITH ",
                modifier = Modifier.padding(horizontal = Spacing.SM),
                variant = TextType.Small,
                color = MaterialTheme.colorScheme.onBackground.copy(.8f)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(.6f)
            )
        }

        // Email field
        TextInputField(
            value = email,
            onValueChange = { email = it },
            label = "Email address",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.mail),
                    contentDescription = "Email",
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            },
            placeHolder = "Your email address",
        )

        // Username field
        TextInputField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.user),
                    contentDescription = "Username",
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            },
            placeHolder = "Your username",
        )

        // Username field
        TextInputField(
            value = fullNames,
            onValueChange = { fullNames = it },
            label = "Full names",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.user_pen),
                    contentDescription = "Full names",
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            },
            placeHolder = "Your full names",
        )

        // Password field
        TextInputField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.lock),
                    contentDescription = "Password",
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        painter = painterResource(if (isPasswordVisible) R.drawable.eye_off else R.drawable.eye),
                        contentDescription = "Toggle password visibility",
                        modifier = Modifier.size(IconSize.NORMAL)
                    )
                }
            },
            isPassword = !isPasswordVisible,
            placeHolder = "Your password",
        )

        // Confirm password field
        TextInputField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm password",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.lock),
                    contentDescription = "Confirm password",
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            },
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(if (isConfirmPasswordVisible) R.drawable.eye_off else R.drawable.eye),
                        contentDescription = "Toggle confirm password visibility",
                        modifier = Modifier.size(IconSize.NORMAL)
                    )
                }
            },
            isPassword = !isConfirmPasswordVisible,
            placeHolder = "Confirm your password",
        )

        // Register Button with Gradient
        AppButton(
            "REGISTER",
            onClick = { },
            iconSize = IconSize.NORMAL,
            iconSide = IconSide.Right,
            icon = R.drawable.arrow_right,
            iconTint = MaterialTheme.colorScheme.onPrimary,
            textVariant = TextType.Label,
            type = ButtonType.Primary,
            modifier = Modifier
                .fillMaxWidth()
        )

        // Login Link
        Row(
            modifier = Modifier.padding(top = Spacing.MD),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.XXS)
        ) {
            AppText(
                text = "Already have an account? ",
            )
            AppText(
                text = "Login",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { navController.navigate("login") }
            )
        }

        // Footer
        Row(
            modifier = Modifier.padding(vertical = Spacing.LG),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.XXS)
        ) {
            AppText(
                text = "© 2026 DOLINE POS. ALL RIGHTS RESERVED.",
                variant = TextType.Small
            )
        }
    }
}
