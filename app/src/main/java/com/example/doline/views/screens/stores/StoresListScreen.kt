package com.example.doline.views.screens.stores

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.doline.DeviceConfiguration
import com.example.doline.R
import com.example.doline.data.AuthRepository
import com.example.doline.data.Store
import com.example.doline.data.StoreRepository
import com.example.doline.data.UserProfile
import com.example.doline.data.UserProfileRepository
import com.example.doline.views.components.StoreCard
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoresListScreen(navController: NavHostController, viewModel: StoreListViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val uiState by viewModel.screenState.collectAsState()

    // Stores is the app's home screen once signed in - whatever auth screens (login/register/
    // initialization) are still sitting underneath in the back stack, back here should exit
    // the app rather than surface them again.
    val activity = LocalContext.current as? Activity
    BackHandler(enabled = activity != null) {
        activity?.finish()
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfig = DeviceConfiguration.getWindowSizeClass(windowSizeClass)
    val columns = DeviceConfiguration.getGridColumnCount(deviceConfig)
    val paddingValues = when(deviceConfig){
        DeviceConfiguration.MOBILE_LANDSCAPE -> Spacing.MD
        else -> 0.dp
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                ),
                title = { AppText("DolineStore", color = colorScheme.onBackground, variant = TextType.Heading) },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.shadow(10.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("stores/create-store"){
                    popUpTo("stores")
                } },
                containerColor = colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    8.dp,
                    6.dp
                ),
                shape = RoundedCornerShape(Rounding.FULL)
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = "Add new item",
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(IconSize.BIG)
                )
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { padding ->
        when(val state = uiState){
            is StoresListScreenUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(colorScheme.background)
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .padding(paddingValues),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.stores.size) { index ->
                        StoreCard(state.stores[index], Modifier, navController)
                    }
                }
            }
            is StoresListScreenUiState.Error -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    AppText(
                        state.message,
                        color = colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shapes.medium)
                            .background(colorScheme.errorContainer)
                            .padding(Spacing.MD)
                    )
                }
            }
            else -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator()
                }
            }
        }
    }
}


@HiltViewModel
class StoreListViewModel @Inject constructor(
    storeRepository: StoreRepository,
    profileRepository: UserProfileRepository,
    authRepository: AuthRepository
) : ViewModel() {
    val screenState: StateFlow<StoresListScreenUiState> = combine(
        storeRepository.getAllStores(),
        authRepository.currentUser?.id?.let { profileRepository.getProfileByUserId(it) } ?: flowOf(null)
    ) { stores, profile ->
        if(profile == null){
            StoresListScreenUiState.Error("Error: The profile is null")

        }else{
            StoresListScreenUiState.Success(profile, stores)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StoresListScreenUiState.Loading
    )
}


sealed class StoresListScreenUiState {
    data object Loading: StoresListScreenUiState()
    data class Success(val profile: UserProfile, val stores: List<Store>): StoresListScreenUiState()
    data class Error(val message: String): StoresListScreenUiState()
}
