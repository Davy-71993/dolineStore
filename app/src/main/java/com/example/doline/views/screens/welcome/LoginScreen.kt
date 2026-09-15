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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.doline.R
import com.example.doline.data.AuthRepository
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppButton
import com.example.doline.views.components.AppText
import com.example.doline.views.components.ButtonType
import com.example.doline.views.components.FormScreen
import com.example.doline.views.components.IconSide
import com.example.doline.views.components.SocialButton
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController, viewModel: SignInViewModel = hiltViewModel()) {
    val email = viewModel.email.collectAsState(initial = "")
    val password = viewModel.password.collectAsState(initial = "")
    val isPasswordVisible = viewModel.isPasswordVisible.collectAsState(initial = false)
    val uiState by viewModel.uiState.collectAsState()

    if(uiState is LoginScreenUiState.Success){
        navController.navigate("initialization"){
            popUpTo("initialization"){ inclusive = true}
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
        appBar ={
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    AppText(
                        "Login",
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
                },
            )
        }
    ) {

        AppText("Welcome back,", textAlign = TextAlign.Center)

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
            value = email.value,
            onValueChange = { viewModel.onEmailChange(it) },
            label = "Email address",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.mail),
                    contentDescription = "Email",
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            },
            isError = uiState is LoginScreenUiState.Error,
            placeHolder = "Your email address",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        // Password field
        TextInputField(
            value = password.value,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = "Password",
            isError = uiState is LoginScreenUiState.Error,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.lock),
                    contentDescription = "Password",
                    modifier = Modifier.size(IconSize.NORMAL)
                )
            },
            trailingIcon = {
                IconButton(onClick = { viewModel.onPasswordVisibilityChanged(!isPasswordVisible.value) }) {
                    Icon(
                        painter = painterResource(if (isPasswordVisible.value) R.drawable.eye_off else R.drawable.eye),
                        contentDescription = "Toggle password visibility",
                        modifier = Modifier.size(IconSize.NORMAL)
                    )
                }
            },
            isPassword = !isPasswordVisible.value,
            placeHolder = "Your password",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { viewModel.signIn() }
            )
        )

        if (uiState is LoginScreenUiState.Error) {
            AppText(
                (uiState as LoginScreenUiState.Error).error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(Spacing.MD)
            )
            Spacer(modifier = Modifier.height(Spacing.MD))
        }


        // Register Button with Gradient
        AppButton(
            "Login",
            onClick = { viewModel.signIn() },
            iconSize = IconSize.NORMAL,
            iconSide = IconSide.Right,
            icon = R.drawable.arrow_right,
            iconTint = MaterialTheme.colorScheme.onPrimary,
            textVariant = TextType.Label,
            type = ButtonType.Primary,
            isLoading = uiState is LoginScreenUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
        )

        // Register Link
        Row(
            modifier = Modifier.padding(top = Spacing.MD),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.XXS)
        ) {
            AppText(text = "Don't have an account? ")
            AppText(
                text = "Register",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { navController.navigate("register") }
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
class SignInViewModel @Inject constructor(
    private val authenticationRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginScreenUiState>(LoginScreenUiState.Idle)
    val uiState: StateFlow<LoginScreenUiState> = _uiState.asStateFlow()
    private val _email = MutableStateFlow("")
    val email: Flow<String> = _email
    private val _password = MutableStateFlow("")
    val password = _password
    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible = _isPasswordVisible

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
                    _uiState.value = LoginScreenUiState.Success
                }
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value = LoginScreenUiState.Loading
            googleSignInInProgress = true
            val launched = authenticationRepository.signInWithGoogle()
            if (!launched) {
                googleSignInInProgress = false
                _uiState.value = LoginScreenUiState.Error("Couldn't open Google sign-in. Please try again.")
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
                _uiState.value = LoginScreenUiState.Error("Google sign-in was cancelled.")
            }
        }
    }

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun onPasswordVisibilityChanged(state: Boolean){
        _isPasswordVisible.value = state
    }

    fun signIn() {
        viewModelScope.launch {
            _uiState.value = LoginScreenUiState.Loading

            if(_email.value.isEmpty() || _password.value.isEmpty()){
                _uiState.value = LoginScreenUiState.Error("Both email and password are required.")
                return@launch
            }

            val res = authenticationRepository.login(
                email = _email.value,
                password = _password.value
            )

            if(res){
                _uiState.value = LoginScreenUiState.Success
            }else{
                _uiState.value = LoginScreenUiState.Error( "Login failed, Invalid credentials.")
            }
        }
    }

}

sealed class LoginScreenUiState {
    data object Idle : LoginScreenUiState()
    data object Loading : LoginScreenUiState()
    data object  Success: LoginScreenUiState()
    data class  Error(val error: String): LoginScreenUiState()
}









