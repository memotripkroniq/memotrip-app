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
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.location.LocationSearchRepository
import com.example.memotrip_kroniq.data.network.HttpClientProvider
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.ui.addtrip.AddTripScreen
import com.example.memotrip_kroniq.ui.addtrip.screens.SavingTripScreen
import com.example.memotrip_kroniq.ui.addtrip.screens.TripSuccessContent
import com.example.memotrip_kroniq.ui.addtrip.screens.TripSuccessScreen
import com.example.memotrip_kroniq.ui.auth.ForgotPasswordScreen
import com.example.memotrip_kroniq.ui.auth.LoginScreen
import com.example.memotrip_kroniq.ui.auth.SignUpScreen
import com.example.memotrip_kroniq.ui.changepassword.ChangePasswordScreen
import com.example.memotrip_kroniq.ui.changepassword.ChangePasswordSavingScreen
import com.example.memotrip_kroniq.ui.changepassword.ChangePasswordSuccessScreen
import com.example.memotrip_kroniq.ui.edittrip.EditTripScreen
import com.example.memotrip_kroniq.ui.home.HomeScreen
import com.example.memotrip_kroniq.ui.home.HomeTab
import com.example.memotrip_kroniq.ui.kroniq.AddKroniqMemberScreen
import com.example.memotrip_kroniq.ui.settings.DeleteAccountScreen
import com.example.memotrip_kroniq.ui.settings.AiFeaturesNoticeScreen
import com.example.memotrip_kroniq.ui.kroniq.KroniqScreen
import com.example.memotrip_kroniq.ui.locationsearch.FullScreenLocationSearchScreen
import com.example.memotrip_kroniq.ui.locationsearch.LocationSearchViewModel
import com.example.memotrip_kroniq.ui.settings.LegalPoliciesScreen
import com.example.memotrip_kroniq.ui.settings.OpenSourceLicensesScreen
import com.example.memotrip_kroniq.ui.settings.PaymentsSubscriptionsScreen
import com.example.memotrip_kroniq.ui.settings.PrivacyPolicyScreen
import com.example.memotrip_kroniq.ui.settings.RateUsScreen
import com.example.memotrip_kroniq.ui.settings.SettingsScreen
import com.example.memotrip_kroniq.ui.settings.TermsOfServiceScreen
import com.example.memotrip_kroniq.ui.settings.LanguageScreen
import com.example.memotrip_kroniq.ui.profile.ProfileScreen
import com.example.memotrip_kroniq.ui.splash.SplashScreen
import com.example.memotrip_kroniq.ui.tripdetail.TripDetailScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home?tab={tab}") {
        fun createRoute(tab: HomeTab) = "home?tab=${tab.name}"
    }

    object ForgotPassword : Screen("forgot_password")
    object ChangePassword : Screen("change_password")
    object ChangePasswordSaving : Screen("change_password_saving")
    object ChangePasswordSuccess : Screen("change_password_success")
    object AddTrip : Screen("add_trip")
    object EditTrip : Screen("edit_trip/{tripId}") {
        fun createRoute(tripId: String) = "edit_trip/$tripId"
    }
    object LocationSearch : Screen("location_search")
    object SavingTrip : Screen("saving_trip")
    object TripSuccess : Screen("trip_success")
    object Settings : Screen("settings")
    object LegalPolicies : Screen("legal_policies")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfService : Screen("terms_of_service")
    object PaymentsSubscriptions : Screen("payments_subscriptions")
    object AiFeaturesNotice : Screen("ai_features_notice")
    object OpenSourceLicenses : Screen("open_source_licenses")
    object DeleteAccount : Screen("delete_account")
    object RateUs : Screen("rate_us")
    object KroniQ : Screen("kroniq")
    object KroniQAddMember : Screen("kroniq_add_member")
    object KroniQAddMemberSaving : Screen("kroniq_add_member_saving")
    object KroniQAddMemberSuccess : Screen("kroniq_add_member_success")
    object KroniQAddGuest : Screen("kroniq_add_guest")
    object KroniQAddGuestSaving : Screen("kroniq_add_guest_saving")
    object KroniQAddGuestSuccess : Screen("kroniq_add_guest_success")
    object Language : Screen("language")
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
private const val CHANGE_PASSWORD_CURRENT_KEY = "change_password_current"
private const val CHANGE_PASSWORD_NEW_KEY = "change_password_new"
private const val CHANGE_PASSWORD_ERROR_KEY = "change_password_error"
private const val KRONIQ_ADD_MEMBER_EMAIL_KEY = "kroniq_add_member_email"
private const val KRONIQ_ADD_MEMBER_ERROR_KEY = "kroniq_add_member_error"
private const val KRONIQ_RELOAD_TOKEN_KEY = "kroniq_reload_token"
private const val KRONIQ_ADD_MEMBER_IN_FLIGHT_KEY = "kroniq_add_member_in_flight"
private const val KRONIQ_ADD_GUEST_EMAIL_KEY = "kroniq_add_guest_email"
private const val KRONIQ_ADD_GUEST_ERROR_KEY = "kroniq_add_guest_error"
private const val KRONIQ_ADD_GUEST_IN_FLIGHT_KEY = "kroniq_add_guest_in_flight"


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

        composable(
            route = Screen.ChangePassword.route,
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
            var hasPassword by remember { mutableStateOf<Boolean?>(null) }
            val errorMessage = navController.currentBackStackEntry
                ?.savedStateHandle
                ?.get<String?>(CHANGE_PASSWORD_ERROR_KEY)

            LaunchedEffect(authRepository) {
                hasPassword = runCatching { authRepository.getMe().hasPassword }
                    .getOrDefault(true)
            }

            Crossfade(
                targetState = hasPassword,
                animationSpec = tween(durationMillis = 220),
                label = "change_password_loading_transition"
            ) { hasPasswordValue ->
                if (hasPasswordValue == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    ChangePasswordScreen(
                        hasPassword = hasPasswordValue,
                        backendErrorMessage = errorMessage,
                        onBackendErrorConsumed = {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.remove<String?>(CHANGE_PASSWORD_ERROR_KEY)
                        },
                        onBack = { navController.popBackStack() },
                        onContinue = { currentPassword, newPassword ->
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                CHANGE_PASSWORD_CURRENT_KEY,
                                currentPassword
                            )
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                CHANGE_PASSWORD_NEW_KEY,
                                newPassword
                            )
                            navController.navigate(Screen.ChangePasswordSaving.route)
                        }
                    )
                }
            }
        }

        composable(Screen.ChangePasswordSaving.route) {
            val context = LocalContext.current
            val tokenStore = remember { TokenDataStore(context) }
            val authRepository = remember {
                AuthRepository(
                    api = RetrofitClient.authApi,
                    tokenStore = tokenStore
                )
            }
            val requestEntry = remember(navController) {
                navController.getBackStackEntry(Screen.ChangePassword.route)
            }
            val currentPassword = requestEntry.savedStateHandle.get<String?>(CHANGE_PASSWORD_CURRENT_KEY)
            val newPassword = requestEntry.savedStateHandle.get<String>(CHANGE_PASSWORD_NEW_KEY).orEmpty()

            ChangePasswordSavingScreen(
                currentPassword = currentPassword,
                newPassword = newPassword,
                changePassword = { current, fresh ->
                    authRepository.changePassword(current, fresh)
                },
                onFinished = {
                    requestEntry.savedStateHandle.remove<String?>(CHANGE_PASSWORD_CURRENT_KEY)
                    requestEntry.savedStateHandle.remove<String>(CHANGE_PASSWORD_NEW_KEY)
                    requestEntry.savedStateHandle.remove<String?>(CHANGE_PASSWORD_ERROR_KEY)
                    navController.navigate(Screen.ChangePasswordSuccess.route) {
                        popUpTo(Screen.ChangePasswordSaving.route) { inclusive = true }
                    }
                },
                onFailed = { message ->
                    requestEntry.savedStateHandle.remove<String?>(CHANGE_PASSWORD_CURRENT_KEY)
                    requestEntry.savedStateHandle.remove<String>(CHANGE_PASSWORD_NEW_KEY)
                    requestEntry.savedStateHandle.set(CHANGE_PASSWORD_ERROR_KEY, message)
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ChangePasswordSuccess.route) {
            val context = LocalContext.current
            val tokenStore = remember { TokenDataStore(context) }
            val coroutineScope = rememberCoroutineScope()

            ChangePasswordSuccessScreen(
                onFinished = {
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

        composable(
            route = Screen.LegalPolicies.route,
            enterTransition = { fadeIn(animationSpec = tween(220)) },
            exitTransition = { fadeOut(animationSpec = tween(180)) }
        ) {
            LegalPoliciesScreen(navController = navController)
        }

        composable(
            route = Screen.PrivacyPolicy.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            PrivacyPolicyScreen(navController = navController)
        }

        composable(
            route = Screen.TermsOfService.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            TermsOfServiceScreen(navController = navController)
        }

        composable(
            route = Screen.PaymentsSubscriptions.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            PaymentsSubscriptionsScreen(navController = navController)
        }

        composable(
            route = Screen.AiFeaturesNotice.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            AiFeaturesNoticeScreen(navController = navController)
        }

        composable(
            route = Screen.OpenSourceLicenses.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            OpenSourceLicensesScreen(navController = navController)
        }

        composable(
            route = Screen.DeleteAccount.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            DeleteAccountScreen(navController = navController)
        }

        composable(
            route = Screen.RateUs.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            RateUsScreen(navController = navController)
        }

        composable(
            route = Screen.KroniQ.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            val reloadToken by navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getStateFlow(KRONIQ_RELOAD_TOKEN_KEY, 0)
                ?.collectAsState()
                ?: remember { mutableStateOf(0) }
            KroniqScreen(
                onBack = { navController.popBackStack() },
                reloadToken = reloadToken,
                onAddMemberClick = {
                    navController.navigate(Screen.KroniQAddMember.route)
                },
                onAddGuestClick = {
                    navController.navigate(Screen.KroniQAddGuest.route)
                }
            )
        }

        composable(Screen.KroniQAddMember.route) {
            val context = LocalContext.current
            val tokenStore = remember { TokenDataStore(context) }
            val authRepository = remember {
                AuthRepository(
                    api = RetrofitClient.authApi,
                    tokenStore = tokenStore
                )
            }
            val errorMessage by navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getStateFlow<String?>(KRONIQ_ADD_MEMBER_ERROR_KEY, null)
                ?.collectAsState()
                ?: remember { mutableStateOf(null) }

            AddKroniqMemberScreen(
                backendErrorMessage = errorMessage,
                onBackendErrorConsumed = {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String?>(KRONIQ_ADD_MEMBER_ERROR_KEY)
                },
                onBack = { navController.popBackStack() },
                onAddMember = { email ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set(KRONIQ_ADD_MEMBER_EMAIL_KEY, email)
                    navController.navigate(Screen.KroniQAddMemberSaving.route)
                }
            )
        }

        composable(Screen.KroniQAddMemberSaving.route) { savingEntry ->
            val context = LocalContext.current
            val tokenStore = remember { TokenDataStore(context) }
            val authRepository = remember {
                AuthRepository(
                    api = RetrofitClient.authApi,
                    tokenStore = tokenStore
                )
            }
            val requestEntry = remember(navController) {
                navController.getBackStackEntry(Screen.KroniQAddMember.route)
            }
            val email = requestEntry.savedStateHandle.get<String>(KRONIQ_ADD_MEMBER_EMAIL_KEY).orEmpty()
            val inFlight = savingEntry.savedStateHandle.get<Boolean>(KRONIQ_ADD_MEMBER_IN_FLIGHT_KEY) == true

            LaunchedEffect(email) {
                if (email.isBlank() || inFlight) return@LaunchedEffect

                savingEntry.savedStateHandle[KRONIQ_ADD_MEMBER_IN_FLIGHT_KEY] = true

                runCatching {
                    authRepository.addKroniqMember(email)
                }.onSuccess {
                    savingEntry.savedStateHandle.remove<Boolean>(KRONIQ_ADD_MEMBER_IN_FLIGHT_KEY)
                    requestEntry.savedStateHandle.remove<String>(KRONIQ_ADD_MEMBER_EMAIL_KEY)
                    requestEntry.savedStateHandle.remove<String?>(KRONIQ_ADD_MEMBER_ERROR_KEY)
                    navController.getBackStackEntry(Screen.KroniQ.route)
                        .savedStateHandle[KRONIQ_RELOAD_TOKEN_KEY] =
                        (navController.getBackStackEntry(Screen.KroniQ.route)
                            .savedStateHandle[KRONIQ_RELOAD_TOKEN_KEY] ?: 0) + 1
                    navController.navigate(Screen.KroniQAddMemberSuccess.route) {
                        popUpTo(Screen.KroniQAddMemberSaving.route) { inclusive = true }
                    }
                }.onFailure { error ->
                    savingEntry.savedStateHandle.remove<Boolean>(KRONIQ_ADD_MEMBER_IN_FLIGHT_KEY)
                    requestEntry.savedStateHandle.remove<String>(KRONIQ_ADD_MEMBER_EMAIL_KEY)
                    requestEntry.savedStateHandle[KRONIQ_ADD_MEMBER_ERROR_KEY] =
                        mapKroniqAddMemberError(context, error.message)
                    navController.popBackStack()
                }
            }

            SavingTripScreen(
                message = androidx.compose.ui.res.stringResource(R.string.kroniq_add_member_saving)
            )
        }

        composable(Screen.KroniQAddMemberSuccess.route) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1500)
                navController.navigate(Screen.KroniQ.route) {
                    popUpTo(Screen.KroniQAddMember.route) { inclusive = true }
                    launchSingleTop = true
                }
            }

            TripSuccessContent(
                title = androidx.compose.ui.res.stringResource(R.string.kroniq_add_member_success_title),
                subtitle = androidx.compose.ui.res.stringResource(R.string.kroniq_add_member_success_subtitle),
                footer = androidx.compose.ui.res.stringResource(R.string.kroniq_add_member_success_footer)
            )
        }

        composable(Screen.KroniQAddGuest.route) {
            val errorMessage by navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getStateFlow<String?>(KRONIQ_ADD_GUEST_ERROR_KEY, null)
                ?.collectAsState()
                ?: remember { mutableStateOf(null) }

            AddKroniqMemberScreen(
                backendErrorMessage = errorMessage,
                onBackendErrorConsumed = {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String?>(KRONIQ_ADD_GUEST_ERROR_KEY)
                },
                title = androidx.compose.ui.res.stringResource(R.string.kroniq_add_guest_title),
                buttonText = androidx.compose.ui.res.stringResource(R.string.kroniq_add_guest_button),
                onBack = { navController.popBackStack() },
                onAddMember = { email ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set(KRONIQ_ADD_GUEST_EMAIL_KEY, email)
                    navController.navigate(Screen.KroniQAddGuestSaving.route)
                }
            )
        }

        composable(Screen.KroniQAddGuestSaving.route) { savingEntry ->
            val context = LocalContext.current
            val tokenStore = remember { TokenDataStore(context) }
            val authRepository = remember {
                AuthRepository(
                    api = RetrofitClient.authApi,
                    tokenStore = tokenStore
                )
            }
            val requestEntry = remember(navController) {
                navController.getBackStackEntry(Screen.KroniQAddGuest.route)
            }
            val email = requestEntry.savedStateHandle.get<String>(KRONIQ_ADD_GUEST_EMAIL_KEY).orEmpty()
            val inFlight = savingEntry.savedStateHandle.get<Boolean>(KRONIQ_ADD_GUEST_IN_FLIGHT_KEY) == true

            LaunchedEffect(email) {
                if (email.isBlank() || inFlight) return@LaunchedEffect

                savingEntry.savedStateHandle[KRONIQ_ADD_GUEST_IN_FLIGHT_KEY] = true

                runCatching {
                    val kroniqMe = authRepository.getKroniqMe()
                    val alreadyMember = kroniqMe.members.any {
                        !it.role.equals("GUEST", ignoreCase = true) &&
                            it.email.equals(email, ignoreCase = true)
                    }
                    val alreadyGuest = kroniqMe.members.any {
                        it.role.equals("GUEST", ignoreCase = true) &&
                            it.email.equals(email, ignoreCase = true)
                    }

                    when {
                        alreadyMember -> "ALREADY_MEMBER"
                        alreadyGuest -> "ALREADY_GUEST"
                        else -> authRepository.addKroniqGuest(email)
                    }
                }.onSuccess { result ->
                    savingEntry.savedStateHandle.remove<Boolean>(KRONIQ_ADD_GUEST_IN_FLIGHT_KEY)
                    requestEntry.savedStateHandle.remove<String>(KRONIQ_ADD_GUEST_EMAIL_KEY)
                    requestEntry.savedStateHandle.remove<String?>(KRONIQ_ADD_GUEST_ERROR_KEY)

                    when (result) {
                        is com.example.memotrip_kroniq.data.remote.dto.AddKroniqGuestResponse -> {
                            navController.getBackStackEntry(Screen.KroniQ.route)
                                .savedStateHandle[KRONIQ_RELOAD_TOKEN_KEY] =
                                (navController.getBackStackEntry(Screen.KroniQ.route)
                                    .savedStateHandle[KRONIQ_RELOAD_TOKEN_KEY] ?: 0) + 1
                            navController.navigate(Screen.KroniQAddGuestSuccess.route) {
                                popUpTo(Screen.KroniQAddGuestSaving.route) { inclusive = true }
                            }
                        }

                        "ALREADY_MEMBER" -> {
                            requestEntry.savedStateHandle[KRONIQ_ADD_GUEST_ERROR_KEY] =
                                context.getString(R.string.kroniq_add_member_error_already_member)
                            navController.popBackStack()
                        }

                        "ALREADY_GUEST" -> {
                            requestEntry.savedStateHandle[KRONIQ_ADD_GUEST_ERROR_KEY] =
                                context.getString(R.string.kroniq_add_guest_error_already_guest)
                            navController.popBackStack()
                        }

                        else -> {
                            requestEntry.savedStateHandle[KRONIQ_ADD_GUEST_ERROR_KEY] = when {
                                result.toString().contains("USER_NOT_FOUND", ignoreCase = true) ||
                                    result.toString().contains("User not found", ignoreCase = true) ->
                                    context.getString(R.string.kroniq_add_member_error_not_registered)
                                result.toString().contains("ALREADY_MEMBER", ignoreCase = true) ->
                                    context.getString(R.string.kroniq_add_member_error_already_member)
                                result.toString().contains("ALREADY_GUEST", ignoreCase = true) ->
                                    context.getString(R.string.kroniq_add_guest_error_already_guest)
                                result.toString().contains("KRONIQ_PLAN_REQUIRED", ignoreCase = true) ->
                                    context.getString(R.string.kroniq_add_member_error_plan_required)
                                else ->
                                    result.toString()
                            }
                            navController.popBackStack()
                        }
                    }
                }.onFailure {
                    savingEntry.savedStateHandle.remove<Boolean>(KRONIQ_ADD_GUEST_IN_FLIGHT_KEY)
                    requestEntry.savedStateHandle.remove<String>(KRONIQ_ADD_GUEST_EMAIL_KEY)
                    requestEntry.savedStateHandle[KRONIQ_ADD_GUEST_ERROR_KEY] =
                        context.getString(R.string.auth_error_network_or_server)
                    navController.popBackStack()
                }
            }

            SavingTripScreen(
                message = androidx.compose.ui.res.stringResource(R.string.kroniq_add_guest_saving)
            )
        }

        composable(Screen.KroniQAddGuestSuccess.route) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1500)
                navController.navigate(Screen.KroniQ.route) {
                    popUpTo(Screen.KroniQAddGuest.route) { inclusive = true }
                    launchSingleTop = true
                }
            }

            TripSuccessContent(
                title = androidx.compose.ui.res.stringResource(R.string.kroniq_add_guest_success_title),
                subtitle = androidx.compose.ui.res.stringResource(R.string.kroniq_add_guest_success_subtitle),
                footer = androidx.compose.ui.res.stringResource(R.string.kroniq_add_guest_success_footer)
            )
        }

        composable(
            route = Screen.Language.route,
            enterTransition = { defaultEnter(initialState, targetState) },
            exitTransition = { defaultExit(initialState, targetState) }
        ) {
            LanguageScreen(
                navController = navController
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

private fun mapKroniqAddMemberError(context: android.content.Context, message: String?): String {
    val normalized = message.orEmpty()
    return when {
        normalized.contains("USER_NOT_FOUND", ignoreCase = true) ||
            normalized.contains("User not found", ignoreCase = true) ->
            context.getString(R.string.kroniq_add_member_error_not_registered)
        normalized.contains("ALREADY_MEMBER", ignoreCase = true) ->
            context.getString(R.string.kroniq_add_member_error_already_member)
        normalized.contains("KRONIQ_PLAN_REQUIRED", ignoreCase = true) ->
            context.getString(R.string.kroniq_add_member_error_plan_required)
        normalized.isBlank() ->
            context.getString(R.string.auth_error_network_or_server)
        else -> normalized
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultEnter(
    initial: NavBackStackEntry,
    target: NavBackStackEntry
): EnterTransition {

    val from = initial.destination.route
    val to = target.destination.route

    if (
        (from == Screen.Login.route && to == Screen.Home.route) ||
        (from == Screen.SignUp.route && to == Screen.Home.route)
    ) {
        return fadeIn(animationSpec = tween(180))
    }

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

    if (
        (from == Screen.Login.route && to == Screen.Home.route) ||
        (from == Screen.SignUp.route && to == Screen.Home.route)
    ) {
        return fadeOut(animationSpec = tween(140))
    }

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

