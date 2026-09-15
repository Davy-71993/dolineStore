package com.example.doline.views.screens.store.suppliers

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.ItemEntity
import com.example.doline.data.ItemRepository
import com.example.doline.data.SupplierRepository
import com.example.doline.data.SupplierWithProfile
import com.example.doline.data.UserProfileRepository
import com.example.doline.timestampToDate
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.Screen
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierScreen(navController: NavController, viewModel: SupplierScreenViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val suppliedItems by viewModel.suppliedItems.collectAsState()
    val availableItems by viewModel.availableItems.collectAsState()
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var addingItem by remember { mutableStateOf(false) }

    AddSuppliedItemSheet(
        open = addingItem,
        availableItems = availableItems,
        onClose = { addingItem = false },
        onSelect = { itemId ->
            viewModel.linkItem(itemId)
            addingItem = false
        }
    )

    if (uiState is SupplierScreenUiState.Success) {
        val supplier = (uiState as SupplierScreenUiState.Success).supplier

        EditSupplierSheet(
            open = editing,
            supplier = supplier,
            onClose = { editing = false },
            onSave = { name, phone, address ->
                viewModel.editSupplier(name, phone, address)
                editing = false
            }
        )

        if (deleting) {
            AlertDialog(
                title = { AppText("Delete supplier!") },
                onDismissRequest = { deleting = false },
                text = { AppText("Are you sure you want to delete ${supplier.profile.fullNames}? This can not be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteSupplier(supplier)
                        deleting = false
                        navController.popBackStack()
                    }) {
                        AppText("Yes", color = colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleting = false }) {
                        AppText("Cancel")
                    }
                }
            )
        }
    }

    Screen(
        topAppBar = {
            TopAppBar(
                title = { AppText("Supplier", variant = TextType.Heading, maxLines = 1) },
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
                    if (uiState is SupplierScreenUiState.Success) {
                        val supplier = (uiState as SupplierScreenUiState.Success).supplier
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                painter = painterResource(R.drawable.more),
                                contentDescription = "Menu",
                                modifier = Modifier.size(IconSize.BIG)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(colorScheme.surface),
                            offset = DpOffset(10)
                        ) {
                            DropdownMenuItem(
                                text = { AppText("Edit supplier") },
                                onClick = {
                                    editing = true
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.user_pen),
                                        contentDescription = null,
                                        modifier = Modifier.size(IconSize.NORMAL)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { AppText("Message supplier") },
                                onClick = {
                                    expanded = false
                                    navController.navigate("store/${viewModel.storeId}/inbox/${supplier.supplier.id}")
                                },
                                leadingIcon = {
                                    Icon(painter = painterResource(R.drawable.chat), contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { AppText("Delete supplier", color = colorScheme.error) },
                                onClick = {
                                    deleting = true
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.trash),
                                        contentDescription = null,
                                        tint = colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is SupplierScreenUiState.Success) {
                FloatingActionButton(
                    onClick = { addingItem = true },
                    containerColor = colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp, 6.dp),
                    shape = RoundedCornerShape(Rounding.FULL),
                    modifier = Modifier
                        .padding(Spacing.SM)
                        .absoluteOffset(y = (-60).dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.plus),
                        contentDescription = "Add item supplied",
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(IconSize.BIG)
                    )
                }
            }
        }
    ) {
        when (val state = uiState) {
            is SupplierScreenUiState.Loading -> LoadingScreen()
            is SupplierScreenUiState.Error -> ErrorMessage(message = state.message)
            is SupplierScreenUiState.Success -> {
                val supplier = state.supplier
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(colorScheme.surface, RoundedCornerShape(Rounding.SM))
                                .padding(Spacing.MD),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.user),
                                contentDescription = null,
                                modifier = Modifier.size(IconSize.BIG),
                                tint = colorScheme.onBackground
                            )
                            Spacer(Modifier.width(Spacing.SM))
                            Column {
                                AppText(supplier.profile.fullNames ?: "Unnamed supplier", variant = TextType.Heading)
                                AppText(
                                    "Supplier since ${timestampToDate(supplier.supplier.createdAt)}",
                                    variant = TextType.Small,
                                    color = colorScheme.onBackground.copy(.6f)
                                )
                            }
                        }
                    }
                    item {
                        SupplierDetailRow(
                            icon = R.drawable.call,
                            label = "Phone",
                            value = supplier.profile.phone ?: "Not provided",
                            trailing = supplier.profile.phone?.let { phone ->
                                {
                                    IconButton(onClick = {
                                        try {
                                            context.startActivity(
                                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }) {
                                        Icon(
                                            painter = painterResource(R.drawable.call),
                                            contentDescription = "Call",
                                            tint = colorScheme.primary,
                                            modifier = Modifier.size(IconSize.NORMAL)
                                        )
                                    }
                                }
                            }
                        )
                    }
                    item {
                        SupplierDetailRow(icon = R.drawable.notes, label = "Address", value = supplier.profile.defaultAddress ?: "Not provided")
                    }
                    item {
                        AppText(
                            "ITEMS SUPPLIED",
                            variant = TextType.LabelSmall,
                            color = colorScheme.onBackground.copy(.6f)
                        )
                    }
                    if (suppliedItems.isEmpty()) {
                        item {
                            AppText(
                                "No items linked yet.",
                                color = colorScheme.onBackground.copy(.6f)
                            )
                        }
                    } else {
                        items(suppliedItems, key = { it.id }) { item ->
                            SuppliedItemRow(
                                item = item,
                                onRemove = { viewModel.unlinkItem(item.id) }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(70.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SupplierDetailRow(
    icon: Int,
    label: String,
    value: String,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(colorScheme.surface, RoundedCornerShape(Rounding.SM))
            .padding(Spacing.MD),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(IconSize.NORMAL),
                tint = colorScheme.onBackground.copy(.6f)
            )
            Spacer(Modifier.width(Spacing.SM))
            Column {
                AppText(label, variant = TextType.LabelSmall, color = colorScheme.onBackground.copy(.6f))
                AppText(value)
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
private fun SuppliedItemRow(item: ItemEntity, onRemove: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(colorScheme.surface, RoundedCornerShape(Rounding.SM))
            .padding(Spacing.MD),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(item.name, variant = TextType.Label)
        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(R.drawable.trash),
                contentDescription = "Remove",
                tint = colorScheme.error,
                modifier = Modifier.size(IconSize.NORMAL)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSuppliedItemSheet(
    open: Boolean,
    availableItems: List<ItemEntity>,
    onClose: () -> Unit,
    onSelect: (itemId: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    if (open) {
        ModalBottomSheet(
            onDismissRequest = onClose,
            sheetState = sheetState
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD, 0.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.MD)
            ) {
                AppText("Add item supplied", variant = TextType.Heading)
                if (availableItems.isEmpty()) {
                    AppText(
                        "All items are already linked to this supplier.",
                        color = colorScheme.onBackground.copy(.6f)
                    )
                    Spacer(Modifier.height(20.dp))
                } else {
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    ) {
                        items(availableItems, key = { it.id }) { item ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(item.id)
                                    }
                                    .padding(Spacing.MD, Spacing.SM),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppText(item.name)
                                Icon(
                                    painter = painterResource(R.drawable.plus),
                                    contentDescription = null,
                                    modifier = Modifier.size(IconSize.NORMAL),
                                    tint = colorScheme.onBackground.copy(.6f)
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSupplierSheet(
    open: Boolean,
    supplier: SupplierWithProfile,
    onClose: () -> Unit,
    onSave: (name: String, phone: String?, address: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember(supplier) { mutableStateOf(supplier.profile.fullNames ?: "") }
    var phone by remember(supplier) { mutableStateOf(supplier.profile.phone ?: "") }
    var address by remember(supplier) { mutableStateOf(supplier.profile.defaultAddress ?: "") }
    var error by remember { mutableStateOf("") }

    if (open) {
        ModalBottomSheet(
            onDismissRequest = onClose,
            sheetState = sheetState
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD, 0.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.MD)
            ) {
                AppText("Edit supplier", variant = TextType.Heading)
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
                    },
                    type = ButtonType.Primary
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@HiltViewModel
class SupplierScreenViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val supplierRepository: SupplierRepository,
    private val profileRepository: UserProfileRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {
    val storeId = savedState.get<Long>("storeId")
    val supplierId = savedState.get<Long>("supplierId")

    private val _uiState = MutableStateFlow<SupplierScreenUiState>(SupplierScreenUiState.Loading)
    val uiState: StateFlow<SupplierScreenUiState> = _uiState

    private val _suppliedItems = MutableStateFlow<List<ItemEntity>>(emptyList())
    val suppliedItems: StateFlow<List<ItemEntity>> = _suppliedItems

    private val _availableItems = MutableStateFlow<List<ItemEntity>>(emptyList())
    val availableItems: StateFlow<List<ItemEntity>> = _availableItems

    private suspend fun fetchSupplier() {
        if (supplierId == null) {
            _uiState.value = SupplierScreenUiState.Error("The supplier ID is undefined")
            return
        }
        supplierRepository.getSupplierById(supplierId).collect { supplier ->
            _uiState.value = if (supplier == null) {
                SupplierScreenUiState.Error("Supplier not found")
            } else {
                SupplierScreenUiState.Success(supplier)
            }
        }
    }

    private suspend fun observeSuppliedItems() {
        if (supplierId == null) return
        supplierRepository.getSupplierWithItems(supplierId).collect { supplierWithItems ->
            _suppliedItems.value = supplierWithItems?.items ?: emptyList()
        }
    }

    private suspend fun observeAvailableItems() {
        val storeId = storeId ?: return
        combine(itemRepository.getAllItems(storeId), _suppliedItems) { items, supplied ->
            val suppliedIds = supplied.map { it.id }.toSet()
            items.map { it.item }.filter { it.id !in suppliedIds }
        }.collect { _availableItems.value = it }
    }

    init {
        viewModelScope.launch {
            launch { fetchSupplier() }
            launch { observeSuppliedItems() }
            launch { observeAvailableItems() }
        }
    }

    fun editSupplier(name: String, phone: String?, address: String?) {
        val current = (_uiState.value as? SupplierScreenUiState.Success)?.supplier ?: return
        viewModelScope.launch {
            try {
                profileRepository.updateProfile(
                    current.profile.copy(fullNames = name, phone = phone, defaultAddress = address)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSupplier(supplier: SupplierWithProfile) {
        viewModelScope.launch {
            try {
                supplierRepository.deleteSupplier(supplier.supplier)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun linkItem(itemId: Long) {
        val supplierId = supplierId ?: return
        viewModelScope.launch {
            try {
                supplierRepository.linkItemToSupplier(itemId, supplierId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unlinkItem(itemId: Long) {
        val supplierId = supplierId ?: return
        viewModelScope.launch {
            try {
                supplierRepository.unlinkItemFromSupplier(itemId, supplierId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

sealed class SupplierScreenUiState {
    data object Loading : SupplierScreenUiState()
    data class Success(val supplier: SupplierWithProfile) : SupplierScreenUiState()
    data class Error(val message: String) : SupplierScreenUiState()
}
