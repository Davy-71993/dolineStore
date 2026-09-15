package com.example.doline.views.screens.store.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.STAFF_PASS_KEY_LENGTH
import com.example.doline.data.StaffRepository
import com.example.doline.data.StaffSessionManager
import com.example.doline.data.toPassKeyInput
import com.example.doline.sha256
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Lets whoever is currently clocked in change their own pass key (no admin needed).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePassKeyScreen(navController: NavController, viewModel: ChangePassKeyViewModel = hiltViewModel()) {
    val currentPassKey by viewModel.currentPassKey.collectAsState()
    val newPassKey by viewModel.newPassKey.collectAsState()
    val confirmPassKey by viewModel.confirmPassKey.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(uiState) {
        if (uiState is ChangePassKeyUiState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Change pass key", variant = TextType.Heading, maxLines = 1) },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    subtitleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                modifier = Modifier.shadow(Spacing.MD),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_left),
                            contentDescription = "Back",
                            modifier = Modifier.size(IconSize.NORMAL),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {},
            )
        },
    ) { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = p.calculateTopPadding())
                .padding(Spacing.MD)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(Spacing.XL)
        ) {
            AppText(
                "Changing pass key for ${viewModel.staffName}",
                variant = TextType.Small,
                color = colorScheme.onBackground.copy(.6f)
            )
            Column(
                modifier = Modifier
                    .background(colorScheme.surface, MaterialTheme.shapes.medium)
                    .padding(Spacing.XL),
                verticalArrangement = Arrangement.spacedBy(Spacing.XL)
            ) {
                TextInputField(
                    value = currentPassKey,
                    onValueChange = viewModel::onCurrentPassKeyChange,
                    label = "Current pass key",
                    isPassword = true,
                    placeHolder = "Your current pass key",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
                TextInputField(
                    value = newPassKey,
                    onValueChange = viewModel::onNewPassKeyChange,
                    label = "New pass key",
                    isPassword = true,
                    placeHolder = "Choose a new $STAFF_PASS_KEY_LENGTH-digit pass key",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
                TextInputField(
                    value = confirmPassKey,
                    onValueChange = viewModel::onConfirmPassKeyChange,
                    label = "Confirm new pass key",
                    isPassword = true,
                    isError = uiState is ChangePassKeyUiState.Error,
                    placeHolder = "Re-enter your new $STAFF_PASS_KEY_LENGTH-digit pass key",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
                if (uiState is ChangePassKeyUiState.Error) {
                    AppText(
                        (uiState as ChangePassKeyUiState.Error).message,
                        color = colorScheme.error
                    )
                }
                Spacer(Modifier.height(10.dp))
                AppButton(
                    "Save",
                    { viewModel.save() },
                    Modifier,
                    ButtonType.Primary,
                    isLoading = uiState is ChangePassKeyUiState.Loading
                )
            }
        }
    }
}

@HiltViewModel
class ChangePassKeyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val staffRepository: StaffRepository,
    private val sessionManager: StaffSessionManager
) : ViewModel() {

    private val storeId: Long? = savedStateHandle.get<Long>("storeId")
    val staffName: String = sessionManager.session.value?.staff?.profile?.fullNames ?: "you"

    private val _currentPassKey = MutableStateFlow("")
    val currentPassKey: StateFlow<String> = _currentPassKey.asStateFlow()

    private val _newPassKey = MutableStateFlow("")
    val newPassKey: StateFlow<String> = _newPassKey.asStateFlow()

    private val _confirmPassKey = MutableStateFlow("")
    val confirmPassKey: StateFlow<String> = _confirmPassKey.asStateFlow()

    private val _uiState = MutableStateFlow<ChangePassKeyUiState>(ChangePassKeyUiState.Idle)
    val uiState: StateFlow<ChangePassKeyUiState> = _uiState.asStateFlow()

    fun onCurrentPassKeyChange(value: String) { _currentPassKey.value = value.toPassKeyInput(); clearError() }
    fun onNewPassKeyChange(value: String) { _newPassKey.value = value.toPassKeyInput(); clearError() }
    fun onConfirmPassKeyChange(value: String) { _confirmPassKey.value = value.toPassKeyInput(); clearError() }

    private fun clearError() {
        if (_uiState.value is ChangePassKeyUiState.Error) _uiState.value = ChangePassKeyUiState.Idle
    }

    fun save() {
        val storeId = storeId
        val staff = sessionManager.session.value?.staff
        if (storeId == null || staff == null) {
            _uiState.value = ChangePassKeyUiState.Error("No active staff session found.")
            return
        }
        if (_newPassKey.value.length != STAFF_PASS_KEY_LENGTH) {
            _uiState.value = ChangePassKeyUiState.Error("New pass key must be exactly $STAFF_PASS_KEY_LENGTH digits.")
            return
        }
        if (_newPassKey.value != _confirmPassKey.value) {
            _uiState.value = ChangePassKeyUiState.Error("New pass keys don't match.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ChangePassKeyUiState.Loading
            val verified = staffRepository.verifyPassKey(staff.staff.id, _currentPassKey.value)
            if (verified == null) {
                _uiState.value = ChangePassKeyUiState.Error("Current pass key is incorrect.")
                return@launch
            }
            val updatedStaff = verified.staff.copy(passKeyHash = _newPassKey.value.sha256())
            staffRepository.editStaff(updatedStaff)
            sessionManager.login(storeId, verified.copy(staff = updatedStaff))
            _uiState.value = ChangePassKeyUiState.Success
        }
    }
}

sealed class ChangePassKeyUiState {
    data object Idle : ChangePassKeyUiState()
    data object Loading : ChangePassKeyUiState()
    data object Success : ChangePassKeyUiState()
    data class Error(val message: String) : ChangePassKeyUiState()
}
