package com.example.doline.views.screens.store.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.StaffEntity
import com.example.doline.data.StaffRepository
import com.example.doline.data.StaffWithProfile
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffSettingsScreen(navController: NavController, viewModel: StaffSettingsViewModel = hiltViewModel()){

    val staffs by viewModel.staffs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Staff & Access", variant = TextType.Heading, maxLines = 1) },
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
                .padding(vertical = p.calculateTopPadding(), horizontal = Spacing.SM)
                .padding(vertical = Spacing.XL),
            verticalArrangement = Arrangement.spacedBy(Spacing.XL)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.XL)
            ) {
                AppText(
                    "Staff members can unlock this device with their pass key. They're auto " +
                        "clocked out after a minute of inactivity.",
                    variant = TextType.Small,
                    color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                )
                if(staffs.isEmpty()){
                    AppText(
                        "No staff members added yet.",
                        variant = TextType.Label,
                        modifier = Modifier.wrapContentSize()
                    )
                }
                else{
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.SM)
                    ) {
                        items(staffs, key = { it.staff.id }) { staffWithProfile ->
                            val staff = staffWithProfile.staff
                            val profile = staffWithProfile.profile
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.shapes.medium
                                    )
                                    .padding(Spacing.MD)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppText(profile.fullNames ?: "Unnamed staff", variant = TextType.Label)
                                    if (staff.isAdmin) {
                                        AppText(
                                            "Admin",
                                            variant = TextType.Small,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppText(
                                        "${profile.username} | ${staff.role}",
                                        variant = TextType.Body,
                                        color = MaterialTheme.colorScheme.onBackground.copy(.6f)
                                    )
                                    Row(
                                        modifier = Modifier.wrapContentSize()
                                    ) {
                                        IconButton(onClick = {
                                            navController.navigate(
                                                "store/${staff.storeId}/settings/staff_&_security/edit_staff/${staff.id}"
                                            )
                                        }) {
                                            Icon(
                                                painter = painterResource(R.drawable.edit),
                                                contentDescription = "Edit staff",
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.size(IconSize.NORMAL)
                                            )
                                        }
                                        IconButton(onClick = { viewModel.delete(staff) }) {
                                            Icon(
                                                painter = painterResource(R.drawable.trash),
                                                contentDescription = "Delete staff",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(IconSize.NORMAL)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AppButton(
                "Add new staff",
                {
                    viewModel.storeId?.let { storeId ->
                        navController.navigate("store/$storeId/settings/staff_&_security/create_staff")
                    }
                },
                type = ButtonType.Primary
            )

            Spacer(Modifier.height(10.dp))
        }
    }

}

@HiltViewModel
class StaffSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val staffRepository: StaffRepository
) : ViewModel() {

    val storeId: Long? = savedStateHandle.get<Long>("storeId")

    val staffs: StateFlow<List<StaffWithProfile>> = (storeId?.let { staffRepository.getStaffs(it) } ?: flowOf(emptyList()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(staff: StaffEntity) {
        viewModelScope.launch { staffRepository.deleteStaff(staff) }
    }
}
