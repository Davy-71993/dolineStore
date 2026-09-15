package com.example.doline.views.screens.store.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.example.doline.data.StaffEntity
import com.example.doline.data.StaffRepository
import com.example.doline.data.StaffSessionManager
import com.example.doline.data.StaffWithProfile
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStaffScreen(navController: NavController, viewModel: CreateStaffViewModel = hiltViewModel()){
    val fullNames by viewModel.fullNames.collectAsState()
    val username by viewModel.username.collectAsState()
    val role by viewModel.role.collectAsState()
    val passKey by viewModel.passKey.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val canGrantAdmin by viewModel.canGrantAdmin.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(uiState) {
        if (uiState is CreateStaffUiState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AppText(
                        if (viewModel.isEditing) "Edit staff" else "Add new staff",
                        variant = TextType.Heading,
                        maxLines = 1
                    )
                },
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
                },
            )
        },
    ) {p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = p.calculateTopPadding())
                .padding(Spacing.MD)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(Spacing.XL)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        colorScheme.surface,
                        MaterialTheme.shapes.medium
                    )
                    .padding(Spacing.XL),
                verticalArrangement = Arrangement.spacedBy(Spacing.XL)
            ) {
                TextInputField(
                    value = fullNames,
                    onValueChange = viewModel::onFullNamesChange,
                    label = "Full names",
                    placeHolder = "Staff's full names",
                )
                TextInputField(
                    value = username,
                    onValueChange = viewModel::onUsernameChange,
                    label = "Username",
                    placeHolder = "Staff's username",
                )
                TextInputField(
                    value = role,
                    onValueChange = viewModel::onRoleChange,
                    label = "Role",
                    placeHolder = "Staff's role",
                )
                TextInputField(
                    value = passKey,
                    onValueChange = viewModel::onPassKeyChange,
                    label = "Pass key",
                    isPassword = true,
                    placeHolder = if (viewModel.isEditing) "Leave blank to keep current pass key" else "$STAFF_PASS_KEY_LENGTH-digit pass key",
                    isError = uiState is CreateStaffUiState.Error,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                )
                if (canGrantAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            AppText("Admin access", variant = TextType.Label)
                            AppText(
                                "Can manage staff and store settings",
                                variant = TextType.Small,
                                color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                            )
                        }
                        Switch(checked = isAdmin, onCheckedChange = viewModel::onIsAdminChange)
                    }
                }
                if (uiState is CreateStaffUiState.Error) {
                    AppText(
                        (uiState as CreateStaffUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.height(10.dp))
                AppButton(
                    "Save",
                    { viewModel.save() },
                    Modifier,
                    ButtonType.Primary,
                    isLoading = uiState is CreateStaffUiState.Loading
                )
            }
        }
    }
}

@HiltViewModel
class CreateStaffViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val staffRepository: StaffRepository,
    private val profileRepository: UserProfileRepository,
    sessionManager: StaffSessionManager
) : ViewModel() {

    private val storeId: Long? = savedStateHandle.get<Long>("storeId")
    private val staffId: Long? = savedStateHandle.get<Long>("staffId")
    val isEditing: Boolean = staffId != null

    // Only an admin can grant admin access to a new or existing staff member.
    private val actingStaffIsAdmin: Boolean = sessionManager.session.value?.staff?.staff?.isAdmin == true
    private val _canGrantAdmin = MutableStateFlow(actingStaffIsAdmin)
    val canGrantAdmin: StateFlow<Boolean> = _canGrantAdmin.asStateFlow()

    private val _fullNames = MutableStateFlow("")
    val fullNames: StateFlow<String> = _fullNames.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _role = MutableStateFlow("")
    val role: StateFlow<String> = _role.asStateFlow()

    private val _passKey = MutableStateFlow("")
    val passKey: StateFlow<String> = _passKey.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _uiState = MutableStateFlow<CreateStaffUiState>(CreateStaffUiState.Idle)
    val uiState: StateFlow<CreateStaffUiState> = _uiState.asStateFlow()

    private var existingStaff: StaffWithProfile? = null

    init {
        val id = staffId
        if (id != null) {
            viewModelScope.launch {
                val staff = staffRepository.getStaffByIdOnce(id)
                if (staff != null) {
                    existingStaff = staff
                    _fullNames.value = staff.profile.fullNames ?: ""
                    _username.value = staff.profile.username ?: ""
                    _role.value = staff.staff.role ?: ""
                    _isAdmin.value = staff.staff.isAdmin
                }
            }
        }
    }

    fun onFullNamesChange(value: String) { _fullNames.value = value }
    fun onUsernameChange(value: String) { _username.value = value }
    fun onRoleChange(value: String) { _role.value = value }
    fun onPassKeyChange(value: String) { _passKey.value = value.toPassKeyInput() }
    fun onIsAdminChange(value: Boolean) { _isAdmin.value = value }

    fun save() {
        val storeId = storeId
        if (storeId == null) {
            _uiState.value = CreateStaffUiState.Error("Could not determine the current store.")
            return
        }
        if (_fullNames.value.isBlank() || _username.value.isBlank() || _role.value.isBlank()) {
            _uiState.value = CreateStaffUiState.Error("Full names, username and role are required.")
            return
        }
        val passKeyRequired = !isEditing || _passKey.value.isNotEmpty()
        if (passKeyRequired && _passKey.value.length != STAFF_PASS_KEY_LENGTH) {
            _uiState.value = CreateStaffUiState.Error("Pass key must be exactly $STAFF_PASS_KEY_LENGTH digits.")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateStaffUiState.Loading

            val current = existingStaff
            val passKeyHash = if (_passKey.value.isNotEmpty()) {
                _passKey.value.sha256()
            } else {
                current?.staff?.passKeyHash ?: ""
            }
            // Only trust the admin toggle if this actor is actually allowed to grant it.
            val isAdminValue = if (actingStaffIsAdmin) _isAdmin.value else (current?.staff?.isAdmin ?: false)

            if (current != null) {
                profileRepository.updateProfile(
                    current.profile.copy(
                        fullNames = _fullNames.value.trim(),
                        username = _username.value.trim()
                    )
                )
                staffRepository.editStaff(
                    current.staff.copy(
                        role = _role.value.trim(),
                        passKeyHash = passKeyHash,
                        isAdmin = isAdminValue
                    )
                )
            } else {
                val profileId = profileRepository.insertProfile(
                    UserProfile(
                        fullNames = _fullNames.value.trim(),
                        username = _username.value.trim()
                    )
                )
                staffRepository.insert(
                    StaffEntity(
                        storeId = storeId,
                        role = _role.value.trim(),
                        passKeyHash = passKeyHash,
                        isAdmin = isAdminValue,
                        profileId = profileId
                    )
                )
            }
            _uiState.value = CreateStaffUiState.Success
        }
    }
}

sealed class CreateStaffUiState {
    data object Idle : CreateStaffUiState()
    data object Loading : CreateStaffUiState()
    data object Success : CreateStaffUiState()
    data class Error(val message: String) : CreateStaffUiState()
}
