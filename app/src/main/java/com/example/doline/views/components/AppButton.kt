package com.example.doline.views.components



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.LetterSpacing
import com.example.doline.ui.theme.Rounding

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.Default,   // Default: gradient like login
    enabled: Boolean = true,
    elevation: Dp = 0.dp,
    pressedElevation: Dp = 0.dp,
    isLoading: Boolean = false,
    buttonHeight: Dp = 46.dp,
    icon: Int? = null,           // ← Nullable Icon (left side)
    iconTint: Color = Color.Unspecified,
    iconSide: IconSide? = IconSide.Left,
    iconSize: Dp = IconSize.NORMAL,
    textVariant: TextType = TextType.Label,
    weight: Float = 1f
) {
    val letterSpacing = LetterSpacing.XS

    when (type) {
        ButtonType.Default -> {
            // Gradient Button (used in Login screen)
            Button(
                onClick = onClick,
                modifier = modifier.fillMaxWidth().height(buttonHeight),
                shape = RoundedCornerShape(Rounding.SM),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                enabled = enabled && !isLoading,
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = elevation,
                    pressedElevation = pressedElevation,
                    disabledElevation = 0.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            if(iconSide == IconSide.Left){
                                if (icon != null) {
                                    Icon(
                                        painter = painterResource(icon),
                                        contentDescription = "Google Logo",
                                        tint = iconTint,
                                        modifier = Modifier.size(iconSize)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }


                            AppText(
                                text,
                                variant = textVariant,
                                letterSpacing = letterSpacing
                            )

                            if(iconSide == IconSide.Right){
                                if (icon != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        painter = painterResource(icon),
                                        contentDescription = "Google Logo",
                                        tint = iconTint,
                                        modifier = Modifier.size(iconSize)
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }

        ButtonType.Primary -> {
            // Solid Primary Button
            Button(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth().height(buttonHeight),
                shape = RoundedCornerShape(Rounding.SM),
                enabled = enabled && !isLoading,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = elevation,
                    pressedElevation = elevation - 2.dp,
                    disabledElevation = 0.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if(iconSide == IconSide.Left){
                            if (icon != null) {
                                Icon(
                                    painter = painterResource(icon),
                                    contentDescription = "Button icon",
                                    tint = iconTint,
                                    modifier = Modifier.size(iconSize)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                        AppText(
                            text,
                            color = MaterialTheme.colorScheme.onPrimary,
                            variant = textVariant,
                            letterSpacing = letterSpacing
                        )
                        if(iconSide == IconSide.Right){
                            if (icon != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    painter = painterResource(icon),
                                    contentDescription = "Button icon",
                                    tint = iconTint,
                                    modifier = Modifier.size(iconSize)
                                )

                            }
                        }
                    }
                }
            }
        }

        ButtonType.Outlined -> {
            // Outlined Button (like Google/Apple)
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth().height(buttonHeight),
                shape = RoundedCornerShape(Rounding.SM),
                enabled = enabled && !isLoading,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = elevation,
                    pressedElevation = elevation - 2.dp,
                    disabledElevation = 0.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if(iconSide == IconSide.Left){
                            if (icon != null) {
                                Icon(
                                    painter = painterResource(icon),
                                    contentDescription = "Button icon",
                                    tint = iconTint,
                                    modifier = Modifier.size(iconSize)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                        AppText(
                            text,
                            variant = textVariant,
                            letterSpacing = letterSpacing
                        )
                        if(iconSide == IconSide.Right){
                            if (icon != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    painter = painterResource(icon),
                                    contentDescription = "Button icon",
                                    tint = iconTint,
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}

// Button Types
enum class ButtonType {
    Default,   // Orange gradient (Login button)
    Primary,           // Solid primary color from theme
    Outlined           // Outlined button
}

enum class IconSide {
    Left,            // Puts the button icon on the left of the text
    Right           // Puts the button icon on the right of the text
}