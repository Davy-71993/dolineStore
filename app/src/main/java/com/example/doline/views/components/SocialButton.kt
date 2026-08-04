package com.example.doline.views.components



import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SocialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Int
){
    AppButton(
        text,
        onClick,
        modifier = modifier,
        type = ButtonType.Default,
        elevation = 6.dp,
        pressedElevation = 4.dp,
        icon = icon,
    )
}