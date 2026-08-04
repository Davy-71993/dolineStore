package com.example.doline.views.screens.welcome

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.doline.data.AuthRepository
import com.example.doline.data.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashScreenViewModel = hiltViewModel()
) {
    val splashState by viewModel.splashState.collectAsStateWithLifecycle()

    LaunchedEffect(splashState) {
        when (splashState) {
            is SplashState.HasProfile -> {
                navController.navigate("stores") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is SplashState.AuthenticatedNoProfile -> {
                navController.navigate("initialization") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is SplashState.NotAuthenticated -> {
                navController.navigate("welcome") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            SplashState.Loading -> {
                // Still loading, do nothing
            }
        }
    }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    authRepository: AuthRepository,
    profileRepository: UserProfileRepository
) : ViewModel() {
    val splashState: StateFlow<SplashState> = combine(
        authRepository.sessionStatus,
        profileRepository.getProfile()
    ) { sessionStatus, profile ->
        when {
            profile != null -> SplashState.HasProfile
            sessionStatus is SessionStatus.Initializing -> SplashState.Loading
            sessionStatus is SessionStatus.Authenticated -> SplashState.AuthenticatedNoProfile
            else -> SplashState.NotAuthenticated
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SplashState.Loading
    )
}

sealed class SplashState {
    data object Loading : SplashState()
    data object HasProfile : SplashState()
    data object AuthenticatedNoProfile : SplashState()
    data object NotAuthenticated : SplashState()
}