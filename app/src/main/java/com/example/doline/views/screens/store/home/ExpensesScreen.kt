package com.example.doline.views.screens.store.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.Expense
import com.example.doline.data.ExpenseCategory
import com.example.doline.data.ExpenseRepository
import com.example.doline.formatWithCommas
import com.example.doline.timestampToDate
import com.example.doline.timestampToTime
import com.example.doline.toDateString
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.ui.theme.amberContainer
import com.example.doline.ui.theme.blueContainer
import com.example.doline.ui.theme.onAmberContainer
import com.example.doline.ui.theme.onBlueContainer
import com.example.doline.ui.theme.onPinkContainer
import com.example.doline.ui.theme.onPurpleContainer
import com.example.doline.ui.theme.onSlateContainer
import com.example.doline.ui.theme.onSuccessContainerLight
import com.example.doline.ui.theme.pinkContainer
import com.example.doline.ui.theme.purpleContainer
import com.example.doline.ui.theme.slateContainer
import com.example.doline.ui.theme.successContainerLight
import com.example.doline.views.components.AppText
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(navController: NavController, viewModel: ExpenseViewModel) {

    val dateState = rememberDatePickerState(
        initialSelectedDate = LocalDate.now(),
        initialDisplayMode = DisplayMode.Picker,
    )

    var isDatePickerOpen by rememberSaveable {
        mutableStateOf(false)
    }

    val dayStart = (dateState.getSelectedDate() ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val dayEnd = (dateState.getSelectedDate() ?: LocalDate.now()).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(dayStart, dayEnd) {
        viewModel.fetchDailyExpenses(dayStart, dayEnd)
    }

    val storeId = viewModel.storeId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Expenses", variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier.padding(vertical = 0.dp),
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    subtitleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_left),
                            contentDescription = "Back",
                            modifier = Modifier.size(IconSize.NORMAL),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isDatePickerOpen = true }) {
                        Icon(
                            painter = painterResource(R.drawable.calendar_today),
                            contentDescription = "Select data",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(IconSize.NORMAL)
                        )
                    }
                    IconButton(onClick = { navController.navigate("store/expenses/create")}) {
                        Icon(
                            painter = painterResource(R.drawable.plus),
                            contentDescription = "New Expense",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(IconSize.BIG)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("store/$storeId/expenses/create")},
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    8.dp,
                    6.dp
                ),
                shape = RoundedCornerShape(Rounding.FULL),
                modifier = Modifier
                    .padding(Spacing.SM)
                    .absoluteOffset(
                        y = (-80).dp
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = "Add new expense",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(IconSize.BIG)
                )
            }
        }

    ) { innerPadding ->
        when(val state = uiState){
            is ExpensesScreenUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ExpensesScreenUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.padding(Spacing.MD,innerPadding.calculateTopPadding())
                ) {
                    item {
                        if(isDatePickerOpen){
                            DatePickerDialog(
                                onDismissRequest = { isDatePickerOpen = false },
                                confirmButton = {
                                    TextButton(onClick = {isDatePickerOpen = false }) {
                                        AppText("Ok", color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {isDatePickerOpen = false }) {
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
                        Row{
                            Spacer(modifier = Modifier.weight(1f))
                            AppText(
                                dateState.getSelectedDate()!!.toDateString(),
                                variant = TextType.Heading
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.MD, horizontal = 0.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(Rounding.MD),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.MD),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                                ) {
                                    AppText(
                                        "Total Expenses Today",
                                        variant = TextType.Label,
                                    )
                                    AppText(
                                    "UGX ${state.expenses.sumOf{ it.amount }.formatWithCommas()}",
                                        variant = TextType.Heading,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .weight(1f),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.background
                                            ),
                                            shape = RoundedCornerShape(Rounding.MD),
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(Spacing.MD),
                                                verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                                            ) {
                                                AppText(
                                                    "Daily Budget",
                                                    variant = TextType.Small,
                                                    maxLines = 1
                                                )

                                                AppText(
                                                    "UGX 100,000",
                                                    variant = TextType.Label,
                                                )
                                            }
                                        }
                                        Card(
                                            modifier = Modifier
                                                .weight(1f),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.background
                                            ),
                                            shape = RoundedCornerShape(Rounding.MD),
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(Spacing.MD),
                                                verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                                            ) {
                                                AppText(
                                                    "Remaining",
                                                    variant = TextType.Small,
                                                    maxLines = 1
                                                )
                                                AppText(
                                                    "UGX 55,000",
                                                    variant = TextType.Label,
                                                )
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                        ) {
                            if(state.expenses.isEmpty()){
                                EmptyExpensesMessage()
                            }else{
                                state.expenses.forEach { expense ->
                                    ExpenseCard(expense)
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
            is ExpensesScreenUiState.Error -> {
                ErrorMessage(state.message)
            }

        }

    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseCard(expense: Expense){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(Rounding.MD),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.MD),
            horizontalArrangement = Arrangement.spacedBy(Spacing.MD)
        ) {
            ExpenseIcon(expense.category)
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
            ) {
                AppText(expense.category.name, variant = TextType.Label)
                AppText(
                    expense.description,
                    color = MaterialTheme.colorScheme.onBackground.copy(.7f),
                    maxLines = 2
                )
                AppText(
                    "UGX ${expense.amount.formatWithCommas()}",
                    variant = TextType.Heading,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppText(
                        timestampToDate(expense.createdAt),
                        variant = TextType.Small,
                        color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                    )
                    AppText(
                        "|",
                        variant = TextType.Heading,
                        color = MaterialTheme.colorScheme.onBackground.copy(.4f)
                    )
                    AppText(
                        timestampToTime(expense.createdAt),
                        variant = TextType.Small,
                        color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                    )
                }
            }
        }

    }
}

@Composable
fun ExpenseIcon(
    category: ExpenseCategory,
    colored: Boolean = true,
    rounded: Boolean = false,
    padding: Dp = Spacing.XXS,
    background: Color = MaterialTheme.colorScheme.background,
    color: Color = MaterialTheme.colorScheme.onBackground
){
    val backgroundColor = when(category){
        ExpenseCategory.STAFF -> successContainerLight
        ExpenseCategory.OTHERS -> slateContainer
        ExpenseCategory.INVENTORY -> blueContainer
        ExpenseCategory.LOGISTICS -> purpleContainer
        ExpenseCategory.UTILITIES -> amberContainer
        ExpenseCategory.MARKETING -> pinkContainer
    }

    val iconColor = when(category){
        ExpenseCategory.STAFF -> onSuccessContainerLight
        ExpenseCategory.OTHERS -> onSlateContainer
        ExpenseCategory.INVENTORY -> onBlueContainer
        ExpenseCategory.LOGISTICS -> onPurpleContainer
        ExpenseCategory.UTILITIES -> onAmberContainer
        ExpenseCategory.MARKETING -> onPinkContainer
    }

    val icon = when(category){
        ExpenseCategory.STAFF -> R.drawable.staff
        ExpenseCategory.OTHERS -> R.drawable.others
        ExpenseCategory.INVENTORY -> R.drawable.inventory
        ExpenseCategory.LOGISTICS -> R.drawable.logistics
        ExpenseCategory.UTILITIES -> R.drawable.utilities
        ExpenseCategory.MARKETING -> R.drawable.marketing
    }

    Box(
        modifier = Modifier
            .background(
                color = if(colored) backgroundColor else background,
                shape = if(rounded) RoundedCornerShape(Rounding.FULL) else RoundedCornerShape(6.dp)
            )
            .padding(padding)
    ){
        Icon(
            painter = painterResource(icon),
            contentDescription = "${category.name} icon",
            tint = if(colored) iconColor else color,
            modifier = Modifier.size(IconSize.BIG)
        )
    }
}

@Composable
private fun EmptyExpensesMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppText("No expenses recorded yet")
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppText(text = "Error: $message", color = MaterialTheme.colorScheme.error)
    }
}


@HiltViewModel
class ExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private  val expenseRepository: ExpenseRepository,
) : ViewModel() {
    val storeId = savedStateHandle.get<Long>("storeId")
    private val _uiState = MutableStateFlow<ExpensesScreenUiState>(ExpensesScreenUiState.Loading)
    val uiState: StateFlow<ExpensesScreenUiState> = _uiState.asStateFlow()
    fun fetchDailyExpenses(start: Long, end: Long) = viewModelScope.launch {
        _uiState.value = ExpensesScreenUiState.Loading
        if (storeId == null){
            _uiState.value = ExpensesScreenUiState.Error("Error: The store Id is null.")

            return@launch
        }
        try{
            expenseRepository.getAllStoreExpensesPerDay(storeId, start, end).collect {
                _uiState.value = ExpensesScreenUiState.Success(it)
            }
        }catch(e: Exception) {
            _uiState.value = ExpensesScreenUiState.Error("Error: ${e.message ?: "Unknown error"}")
        }
    }
}

sealed class ExpensesScreenUiState {
    data object Loading : ExpensesScreenUiState()
    data class Success(val expenses: List<Expense>) : ExpensesScreenUiState()
    data class Error(val message: String) : ExpensesScreenUiState()
}