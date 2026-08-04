package com.example.doline.views.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.doline.ui.theme.FontSize

@Composable
fun GhostInputField(
    value: String,
    onValueChange: (text: String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    focusedTextColor: Color = MaterialTheme.colorScheme.primary,
    unfocusedTextColor: Color = MaterialTheme.colorScheme.primary,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minLines: Int = 1,
){
    TextField(
        value = value,
        modifier = modifier,
        singleLine= singleLine,
        maxLines = maxLines,
        minLines = minLines,
        onValueChange = onValueChange,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            focusedTextColor = focusedTextColor,
            unfocusedTextColor = unfocusedTextColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = FontSize.MD
        ),
        placeholder = {
            AppText(
                placeholder,
                color = MaterialTheme.colorScheme.onBackground.copy(.4f)
            )
        },
        keyboardOptions = keyboardOptions
    )
}