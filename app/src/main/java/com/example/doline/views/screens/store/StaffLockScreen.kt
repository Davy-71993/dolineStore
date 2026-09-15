package com.example.doline.views.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doline.R
import com.example.doline.data.StaffRepository
import com.example.doline.data.StaffSessionManager
import com.example.doline.data.StaffWithProfile
import com.example.doline.data.toPassKeyInput
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

// "Who's on shift" unlock screen shown when no staff member is currently clocked in on this device.
@Composable
fun StaffLockScreen(
    storeId: Long,
    staffs: List<StaffWithProfile>,
    viewModel: StaffLockViewModel = hiltViewModel()
) {
    val selectedStaff by viewModel.selectedStaff.collectAsState()
    val passKey by viewModel.passKey.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isVerifying by viewModel.isVerifying.collectAsState()

    // Wipe the pass key/selection the moment this screen is dismissed, not on next mount.
    DisposableEffect(Unit) {
        onDispose { viewModel.resetForNewSession() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.XL)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.lock),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(IconSize.BIG)
            )
            AppText(
                "Who's on shift?",
                variant = TextType.Heading,
                modifier = Modifier.padding(top = Spacing.MD)
            )

            val staff = selectedStaff
            if (staff == null) {
                AppText(
                    "Select your name to unlock this device",
                    color = MaterialTheme.colorScheme.onBackground.copy(.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Spacing.XS, bottom = Spacing.LG)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                ) {
                    items(staffs, key = { it.staff.id }) { member ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { viewModel.selectStaff(member) }
                                .padding(Spacing.MD)
                        ) {
                            AppText(member.profile.fullNames ?: "Unnamed staff", variant = TextType.Label)
                            member.staff.role?.takeIf { it.isNotBlank() }?.let {
                                AppText(
                                    it,
                                    variant = TextType.Small,
                                    color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppText(
                        staff.profile.fullNames ?: "Unnamed staff",
                        variant = TextType.Label,
                        modifier = Modifier.padding(top = Spacing.XS, bottom = Spacing.LG)
                    )
                    TextInputField(
                        value = passKey,
                        onValueChange = viewModel::onPassKeyChange,
                        label = "Pass key",
                        isPassword = true,
                        isError = errorMessage != null,
                        placeHolder = "Enter your pass key",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.unlock(storeId) }
                        )
                    )
                    errorMessage?.let {
                        AppText(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = Spacing.XS)
                        )
                    }
                    AppButton(
                        "Unlock",
                        onClick = { viewModel.unlock(storeId) },
                        type = ButtonType.Primary,
                        isLoading = isVerifying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.LG)
                    )
                    AppText(
                        "Not you? Choose again",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = Spacing.MD)
                            .clickable { viewModel.clearSelection() }
                    )
                }
            }
        }
    }
}

@HiltViewModel
class StaffLockViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val sessionManager: StaffSessionManager
) : ViewModel() {

    private val _selectedStaff = MutableStateFlow<StaffWithProfile?>(null)
    val selectedStaff: StateFlow<StaffWithProfile?> = _selectedStaff.asStateFlow()

    private val _passKey = MutableStateFlow("")
    val passKey: StateFlow<String> = _passKey.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    fun selectStaff(staff: StaffWithProfile) {
        _selectedStaff.value = staff
        _passKey.value = ""
        _errorMessage.value = null
    }

    fun clearSelection() {
        _selectedStaff.value = null
        _passKey.value = ""
        _errorMessage.value = null
    }

    // Clears any typed pass key/selection so it can't leak into the next lock session.
    fun resetForNewSession() {
        _selectedStaff.value = null
        _passKey.value = ""
        _errorMessage.value = null
        _isVerifying.value = false
    }

    fun onPassKeyChange(value: String) {
        _passKey.value = value.toPassKeyInput()
        _errorMessage.value = null
    }

    fun unlock(storeId: Long) {
        val staff = _selectedStaff.value ?: return
        if (_passKey.value.isEmpty()) {
            _errorMessage.value = "Enter your pass key."
            return
        }
        viewModelScope.launch {
            _isVerifying.value = true
            val verified = staffRepository.verifyPassKey(staff.staff.id, _passKey.value)
            _isVerifying.value = false
            if (verified != null) {
                sessionManager.login(storeId, verified)
            } else {
                _errorMessage.value = "Incorrect pass key."
                _passKey.value = ""
            }
        }
    }
}

/** Small gate ViewModel StoreMain uses to decide whether a staff session needs unlocking. */
@HiltViewModel
class StoreGateViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    val sessionManager: StaffSessionManager
) : ViewModel() {
    fun staffs(storeId: Long) = staffRepository.getStaffs(storeId)
}
