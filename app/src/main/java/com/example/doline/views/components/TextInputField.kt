package com.example.doline.views.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.doline.ui.theme.FontSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing

@Composable
fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    singleLine: Boolean = true,
    enabled: Boolean = true,
    isError: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    placeHolder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    action: @Composable (() -> Unit)? = null,
    maxLines: Int = 5,
    minLines: Int = 1,
    fraction: Float = 1f,
    readOnly: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp, 8.dp),
    errorMessage: String? = "",
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = FontSize.MD
){
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier.fillMaxWidth(fraction)
    ){
        if(label.isNotEmpty()){
            Row(
                modifier = Modifier.fillMaxWidth(fraction),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                AppText(label, variant = TextType.LabelSmall)
                Spacer(modifier = Modifier.weight(1f))
                if(action != null){ action() }
            }
        }
        BasicTextField(
            value = value,
            readOnly = readOnly,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .padding(top = Spacing.XXS),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize, textAlign = textAlign),
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            visualTransformation = if (isPassword)
                PasswordVisualTransformation() else VisualTransformation.None,

            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    singleLine = singleLine,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    contentPadding = contentPadding,
                    colors = OutlinedTextFieldDefaults.colors(),
                    placeholder = {
                        Text(
                            placeHolder,
                            color = colorScheme.onBackground.copy(.6f),
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize)
                        )
                    },
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = colorScheme.surface.copy(.5f),
                                focusedContainerColor = colorScheme.surface.copy(.5f),
                                focusedBorderColor = colorScheme.onBackground
                            ),
                            shape = RoundedCornerShape(Rounding.SM),

                        )
                    }
                )
            }
        )

        if (!errorMessage.isNullOrEmpty()){
            AppText(errorMessage, variant = TextType.Small, color = colorScheme.error)
        }
    }
}