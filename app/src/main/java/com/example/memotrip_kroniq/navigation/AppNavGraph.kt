package com.example.memotrip_kroniq.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.location.LocationSearchRepository
import com.example.memotrip_kroniq.data.network.HttpClientProvider
import com.example.memotrip_kroniq.ui.addtrip.AddTripScreen
import com.example.memotrip_kroniq.ui.addtrip.screens.SavingTripScreen
import com.example.memotrip_kroniq.ui.addtrip.screens.TripSuccessScreen
import com.example.memotrip_kroniq.ui.auth.ForgotPasswordScreen
import com.example.memotrip_kroniq.ui.auth.LoginScreen
import com.example.memotrip_kroniq.ui.auth.SignUpScreen
import com.example.memotrip_kroniq.ui.home.HomeScreen
import com.example.memotrip_kroniq.ui.home.HomeTab
import com.example.memotrip_kroniq.ui.locationsearch.FullScreenLocationSearchScreen
import com.example.memotrip_kroniq.ui.locationsearch.LocationSearchViewModel
import com.example.memotrip_kroniq.ui.settings.SettingsScreen
import com.example.memotrip_kroniq.ui.profile.ProfileScreen
import com.example.memotrip_kroniq.ui.splash.SplashScreen
import com.example.memotrip_kroniq.ui.tripdetail.TripDetailScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import kotlinx.coroutines.launch


sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home?tab={tab}") {
        fun createRoute(tab: HomeTab) = "home?tab=${tab.name}"
    }

    object ForgotPassword : Screen("forgot_password")
    object AddTrip : Screen("add_trip")
    object LocationSearch : Screen("location_search")
    object SavingTrip : Screen("saving_trip")
    object TripSuccess : Screen("trip_success")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object TripDetail : Screen("trip_detail/{tripId}") {
        fun createRoute(tripId: String) = "trip_detail/$tripId"
    }
}

enum class LocationTarget {
    FROM,
    TO,
    STOP
}

const val LOCATION_RESULT_KEY = "location_result"
const val LOCATION_TARGET_KEY = "location_target"
const val LOCATION_NAME_KEY = "location_name"
const val LOCATION_LAT_KEY = "location_lat"
const val LOCATION_LON_KEY = "location_lon"


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(navController: NavHostController) {

    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        // =============================================================
        // ⭐ SPLASH → fade animace
        // =============================================================
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }
        ) {
            val context = LocalContext.current
            val tokenStore = remember { TokenDataStore(context) }
            val token by tokenStore.token.collectAsState(initial = null)

            LaunchedEffect(token) {
                if (token != null) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }

            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // =============================================================
        // ⭐ LOGIN
        // =============================================================
        composable(
            route = Screen.Login.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onCreateAccount = {
                    navController.navigate(Screen.SignUp.route)
                },
                onForgot = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        // =============================================================
        // ⭐ SIGNUP
        // =============================================================
        composable(
            route = Screen.SignUp.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            SignUpScreen(
                onSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        // =============================================================
        // ⭐ FORGOT PASSWORD
        // =============================================================
        composable(
            route = Screen.ForgotPassword.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // =============================================================
        // ⭐ HOME
        // =============================================================
        composable(
            route = Screen.Home.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) { backStackEntry ->

            val tabName = backStackEntry.arguments?.getString("tab")

            val initialTab = HomeTab.values()
                .firstOrNull { it.name == tabName }
                ?: HomeTab.THEMES

            HomeScreen(
                navController = navController,
                initialTab = initialTab
            )
        }


        // =============================================================
        // ⭐ ADD TRIP
        // =============================================================
        composable(Screen.AddTrip.route) {
            AddTripScreen(
                navController = navController
            )
        }

        // =============================================================
        // ⭐ LOCATION SEARCH (FULLSCREEN)
        // =============================================================
        composable(Screen.LocationSearch.route) {

            val vm: LocationSearchViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return LocationSearchViewModel(
                            LocationSearchRepository(HttpClientProvider.client)
                        ) as T
                    }
                }
            )

            FullScreenLocationSearchScreen(
                navController = navController,
                viewModel = vm
            )
        }

        // =============================================================
        // ⭐ SAVING TRIP
        // =============================================================
        composable(Screen.SavingTrip.route) {
            SavingTripScreen()
        }

        // =============================================================
        // ⭐ TRIP SUCCESS
        // =============================================================
        composable(Screen.TripSuccess.route) {
            TripSuccessScreen(
                navController = navController
            )
        }

        // =============================================================
        // ⭐ TRIP DETAIL
        // =============================================================
        composable(
            route = Screen.TripDetail.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId").orEmpty()

            TripDetailScreen(
                navController = navController,
                tripId = tripId
            )
        }

        // =============================================================
        // ⭐ SETTINGS
        // =============================================================
        composable(
            route = Screen.Settings.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            val context = LocalContext.current
            val tokenStore = remember { TokenDataStore(context) }
            val coroutineScope = rememberCoroutineScope()

            SettingsScreen(
                navController = navController,
                onLogoutClick = {
                    coroutineScope.launch {
                        tokenStore.clearTokens()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // =============================================================
        // ⭐ PROFILE
        // =============================================================
        composable(
            route = Screen.Profile.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            ProfileScreen(
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultEnter(
    initial: NavBackStackEntry,
    target: NavBackStackEntry
): EnterTransition {

    val from = initial.destination.route
    val to = target.destination.route

    // Návrat zpět → slide zleva doprava
    return if (
        (from == Screen.SignUp.route && to == Screen.Login.route) ||
        (from == Screen.Home.route && to == Screen.Login.route)
    ) {
        slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300)
        )
    } else {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultExit(
    initial: NavBackStackEntry,
    target: NavBackStackEntry
): ExitTransition {

    val from = initial.destination.route
    val to = target.destination.route

    return if (from == Screen.SignUp.route && to == Screen.Login.route) {
        slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(300)
        )
    } else {
        slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300)
        )
    }
}

