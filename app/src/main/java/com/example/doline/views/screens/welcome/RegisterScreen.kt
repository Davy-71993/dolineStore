package com.example.doline.views.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.doline.R
import com.example.doline.data.AuthRepository
import com.example.doline.data.UserProfile
import com.example.doline.data.UserProfileRepository
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.IconSide
import com.example.doline.views.components.SocialButton
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.FormScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: RegisterScreenViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var fullNames by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val googleError by viewModel.googleError.collectAsState()
    val registerState by viewModel.registerState.collectAsState()
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate("initialization") {
                popUpTo("initialization") { inclusive = true }
            }
        }
    }

    // Google sign-in has no callback for "the user closed the browser without finishing" - the
    // best signal available is the app resuming without a session having come through.
    val currentOnResume = rememberUpdatedState { viewModel.onResumed() }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) currentOnResume.value()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    FormScreen(
        appBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    AppText(
                        "Register",
                        color = MaterialTheme.colorScheme.onBackground,
                        variant = TextType.Heading
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("welcome") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        AppText("Welcome, Create your account")

        // Social Login Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.SM)
        ) {
            SocialButton(
                text = "GOOGLE",
                onClick = { viewModel.signInWithGoogle() },
                modifier = Modifier.weight(1f),
                icon = R.drawable.google_favicon_2025
            )
            SocialButton(
                text = "APPLE",
                onClick = {},
                modifier = Modifier.weight(1f),
                icon = R.drawable.apple_logo_black
            )
        }

        if (googleError != null) {
            Spacer(modifier = Modifier.height(Spacing.MD))
            AppText(
                googleError ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(Spacing.MD)
            )
        }

        // Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(.6f)
            )
            AppText(
                text = " OR  CONTINUE  WITH ",
                modifier = Modifier.padding(horizontal = Spacing.SM),
                variant = TextType.Small,
                color = MaterialTheme.colorScheme.onBackground.copy(.8f)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(.6f)
            )
        }

        // Email field
        TextInputField(
            value = email,
            onValueChange = { email = it },
            label = "Email address",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.mail),
                    contentDescription = "Email",
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            },
            placeHolder = "Your email address",
        )

        // Username field
        TextInputField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.user),
                    contentDescription = "Username",
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            },
            placeHolder = "Your username",
        )

        // Username field
        TextInputField(
            value = fullNames,
            onValueChange = { fullNames = it },
            label = "Full names",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.user_pen),
                    contentDescription = "Full names",
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            },
            placeHolder = "Your full names",
        )

        // Password field
        TextInputField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.lock),
                    contentDescription = "Password",
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        painter = painterResource(if (isPasswordVisible) R.drawable.eye_off else R.drawable.eye),
                        contentDescription = "Toggle password visibility",
                        modifier = Modifier.size(IconSize.NORMAL)
                    )
                }
            },
            isPassword = !isPasswordVisible,
            placeHolder = "Your password",
        )

        // Confirm password field
        TextInputField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm password",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.lock),
                    contentDescription = "Confirm password",
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            },
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(if (isConfirmPasswordVisible) R.drawable.eye_off else R.drawable.eye),
                        contentDescription = "Toggle confirm password visibility",
                        modifier = Modifier.size(IconSize.NORMAL)
                    )
                }
            },
            isPassword = !isConfirmPasswordVisible,
            placeHolder = "Confirm your password",
        )

        when (val state = registerState) {
            is RegisterUiState.Error -> {
                AppText(
                    state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(Spacing.MD)
                )
                Spacer(modifier = Modifier.height(Spacing.MD))
            }
            RegisterUiState.PendingConfirmation -> {
                AppText(
                    "We sent a confirmation link to $email. Verify it, then log in.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(Spacing.MD)
                )
                AppText(
                    text = "Didn't get it? Resend email",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = Spacing.XS)
                        .clickable { viewModel.resendConfirmationEmail(email) }
                )
                Spacer(modifier = Modifier.height(Spacing.MD))
            }
            else -> {}
        }

        // Register Button with Gradient
        AppButton(
            "REGISTER",
            onClick = {
                viewModel.register(
                    email = email,
                    username = username,
                    fullNames = fullNames,
                    password = password,
                    confirmPassword = confirmPassword
                )
            },
            iconSize = IconSize.NORMAL,
            iconSide = IconSide.Right,
            icon = R.drawable.arrow_right,
            iconTint = MaterialTheme.colorScheme.onPrimary,
            textVariant = TextType.Label,
            type = ButtonType.Primary,
            isLoading = registerState is RegisterUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
        )

        // Login Link
        Row(
            modifier = Modifier.padding(top = Spacing.MD),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.XXS)
        ) {
            AppText(
                text = "Already have an account? ",
            )
            AppText(
                text = "Login",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { navController.navigate("login") }
            )
        }

        // Footer
        Row(
            modifier = Modifier.padding(vertical = Spacing.LG),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.XXS)
        ) {
            AppText(
                text = "© 2026 DOLINE POS. ALL RIGHTS RESERVED.",
                variant = TextType.Small
            )
        }
    }
}

