package com.example.memotrip_kroniq.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.location.LocationSearchRepository
import com.example.memotrip_kroniq.data.network.HttpClientProvider
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.ui.addtrip.AddTripScreen
import com.example.memotrip_kroniq.ui.addtrip.screens.SavingTripScreen
import com.example.memotrip_kroniq.ui.addtrip.screens.TripSuccessScreen
import com.example.memotrip_kroniq.ui.auth.ForgotPasswordScreen
import com.example.memotrip_kroniq.ui.auth.LoginScreen
import com.example.memotrip_kroniq.ui.auth.SignUpScreen
import com.example.memotrip_kroniq.ui.edittrip.EditTripScreen
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
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home?tab={tab}") {
        fun createRoute(tab: HomeTab) = "home?tab=${tab.name}"
    }

    object ForgotPassword : Screen("forgot_password")
    object AddTrip : Screen("add_trip")
    object EditTrip : Screen("edit_trip/{tripId}") {
        fun createRoute(tripId: String) = "edit_trip/$tripId"
    }
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

        composable(
            route = Screen.EditTrip.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId").orEmpty()

            EditTripScreen(
                navController = navController,
                tripId = tripId
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
            val context = LocalContext.current
            val tokenStore = remember { TokenDataStore(context) }
            val authRepository = remember {
                AuthRepository(
                    api = RetrofitClient.authApi,
                    tokenStore = tokenStore
                )
            }
            val coroutineScope = rememberCoroutineScope()
            var initialName by remember { mutableStateOf("") }
            var initialEmail by remember { mutableStateOf("") }
            var initialAccountType by remember { mutableStateOf("Free") }
            var initialGender by remember { mutableStateOf("") }
            var initialFirstName by remember { mutableStateOf("") }
            var initialLastName by remember { mutableStateOf("") }
            var initialDateOfBirth by remember { mutableStateOf("") }
            var initialProfileImageUrl by remember { mutableStateOf("") }
            var isProfileLoading by remember { mutableStateOf(true) }

            LaunchedEffect(authRepository) {
                try {
                    val me = authRepository.getMe()
                    initialName = me.name.orEmpty()
                    initialEmail = me.email
                    initialGender = me.gender.orEmpty()
                    initialFirstName = me.firstName.orEmpty()
                    initialLastName = me.lastName.orEmpty()
                    initialDateOfBirth = me.dateOfBirth.orEmpty()
                    initialProfileImageUrl = me.profileImageUrl.orEmpty()
                    initialAccountType = when {
                        me.isKroniq -> "KroniQ"
                        me.isPremium -> "Premium"
                        else -> "Free"
                    }
                } catch (e: Exception) {
                    Log.e("PROFILE_LOAD", "Profile preload failed", e)
                } finally {
                    isProfileLoading = false
                }
            }

            Crossfade(
                targetState = isProfileLoading,
                animationSpec = tween(durationMillis = 220),
                label = "profile_loading_transition"
            ) { loading ->
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    ProfileScreen(
                        navController = navController,
                        initialName = initialName,
                        initialAccountType = initialAccountType,
                        initialEmail = initialEmail,
                        initialGender = initialGender,
                        initialFirstName = initialFirstName,
                        initialLastName = initialLastName,
                        initialDateOfBirth = initialDateOfBirth,
                        initialProfileImageUrl = initialProfileImageUrl,
                        onSaveClick = { photoUri, isPhotoRemoved, name, accountType, kroniqRole, gender, firstName, lastName, email, dateOfBirth ->
                            coroutineScope.launch {
                                try {
                                    val apiDateOfBirth = formatProfileDateForApi(dateOfBirth)
                                    val request = linkedMapOf<String, String>().apply {
                                        name.takeIf { it != initialName }?.let { put("name", it) }
                                        gender.takeIf { it != initialGender }?.let { put("gender", it) }
                                        firstName.takeIf { it != initialFirstName }?.let { put("firstName", it) }
                                        lastName.takeIf { it != initialLastName }?.let { put("lastName", it) }
                                        apiDateOfBirth.takeIf { it != initialDateOfBirth }?.let {
                                            put("dateOfBirth", it)
                                        }
                                    }

                                    val hasPhotoUpload = photoUri != null
                                    val hasPhotoDelete = isPhotoRemoved && initialProfileImageUrl.isNotBlank()

                                    if (!hasPhotoUpload && !hasPhotoDelete && request.isEmpty()) {
                                        navController.navigate(Screen.Home.createRoute(HomeTab.TRIP_HISTORY)) {
                                            popUpTo(Screen.Profile.route) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                        return@launch
                                    }

                                    if (hasPhotoUpload) {
                                        authRepository.uploadProfilePhoto(
                                            contentResolver = context.contentResolver,
                                            uri = photoUri
                                        )
                                    } else if (hasPhotoDelete) {
                                        authRepository.deleteProfilePhoto()
                                    }

                                    val updatedUser = if (request.isNotEmpty()) {
                                        authRepository.updateMe(request)
                                    } else {
                                        authRepository.getMe()
                                    }

                                    Log.d(
                                        "PROFILE_SAVE",
                                        "Profile updated successfully for userId=${updatedUser.id}, photoUri=$photoUri, accountType=$accountType, kroniqRole=$kroniqRole, email=$email"
                                    )

                                    navController.navigate(Screen.Home.createRoute(HomeTab.TRIP_HISTORY)) {
                                        popUpTo(Screen.Profile.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } catch (e: Exception) {
                                    Log.e("PROFILE_SAVE", "Profile update failed", e)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatProfileDateForApi(dateOfBirth: String): String? {
    if (dateOfBirth.isBlank()) return null

    return runCatching {
        LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrElse {
        dateOfBirth
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

