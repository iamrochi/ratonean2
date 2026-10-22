package com.example.ratonean2_app.core.network

object ApiUrls {
    // P0.1: URL por buildType (debug=emulador/local, release=HTTPS prod).
    // Se configura en app/build.gradle.kts via API_BASE_URL_DEBUG / API_BASE_URL_RELEASE.
    val BASE_URL: String = com.example.ratonean2_app.BuildConfig.BASE_URL
    val LOGIN get() = "${BASE_URL}/auth/login"
    val AUTH_GOOGLE get() = "${BASE_URL}/auth/google"
    val USERS get() = "${BASE_URL}/users"
    val USER_BY_ID get() = "${BASE_URL}/users/{id}"
    // Backend: GET /users/by-email?email= (query param, CON auth JWT).
    val USER_BY_EMAIL get() = "${BASE_URL}/users/by-email"

    val COMMERCES get() = "${BASE_URL}/commerce"
    val COMMERCES_BY_ID get() = "${BASE_URL}/commerce/{id}"

    val BRANCHES get() = "${BASE_URL}/branch"
    val BRANCHES_BY_ID get() = "${BASE_URL}/branch/{id}"
    val BRANCHES_NEARBY get() = "${BASE_URL}/branch/nearby"

    val PRODUCTS get() = "${BASE_URL}/product"
    val PRODUCTS_BY_ID get() = "${BASE_URL}/product/{id}"
    val PRODUCTS_BY_BRANCH get() = "${BASE_URL}/product/branch/{branchId}"
    val PRODUCTS_BY_BRANCH_KEY get() = "${BASE_URL}/product/by-branch-key/{key}"
    val PRODUCTS_BY_SEARCH_IN_BRANCH_KEYS get() = "${BASE_URL}/product/searchByBranchKeys"
    val PRODUCTS_PROMOS get() = "${BASE_URL}/product/promos"

    val PRODUCTS_BY_SEARCH_IN_BRANCHES get() = "${BASE_URL}/product/searchNearby"
    val PRODUCTS_POPULAR get() = "${BASE_URL}/product/popularProducts"
    val PRODUCTS_NEARBY get() = "${BASE_URL}/product/nearby"

    const val PLACES_URL = "https://nominatim.openstreetmap.org/search"
}