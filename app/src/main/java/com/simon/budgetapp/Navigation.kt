package com.simon.budgetapp

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Budgets : Screen("budgets")
    object BudgetDetail : Screen("budget_detail/{budgetId}") {
        fun createRoute(budgetId: Int) = "budget_detail/$budgetId"
    }
    object Stats : Screen("stats/{budgetId}") {
        fun createRoute(budgetId: Int) = "stats/$budgetId"
    }
    object CategoryDetail : Screen("category_detail/{budgetId}") {
        fun createRoute(budgetId: Int) = "category_detail/$budgetId"
    }
    object Register : Screen("register")
    object Recurring : Screen("recurring/{budgetId}") {
        fun createRoute(budgetId: Int) = "recurring/$budgetId"
    }
    object Sharing : Screen("sharing/{budgetId}") {
        fun createRoute(budgetId: Int) = "sharing/$budgetId"
    }
    object Splash : Screen("splash")

}

