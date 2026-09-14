package com.example.doline.views.screens.store.clients

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.ClientEntity
import com.example.doline.data.ClientRepository
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(navController: NavController, viewModel: ClientScreenViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    if (uiState is ClientScreenUiState.Success) {
        val client = (uiState as ClientScreenUiState.Success).client

        EditClientSheet(
            open = editing,
            client = client,
            onClose = { editing = false },
            onSave = { updated ->
                viewModel.editClient(updated)
                editing = false
            }
        )

        if (deleting) {
            AlertDialog(
                title = { AppText("Delete client!") },
                onDismissRequest = { deleting = false },
                text = { AppText("Are you sure you want to delete ${client.name}? This can not be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteClient(client)
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
                title = { AppText("Client", variant = TextType.Heading, maxLines = 1) },
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
                    if (uiState is ClientScreenUiState.Success) {
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
                                text = { AppText("Edit client") },
                                onClick = {
                                    editing = true
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(painter = painterResource(R.drawable.user_pen), contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { AppText("Delete client", color = colorScheme.error) },
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
        }
    ) {
        when (val state = uiState) {
            is ClientScreenUiState.Loading -> LoadingScreen()
            is ClientScreenUiState.Error -> ErrorMessage(message = state.message)
            is ClientScreenUiState.Success -> {
                val client = state.client
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(Spacing.MD),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                ) {
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
                            AppText(client.name, variant = TextType.Heading)
                            AppText(
                                "Client since ${timestampToDate(client.createdAt)}",
                                variant = TextType.Small,
                                color = colorScheme.onBackground.copy(.6f)
                            )
                        }
                    }
                    ClientDetailRow(icon = R.drawable.call, label = "Phone", value = client.phone ?: "Not provided")
                    ClientDetailRow(icon = R.drawable.notes, label = "Address", value = client.address ?: "Not provided")
                }
            }
        }
    }
}

@Composable
private fun ClientDetailRow(icon: Int, label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(colorScheme.surface, RoundedCornerShape(Rounding.SM))
            .padding(Spacing.MD),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditClientSheet(
    open: Boolean,
    client: ClientEntity,
    onClose: () -> Unit,
    onSave: (ClientEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember(client) { mutableStateOf(client.name) }
    var phone by remember(client) { mutableStateOf(client.phone ?: "") }
    var address by remember(client) { mutableStateOf(client.address ?: "") }
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
                AppText("Edit client", variant = TextType.Heading)
                if (error.isNotBlank()) {
                    ErrorMessage(error)
                }
                TextInputField(
                    value = name,
                    onValueChange = { name = it; error = "" },
                    label = "Name",
                    placeHolder = "Client's full name",
                )
                TextInputField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone",
                    placeHolder = "Client's phone number",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                TextInputField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address",
                    placeHolder = "Client's address",
                )
                Spacer(Modifier.height(10.dp))
                AppButton(
                    text = "Save",
                    onClick = {
                        if (name.isBlank()) {
                            error = "The client's name is required."
                            return@AppButton
                        }
                        onSave(
                            client.copy(
                                name = name.trim(),
                                phone = phone.trim().ifBlank { null },
                                address = address.trim().ifBlank { null }
                            )
                        )
                    },
                    type = ButtonType.Primary
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@HiltViewModel
class ClientScreenViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val clientRepository: ClientRepository
) : ViewModel() {
    val storeId = savedState.get<Long>("storeId")
    val clientId = savedState.get<Long>("clientId")

    private val _uiState = MutableStateFlow<ClientScreenUiState>(ClientScreenUiState.Loading)
    val uiState: StateFlow<ClientScreenUiState> = _uiState

    private suspend fun fetchClient() {
        if (clientId == null) {
            _uiState.value = ClientScreenUiState.Error("The client ID is undefined")
            return
        }
        clientRepository.getClientById(clientId).collect { client ->
            _uiState.value = if (client == null) {
                ClientScreenUiState.Error("Client not found")
            } else {
                ClientScreenUiState.Success(client)
            }
        }
    }

    init {
        viewModelScope.launch { fetchClient() }
    }

    fun editClient(client: ClientEntity) {
        viewModelScope.launch {
            try {
                clientRepository.editClient(client)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            try {
                clientRepository.deleteClient(client)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

sealed class ClientScreenUiState {
    data object Loading : ClientScreenUiState()
    data class Success(val client: ClientEntity) : ClientScreenUiState()
    data class Error(val message: String) : ClientScreenUiState()
}
