package com.example.doline.views.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doline.R
import com.example.doline.data.AuthRepository
import com.example.doline.data.STAFF_PASS_KEY_LENGTH
import com.example.doline.data.StaffEntity
import com.example.doline.data.StaffRepository
import com.example.doline.data.StaffSessionManager
import com.example.doline.data.UserProfile
import com.example.doline.data.UserProfileRepository
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

// Shown once per store, before anything else, so the first person in can set up the admin
// account that will unlock the till and manage other staff going forward.
@Composable
fun AdminSetupScreen(
    storeId: Long,
    viewModel: AdminSetupViewModel = hiltViewModel()
) {
    val fullNames by viewModel.fullNames.collectAsState()
    val username by viewModel.username.collectAsState()
    val passKey by viewModel.passKey.collectAsState()
    val confirmPassKey by viewModel.confirmPassKey.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.XL)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.staff),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(IconSize.BIG)
            )
            AppText(
                "Set up your admin account",
                variant = TextType.Heading,
                modifier = Modifier.padding(top = Spacing.MD)
            )
            AppText(
                "You're the first one in. Create an admin pass key to unlock the till and add other staff.",
                color = MaterialTheme.colorScheme.onBackground.copy(.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.XS, bottom = Spacing.LG)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                    .padding(Spacing.XL),
                verticalArrangement = Arrangement.spacedBy(Spacing.MD)
            ) {
                TextInputField(
                    value = fullNames,
                    onValueChange = viewModel::onFullNamesChange,
                    label = "Full names",
                    placeHolder = "Your full names",
                )
                TextInputField(
                    value = username,
                    onValueChange = viewModel::onUsernameChange,
                    label = "Username",
                    placeHolder = "Your username",
                )
                TextInputField(
                    value = passKey,
                    onValueChange = viewModel::onPassKeyChange,
                    label = "Pass key",
                    isPassword = true,
                    placeHolder = "Choose a $STAFF_PASS_KEY_LENGTH-digit pass key",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
                TextInputField(
                    value = confirmPassKey,
                    onValueChange = viewModel::onConfirmPassKeyChange,
                    label = "Confirm pass key",
                    isPassword = true,
                    isError = errorMessage != null,
                    placeHolder = "Re-enter your $STAFF_PASS_KEY_LENGTH-digit pass key",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
                errorMessage?.let {
                    AppText(it, color = MaterialTheme.colorScheme.error)
                }
                AppButton(
                    "Create admin account",
                    onClick = { viewModel.createAdmin(storeId) },
                    type = ButtonType.Primary,
                    isLoading = isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@HiltViewModel
class AdminSetupViewModel @Inject constructor(
    private val profileRepository: UserProfileRepository,
    private val staffRepository: StaffRepository,
    private val sessionManager: StaffSessionManager,
    authRepository: AuthRepository
) : ViewModel() {

    // The admin staff record references the owner's existing profile rather than duplicating it.
    private val ownerUserId: String? = authRepository.currentUser?.id
    private var ownerProfile: UserProfile? = null

    private val _fullNames = MutableStateFlow("")
    val fullNames: StateFlow<String> = _fullNames.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _passKey = MutableStateFlow("")
    val passKey: StateFlow<String> = _passKey.asStateFlow()

    private val _confirmPassKey = MutableStateFlow("")
    val confirmPassKey: StateFlow<String> = _confirmPassKey.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        val userId = ownerUserId
        if (userId != null) {
            viewModelScope.launch {
                val profile = profileRepository.getProfileByUserIdOnce(userId)
                ownerProfile = profile
                if (profile != null) {
                    _fullNames.value = profile.fullNames ?: ""
                    _username.value = profile.username ?: profile.email?.substringBefore("@") ?: ""
                }
            }
        }
    }

    fun onFullNamesChange(value: String) { _fullNames.value = value }
    fun onUsernameChange(value: String) { _username.value = value }
    fun onPassKeyChange(value: String) { _passKey.value = value.toPassKeyInput(); _errorMessage.value = null }
    fun onConfirmPassKeyChange(value: String) { _confirmPassKey.value = value.toPassKeyInput(); _errorMessage.value = null }

    fun createAdmin(storeId: Long) {
        if (_fullNames.value.isBlank() || _username.value.isBlank()) {
            _errorMessage.value = "Full names and username are required."
            return
        }
        if (_passKey.value.length != STAFF_PASS_KEY_LENGTH) {
            _errorMessage.value = "Pass key must be exactly $STAFF_PASS_KEY_LENGTH digits."
            return
        }
        if (_passKey.value != _confirmPassKey.value) {
            _errorMessage.value = "Pass keys don't match."
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            val existing = ownerProfile
            val profileId = if (existing != null) {
                profileRepository.updateProfile(
                    existing.copy(fullNames = _fullNames.value.trim(), username = _username.value.trim())
                )
                existing.id
            } else {
                profileRepository.insertProfile(
                    UserProfile(
                        userId = ownerUserId,
                        fullNames = _fullNames.value.trim(),
                        username = _username.value.trim()
                    )
                )
            }
            val id = staffRepository.insert(
                StaffEntity(
                    storeId = storeId,
                    role = "Admin",
                    passKeyHash = _passKey.value.sha256(),
                    isAdmin = true,
                    profileId = profileId
                )
            )
            val staff = staffRepository.getStaffByIdOnce(id)
            _isSaving.value = false
            if (staff != null) {
                sessionManager.login(storeId, staff)
            } else {
                _errorMessage.value = "Could not create the admin account. Try again."
            }
        }
    }
}
