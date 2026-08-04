package com.example.doline.views.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.example.doline.formatWithCommas
import com.example.doline.ui.theme.FontSize
import com.example.doline.views.screens.store.home.Message

@Composable
fun NumberInputField(
    onChange: (input: Number?) -> Unit,
    number: Number? = null,
    label: String = "",
    placeholder: String = "",
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    action: (@Composable ()-> Unit)? = null,
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = FontSize.MD,
    isError: Boolean = false,
    errorMessage: String? = null
){
    // Local string state to handle raw user input smoothly (e.g., handling trailing decimals)
    var textState by remember { mutableStateOf(number?.toString() ?: "") }
    val cleanDecimalRegex = remember { Regex("^[-?]?\\d*\\.?\\d*$") }

    // Sync local state if the external state changes programmatically
    LaunchedEffect(number) {
        if (number == null) {
            if (textState.isNotEmpty()) textState = ""
        } else {
            // Only update local state if the external value actually diverges from what's typed
            val currentCleanAmount = textState.replace(",", "")
            if (number != currentCleanAmount.toDoubleOrNull()) {
                textState = formatValueWithCommas(number.toDouble())
            }
        }
    }

    TextInputField(
        textState,
        { incomingText ->
            // 1. Strip commas to validate the actual underlying number structure
            val cleanText = incomingText.replace(",", "")

            // 2. Run the same strict decimal check (prevents double dots, letters, etc.)
            if (cleanText.matches(cleanDecimalRegex)) {

                // 3. Format the clean text with commas *while preserving* trailing dots/zeros for typing fluidly
                textState = formatStringToCommas(cleanText)

                // 4. Send the clean Double? back to the parent state
                val parsed = cleanText.toDoubleOrNull()
                onChange(parsed)
            }
        },
        label = label,
        textAlign = textAlign,
        placeHolder = placeholder,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        fontSize = fontSize,
        action = action,
        isError = isError,
        errorMessage = errorMessage
    )
}

private  fun formatValueWithCommas(value: Double): String {
    // Calls your existing `Number.formatWithCommas()` helper
    return value.formatWithCommas()
}

private  fun formatStringToCommas(cleanText: String): String {
    if (cleanText.isEmpty() || cleanText == "-" || cleanText == "." || cleanText == "-.") {
        return cleanText
    }

    val parts = cleanText.split(".")
    val integerPart = parts[0]

    // Parse the integer part to a Double (or Long) just to format it with your helper
    val formattedInteger = integerPart.toLongOrNull()?.let {
        it.formatWithCommas()
    } ?: integerPart

    // Reconstruct the string: Integer part + dot + whatever decimal parts they typed
    return if (parts.size > 1) {
        "$formattedInteger.${parts[1]}"
    } else {
        formattedInteger
    }
}