@HiltViewModel
class RegisterScreenViewModel @Inject constructor(
    private val authenticationRepository: AuthRepository,
    private val profileRepository: UserProfileRepository
) : ViewModel() {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    private val _googleError = MutableStateFlow<String?>(null)
    val googleError: StateFlow<String?> = _googleError.asStateFlow()
    private val _registerState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    // True from the moment the Google browser is launched until either a session comes through
    // or onResumed() decides the user abandoned it.
    private var googleSignInInProgress = false

    init {
        // Google sign-in completes asynchronously via MainActivity's deep-link handling, not
        // as a result of signInWithGoogle() itself - react to the session going Authenticated
        // instead of a return value.
        viewModelScope.launch {
            authenticationRepository.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    googleSignInInProgress = false
                    _isAuthenticated.value = true
                }
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _googleError.value = null
            googleSignInInProgress = true
            val launched = authenticationRepository.signInWithGoogle()
            if (!launched) {
                googleSignInInProgress = false
                _googleError.value = "Couldn't open Google sign-in. Please try again."
            }
        }
    }

    // Called when the app comes back to the foreground. If a Google sign-in was in flight and
    // no session has shown up shortly after resuming, the user most likely closed the browser
    // without finishing - there's no direct cancel callback for that flow, so this is the best
    // available signal.
    fun onResumed() {
        if (!googleSignInInProgress) return
        viewModelScope.launch {
            delay(1500)
            if (googleSignInInProgress && authenticationRepository.currentSession == null) {
                googleSignInInProgress = false
                _googleError.value = "Google sign-in was cancelled."
            }
        }
    }

    fun register(email: String, username: String, fullNames: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            if (email.isBlank() || username.isBlank() || fullNames.isBlank() || password.isBlank()) {
                _registerState.value = RegisterUiState.Error("All fields are required.")
                return@launch
            }
            if (password != confirmPassword) {
                _registerState.value = RegisterUiState.Error("Passwords do not match.")
                return@launch
            }
            if (password.length < 6) {
                _registerState.value = RegisterUiState.Error("Password must be at least 6 characters.")
                return@launch
            }

            _registerState.value = RegisterUiState.Loading
            val userId = authenticationRepository.register(email, password)
            if (userId == null) {
                _registerState.value = RegisterUiState.Error("Registration failed. The email may already be in use.")
                return@launch
            }

            // Created locally regardless of whether a session exists yet - it queues to push to
            // Supabase once one does (immediately if email confirmation is off, or after the
            // user confirms and logs in otherwise).
            profileRepository.insertProfile(
                UserProfile(
                    userId = userId,
                    email = email,
                    username = username,
                    fullNames = fullNames
                )
            )

            _registerState.value = if (authenticationRepository.isUserAuthenticated()) {
                RegisterUiState.Idle
            } else {
                RegisterUiState.PendingConfirmation
            }
        }
    }

    fun resendConfirmationEmail(email: String) {
        viewModelScope.launch {
            authenticationRepository.resendVerificationEmail(email)
        }
    }
}

sealed class RegisterUiState {
    data object Idle : RegisterUiState()
    data object Loading : RegisterUiState()
    data object PendingConfirmation : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
