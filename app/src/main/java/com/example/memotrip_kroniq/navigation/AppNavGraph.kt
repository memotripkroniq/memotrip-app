package com.example.memotrip_kroniq.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.ui.addtrip.AddTripScreen
import com.example.memotrip_kroniq.ui.auth.ForgotPasswordScreen
import com.example.memotrip_kroniq.ui.auth.LoginScreen
import com.example.memotrip_kroniq.ui.auth.SignUpScreen
import com.example.memotrip_kroniq.ui.home.HomeScreen
import com.example.memotrip_kroniq.ui.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object ForgotPassword : Screen("forgot_password")
    object AddTrip : Screen("add_trip")
}

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
        ) {
            HomeScreen(navController = navController)
        }

        // =============================================================
        // ⭐ ADD TRIP
        // =============================================================
        composable(Screen.AddTrip.route) {
            AddTripScreen(
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

