package com.example.ratonean2_app.navigation.presentation

import android.net.Uri

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ShoppingList : Screen("shopping_list")
    object Profile : Screen("profile")
    object CompareLists : Screen("compare_lists")
    // branchName como query-arg opcional: IDs con "/" o "%" ya no rompen la ruta
    // (antes era path segment con URLEncoder de formularios, frágil con + y %).
    object BranchDetail : Screen("branch_detail/{branchKey}?branchName={branchName}") {
        fun createRoute(branchKey: String, branchName: String) =
            "branch_detail/${Uri.encode(branchKey)}?branchName=${Uri.encode(branchName)}"
    }
}
