package com.example.doline.views.screens.store.suppliers

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.SupplierEntity
import com.example.doline.data.SupplierRepository
import com.example.doline.data.SupplierWithProfile
import com.example.doline.data.UserProfile
import com.example.doline.data.UserProfileRepository
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.EmptyMessage
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.Screen
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(navController: NavController, viewModel: SuppliersScreenViewModel) {
    val storeId = viewModel.storeId
    val uiState by viewModel.uiState.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()

    var openAddSheet by remember { mutableStateOf(false) }

    AddSupplierSheet(
        open = openAddSheet,
        onClose = { openAddSheet = false },
        onSave = { name, phone, address ->
            viewModel.addSupplier(name, phone, address)
            openAddSheet = false
        }
    )

    Screen(
        topAppBar = {
            TopAppBar(
                title = { AppText("Suppliers", variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier
                    .padding(vertical = 0.dp)
                    .shadow(10.dp),
                colors = TopAppBarColors(
                    containerColor = colorScheme.background,
                    scrolledContainerColor = colorScheme.background,
                    navigationIconContentColor = colorScheme.onBackground,
                    titleContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground,
                    subtitleContentColor = colorScheme.onBackground,
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_left),
                            contentDescription = "Back",
                            modifier = Modifier.size(IconSize.BIG),
                            tint = colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = "Search",
                            modifier = Modifier.size(IconSize.BIG),
                            tint = colorScheme.onBackground
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddSheet = true },
                containerColor = colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp, 6.dp),
                shape = RoundedCornerShape(Rounding.FULL),
                modifier = Modifier
                    .padding(Spacing.SM)
                    .absoluteOffset(y = (-60).dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.user_add),
                    contentDescription = "Add new supplier",
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(IconSize.BIG)
                )
            }
        }
    ) {
        when (val state = uiState) {
            is SuppliersScreenUiState.Loading -> LoadingScreen()
            is SuppliersScreenUiState.Error -> ErrorMessage(message = state.message)
            is SuppliersScreenUiState.Success -> {
                if (suppliers.isEmpty()) {
                    EmptyMessage("No suppliers added yet.")
                } else {
                    LazyColumn(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                    ) {
                        items(suppliers, key = { it.supplier.id }) { supplier ->
                            SupplierListItem(
                                supplier = supplier,
                                onClick = { navController.navigate("$storeId/suppliers/${supplier.supplier.id}") }
                            )
                        }
                        item { Spacer(Modifier.height(70.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierListItem(supplier: SupplierWithProfile, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(colorScheme.surface)
            .padding(Spacing.MD, Spacing.SM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            AppText(supplier.profile.fullNames ?: "Unnamed supplier", variant = TextType.Label)
            Spacer(Modifier.height(2.dp))
            AppText(
                supplier.profile.phone ?: supplier.profile.defaultAddress ?: "No contact details",
                variant = TextType.Small,
                color = colorScheme.onBackground.copy(.6f)
            )
        }
        Icon(
            painter = painterResource(R.drawable.arrow_right),
            contentDescription = null,
            modifier = Modifier.size(IconSize.NORMAL),
            tint = colorScheme.onBackground.copy(.4f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSupplierSheet(
    open: Boolean,
    onClose: () -> Unit,
    onSave: (name: String, phone: String?, address: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    if (open) {
        ModalBottomSheet(
            onDismissRequest = {
                name = ""
                phone = ""
                address = ""
                error = ""
                onClose()
            },
            sheetState = sheetState
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD, 0.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.MD)
            ) {
                AppText("Add new supplier", variant = TextType.Heading)
                if (error.isNotBlank()) {
                    ErrorMessage(error)
                }
                TextInputField(
                    value = name,
                    onValueChange = { name = it; error = "" },
                    label = "Name",
                    placeHolder = "Supplier's name",
                )
                TextInputField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone",
                    placeHolder = "Supplier's phone number",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                TextInputField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address",
                    placeHolder = "Supplier's address",
                )
                Spacer(Modifier.height(10.dp))
                AppButton(
                    text = "Save",
                    onClick = {
                        if (name.isBlank()) {
                            error = "The supplier's name is required."
                            return@AppButton
                        }
                        onSave(name.trim(), phone.trim().ifBlank { null }, address.trim().ifBlank { null })
                        name = ""
                        phone = ""
                        address = ""
                        error = ""
                    },
                    type = ButtonType.Primary
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@HiltViewModel
class SuppliersScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val supplierRepository: SupplierRepository,
    private val profileRepository: UserProfileRepository
) : ViewModel() {
    val storeId = savedStateHandle.get<Long>("storeId")

    private val _uiState = MutableStateFlow<SuppliersScreenUiState>(SuppliersScreenUiState.Loading)
    val uiState: StateFlow<SuppliersScreenUiState> = _uiState

    private val _suppliers = MutableStateFlow<List<SupplierWithProfile>>(emptyList())
    val suppliers: StateFlow<List<SupplierWithProfile>> = _suppliers

    init {
        viewModelScope.launch { fetchSuppliers() }
    }

    private suspend fun fetchSuppliers() {
        if (storeId == null) {
            _uiState.value = SuppliersScreenUiState.Error("The store ID can not be null.")
            return
        }
        try {
            supplierRepository.getSuppliers(storeId).collect {
                _suppliers.value = it
                _uiState.value = SuppliersScreenUiState.Success
            }
        } catch (e: Exception) {
            _uiState.value = SuppliersScreenUiState.Error(e.message ?: "Failed to fetch suppliers")
        }
    }

    fun addSupplier(name: String, phone: String?, address: String?) {
        if (storeId == null) return
        viewModelScope.launch {
            try {
                val profileId = profileRepository.insertProfile(
                    UserProfile(fullNames = name, phone = phone, defaultAddress = address)
                )
                supplierRepository.insert(SupplierEntity(storeId = storeId, profileId = profileId))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

sealed class SuppliersScreenUiState {
    data object Loading : SuppliersScreenUiState()
    data object Success : SuppliersScreenUiState()
    data class Error(val message: String) : SuppliersScreenUiState()
}
