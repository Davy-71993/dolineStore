package com.example.doline.views.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IconButtonWithBadge(
    icon: Painter,
    contentDescription: String?,
    badgeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeColor: Color = colorScheme.onBackground,
    contentColor: Color = Color.Unspecified,
    iconSize: Dp = 24.dp,
    enabled: Boolean = true
) {
    Box(modifier = modifier) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(48.dp) // standard touch target
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
                tint = contentColor
            )
        }

        // Badge
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-36).dp, y = (-4).dp)
                    .background(
                        color = badgeColor,
                        shape = CircleShape
                    )
                    .padding(4.dp, 2.dp)
                    .sizeIn(minWidth = 18.dp, minHeight = 18.dp)
            ) {
                AppText(
                    text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                    color = colorScheme.primary,
                    variant = TextType.LabelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}