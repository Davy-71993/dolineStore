package com.example.doline.views.screens.store.home.settings


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.data.StaffSessionManager
import com.example.doline.data.Store
import com.example.doline.data.StoreRepository
import com.example.doline.data.models.SettingType
import com.example.doline.data.models.settings
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ErrorMessage
import com.example.doline.views.components.ImageView
import com.example.doline.views.components.LoadingScreen
import com.example.doline.views.components.Screen
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController,  viewModel: ScreenViewModel){

    val uiState by viewModel.uiState.collectAsState()
    when(val state = uiState){
        is UiState.Loading -> {
            LoadingScreen()
        }
        is UiState.Error -> {
            ErrorMessage(state.message)
        }
        is UiState.Success -> {
            val store = state.store ?: return
            Screen (
                topAppBar = {
                    TopAppBar(
                        title = { AppText("Settings - ${store.name}", variant = TextType.Heading, maxLines = 1) },
                        colors = TopAppBarColors(
                            containerColor = colorScheme.background,
                            scrolledContainerColor = colorScheme.background,
                            navigationIconContentColor = colorScheme.onBackground,
                            titleContentColor = colorScheme.onBackground,
                            actionIconContentColor = colorScheme.onBackground,
                            subtitleContentColor = colorScheme.onBackground,
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
                                    tint = colorScheme.onBackground
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { }) {
                                Icon(
                                    painter = painterResource(R.drawable.search),
                                    contentDescription = "Search in settings",
                                    tint = colorScheme.onBackground,
                                    modifier = Modifier.size(IconSize.BIG)
                                )
                            }
                        },
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                    ) {
                        ImageView(store.image, modifier = Modifier.fillMaxWidth())
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Spacer(Modifier.weight(1f))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Brush.linearGradient(
                                        listOf(Color.Transparent, colorScheme.background.copy(.4f), colorScheme.background.copy(.9f), colorScheme.background.copy(.9f), colorScheme.background.copy(.9f), colorScheme.background),
                                        start = Offset(0f,0f),
                                        end = Offset(0f, Float.POSITIVE_INFINITY)
                                    ))
                                    .padding(Spacing.MD),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ){
                                AppText(store.name, variant = TextType.Label, maxLines = 1)
                                AppText(store.description, textAlign = TextAlign.Center, variant = TextType.Small, color = colorScheme.onBackground.copy(.7f), maxLines = 2)
                            }
                        }
                    }
                    Column(
                        Modifier.padding(Spacing.MD),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD)
                    ) {
                        val visibleSettings = if (viewModel.isAdmin) settings else settings.filterNot {
                            it.route == "staff_&_security"
                        }
                        visibleSettings.forEach { item ->
                            SettingItem(
                                item = item,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Rounding.FULL))
                                    .clickable{
                                        navController.navigate("store/${store.id}/settings/${item.route}")
                                    }
                            )
                        }
                    }
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }


}

@Composable
fun SettingItem(item: SettingType, modifier: Modifier = Modifier){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.XXS, vertical = Spacing.XXS),
        horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = item.name,
            tint = colorScheme.onBackground,
            modifier = Modifier.size(IconSize.NORMAL)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            AppText(item.name, variant = TextType.LabelSmall)
            AppText(item.description, variant = TextType.Small)
        }
    }
}

@HiltViewModel
class ScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private  val storeRepository: StoreRepository,
    sessionManager: StaffSessionManager
): ViewModel(){
    val storeId = savedStateHandle.get<Long>("storeId")
    val isAdmin: Boolean = sessionManager.session.value?.staff?.staff?.isAdmin == true
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        getStore()
    }

    fun getStore(){
        viewModelScope.launch {
            if(storeId == null){
                _uiState.value = UiState.Error("The Store ID is null")
                return@launch
            }
            try {
                storeRepository.getStoreById(storeId).collect {
                    _uiState.value = UiState.Success(it)
                }
            }catch (e: Exception){
                _uiState.value = UiState.Error(e.message ?: "Unknown error!")
            }
        }
    }
}

sealed class UiState{
    data object Loading: UiState()
    data class Success(val store: Store?): UiState()
    data class Error(val message: String): UiState()
}