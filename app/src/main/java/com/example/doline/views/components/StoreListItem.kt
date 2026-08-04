package com.example.doline.views.components


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.doline.data.Store
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing

@Composable
fun StoreCard(
    store: Store,
    modifier: Modifier,
    navController: NavHostController
) {
    val title = store.name
    val  description = store.description
    val location = store.address
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.MD),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing.XXS),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = {
             navController.navigate(route="stores/${store.id}")
        }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.onSurface)
            ) {
                ImageView(imageUrl = store.image)
            }

            // Content Section
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .offset(y=Spacing.LG)
                    .background(brush = Brush.verticalGradient(colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(0.0f),
                        MaterialTheme.colorScheme.surface.copy(1.0f),
                    )))
            ) {
                AppText(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    variant = TextType.Label
                )

                Spacer(modifier = Modifier.height(Spacing.XS))

                AppText(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(Spacing.SM))

                // Location Row
                Row{
                    AppText(
                        text = "📍",
                    )
                    Spacer(modifier = Modifier.width(Spacing.XXS))
                    AppText(
                        text = location ?: "Location",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.XXL))
            }
        }
    }
}