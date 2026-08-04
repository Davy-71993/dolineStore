package com.example.doline.views.screens.store.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.doline.R
import com.example.doline.capitalize
import com.example.doline.data.Currency
import com.example.doline.data.Expense
import com.example.doline.data.ExpenseCategory
import com.example.doline.data.ExpenseRepository
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.FormScreen
import com.example.doline.views.components.PriceInputField
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExpenseScreen(navController: NavHostController, viewModel: CreateExpenseViewModel) {

    val expenseCategories = ExpenseCategory.getExpenseCategories()
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val amount by viewModel.amount.collectAsState()
    val description by viewModel.description.collectAsState()
    val category by viewModel.category.collectAsState()

    if (uiState is CreateExpensesScreenUiState.Success){
        navController.popBackStack()
    }

    FormScreen (
        appBar = {
            TopAppBar(
                title = { AppText("Add new Expense", variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier.padding(vertical = 0.dp),
                scrollBehavior = scrollBehavior,
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
                }
            )
        },
    ) {
            if(uiState is CreateExpensesScreenUiState.Error){
                AppText(
                    (uiState as CreateExpensesScreenUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.shapes.medium
                        )
                        .padding(Spacing.MD)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.XXS)
            ) {
                AppText("EXPENSE CATEGORY", variant = TextType.LabelSmall)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    expenseCategories.forEach { entry ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.XS),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if(category == entry)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(Rounding.MD)
                                )
                                .border(
                                    shape = RoundedCornerShape(Rounding.MD),
                                    border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.onBackground.copy(.5f))
                                )
                                .padding(Spacing.MD)
                                .clickable {
                                    viewModel.onCategoryChanged(entry)
                                },
                        ) {
                            ExpenseIcon(
                                category = entry,
                                colored = false,
                                rounded = true,
                                padding = Spacing.MD,
                                background = if (category == entry)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.background,
                                color = if (category == entry)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onBackground,
                            )
                            AppText(
                                entry.name.capitalize(),
                                variant = TextType.Small,
                                color  = if(category == entry)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        PriceInputField(
            currency = Currency.UGX,
            amount = amount,
            onChange = {viewModel.onAmountChanged(it)},
            label = "AMOUNT"
        )
        TextInputField(
            value = description,
            onValueChange = {viewModel.onDescriptionChanged(it)},
            singleLine = false,
            minLines = 2,
            label = "DESCRIPTION"
        )

        AppButton(
            type = ButtonType.Primary,
            text = "Save Expense",
            onClick = {
                viewModel.insertExpense()
            },
            icon = R.drawable.save,
            iconTint = MaterialTheme.colorScheme.onPrimary,
            isLoading = uiState is CreateExpensesScreenUiState.Loading,
            buttonHeight = 56.dp,
            enabled = (amount != null && category != null && description.isNotEmpty())
        )

        Spacer(
            modifier = Modifier.height(80.dp)
        )
    }
}


@HiltViewModel
class CreateExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private  val expenseRepository: ExpenseRepository,
) : ViewModel() {
    val storeId = savedStateHandle.get<Long>("storeId")
    private val _uiState = MutableStateFlow<CreateExpensesScreenUiState>(CreateExpensesScreenUiState.Idle)
    val uiState: StateFlow<CreateExpensesScreenUiState> = _uiState.asStateFlow()

    private val _amount = MutableStateFlow<Double?>(null)
    var amount: StateFlow<Double?> = _amount

    private val _category = MutableStateFlow<ExpenseCategory?>(null)
    var category: StateFlow<ExpenseCategory?> = _category

    private val _description = MutableStateFlow("")
    var description: StateFlow<String> = _description

    fun onAmountChanged(am: Double?){
        _amount.value = am
    }

    fun onCategoryChanged(cat: ExpenseCategory?){
        _category.value = cat
    }

    fun onDescriptionChanged(desc: String){
        _description.value = desc
    }

    fun insertExpense() {
        viewModelScope.launch {
            try {
                _uiState.value = CreateExpensesScreenUiState.Loading
                val category = _category.value
                val amount = _amount.value
                val desc = _description.value
                if(desc.isEmpty() || category == null || amount == null || storeId == null){
                    _uiState.value = CreateExpensesScreenUiState.Error("Fill out all the fields.")
                    return@launch
                }
                val expense = Expense(
                    category = category,
                    description = desc,
                    amount = amount,
                    storeId = storeId
                )
                expenseRepository.insert(expense)
                _uiState.value = CreateExpensesScreenUiState.Success
            } catch (e: Exception) {
                _uiState.value = CreateExpensesScreenUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class CreateExpensesScreenUiState {
    data object Idle: CreateExpensesScreenUiState()
    data object Loading: CreateExpensesScreenUiState()
    data object Success: CreateExpensesScreenUiState()
    data class Error(val message: String) : CreateExpensesScreenUiState()
}