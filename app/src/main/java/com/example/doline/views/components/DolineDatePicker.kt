package com.example.doline.views.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.doline.R
import com.example.doline.ui.theme.IconSize
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DolineDatePicker(onDatePicked: (date: LocalDate?) -> Unit){
    val dateState = rememberDatePickerState(
        initialSelectedDate = LocalDate.now(),
        initialDisplayMode = DisplayMode.Picker,
    )
    var isDatePickerOpen by rememberSaveable {
        mutableStateOf(false)
    }

    IconButton(onClick = { isDatePickerOpen = true }) {
        Icon(
            painter = painterResource(R.drawable.calendar_today),
            contentDescription = "Select data",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(IconSize.NORMAL)
        )
    }

    if(isDatePickerOpen){
        DatePickerDialog(
            onDismissRequest = {
                onDatePicked(dateState.getSelectedDate())
                isDatePickerOpen = false
            },
            confirmButton = {
                TextButton(onClick = {
                    onDatePicked(dateState.getSelectedDate())
                    isDatePickerOpen = false
                }) {
                    AppText("Ok", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDatePicked(null)
                    isDatePickerOpen = false
                }) {
                    AppText("Cancel")
                }
            },
            content = {
                DatePicker(
                    state = dateState
                )
            },
        )
    }
}