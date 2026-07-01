package com.simon.budgetapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.simon.budgetapp.ui.auth.LoginScreen
import com.simon.budgetapp.ui.budgets.BudgetsScreen
import com.simon.budgetapp.ui.budgetdetail.BudgetDetailScreen
import com.simon.budgetapp.ui.stats.StatsScreen
import com.simon.budgetapp.ui.auth.RegisterScreen
import com.simon.budgetapp.ui.recurring.RecurringScreen
import com.simon.budgetapp.ui.sharing.SharingScreen
import com.simon.budgetapp.ui.auth.SplashScreen
import com.simon.budgetapp.ui.categorydetail.CategoryDetailScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onSessionValid = {
                    navController.navigate(Screen.Budgets.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onSessionInvalid = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Une fois inscrit, on renvoie vers Login pour que l'utilisateur se connecte
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Budgets.route) {
            BudgetsScreen(
                onBudgetClick = { budgetId ->
                    navController.navigate(Screen.BudgetDetail.createRoute(budgetId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.BudgetDetail.route,
            arguments = listOf(navArgument("budgetId") { type = NavType.IntType })
        ) { backStackEntry ->
            val budgetId = backStackEntry.arguments?.getInt("budgetId") ?: 0
            BudgetDetailScreen(
                budgetId = budgetId,
                onBack = { navController.popBackStack() },
                onNavigateToStats = { id -> navController.navigate(Screen.Stats.createRoute(id)) },
                onNavigateToRecurring = { id -> navController.navigate(Screen.Recurring.createRoute(id)) },
                onNavigateToSharing = { id -> navController.navigate(Screen.Sharing.createRoute(id)) },
                onNavigateToCategoryDetail = { id -> navController.navigate(Screen.CategoryDetail.createRoute(id)) }
            )
        }
        composable(
            route = Screen.Stats.route,
            arguments = listOf(navArgument("budgetId") { type = NavType.IntType })
        ) { backStackEntry ->
            val budgetId = backStackEntry.arguments?.getInt("budgetId") ?: 0
            StatsScreen(
                budgetId = budgetId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.CategoryDetail.route,
            arguments = listOf(navArgument("budgetId") { type = NavType.IntType })
        ) { backStackEntry ->
            val budgetId = backStackEntry.arguments?.getInt("budgetId") ?: 0
            CategoryDetailScreen(
                budgetId = budgetId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Recurring.route,
            arguments = listOf(navArgument("budgetId") { type = NavType.IntType })
        ) { backStackEntry ->
            val budgetId = backStackEntry.arguments?.getInt("budgetId") ?: 0
            RecurringScreen(
                budgetId = budgetId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Sharing.route,
            arguments = listOf(navArgument("budgetId") { type = NavType.IntType })
        ) { backStackEntry ->
            val budgetId = backStackEntry.arguments?.getInt("budgetId") ?: 0
            SharingScreen(
                budgetId = budgetId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Budgets.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

    }
}

