package com.example.doline.views.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.doline.data.SelectOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T>SelectField(
    value: String,
    options: List<SelectOption<T>>,
    onSelect: (selected: SelectOption<T>) -> Unit,
    placeholder: String = "",
    label: String = "",
    isError: Boolean = false,
    errorMessage: String? = null
){
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextInputField(
            value = value,
            onValueChange = {},
            placeHolder = placeholder,
            label = label,
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded
                )
            },
            modifier = Modifier
                .menuAnchor(
                    ExposedDropdownMenuAnchorType.PrimaryEditable,
                    true
                ),
            isError = isError,
            errorMessage = errorMessage
        )
        ExposedDropdownMenu(
            expanded = expanded,
            containerColor = colorScheme.surface,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { entry ->
                DropdownMenuItem(
                    text = { AppText(entry.label) },
                    onClick = {
                        onSelect(entry)
                        expanded = false
                    }
                )
            }
        }
    }
}