package com.example.doline

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.doline.data.SyncScheduler
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.doline.views.screens.welcome.LoginScreen
import com.example.doline.views.screens.stores.StoresListScreen
import com.example.doline.views.screens.welcome.RegisterScreen
import com.example.doline.views.screens.welcome.WelcomeScreen
import com.example.doline.ui.theme.AppTheme
import com.example.doline.ui.theme.backgroundLight
import com.example.doline.views.components.AppText
import com.example.doline.views.components.Screen
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import com.example.doline.views.screens.ErrorScreen
import com.example.doline.views.screens.store.StoreMain
import com.example.doline.views.screens.stores.CreateStoreScreen
import com.example.doline.views.screens.welcome.InitializationScreen
import com.example.doline.views.screens.welcome.SplashScreen


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                scrim = backgroundLight.toArgb(),
                darkScrim = backgroundLight.toArgb()
            )
        )
        supabaseClient.handleDeeplinks(intent)
        setContent {
            AppTheme {
                AppNavigation()
            }
        }
    }

    // MainActivity is singleTask, so the OAuth redirect (doline://auth) arrives here instead
    // of creating a new instance - handleDeeplinks exchanges the code and updates Auth's
    // sessionStatus, which screens observing it (e.g. SplashScreen, SignInViewModel) react to.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        supabaseClient.handleDeeplinks(intent)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // Welcome screens
        composable("splash") {
            SplashScreen(navController)
        }
        composable("welcome") {
            WelcomeScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable("initialization") {
            InitializationScreen(navController)
        }
        composable("error") {
            ErrorScreen(navController)
        }

        // Stores' links
        composable("stores") {
            StoresListScreen(navController)
        }
        composable("stores/create-store") {
            CreateStoreScreen(navController)
        }

        // Entry point to any store’s tabbed app (dynamic route)
        composable(
            route = "stores/{storeId}",
            listOf(navArgument("storeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getLong("storeId")
            StoreMain(storeId)
        }
    }

}

@HiltAndroidApp
class Doline : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        SyncScheduler.schedulePeriodic(this)
    }
}

@Composable
fun SampleScree(){
    Screen(
        topAppBar = {}
    ) {
        AppText("Sample Screen")
    }
}



