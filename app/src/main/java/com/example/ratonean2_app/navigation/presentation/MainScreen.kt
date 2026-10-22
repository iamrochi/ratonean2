package com.example.ratonean2_app.navigation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ratonean2_app.R
import com.example.ratonean2_app.branch.domain.model.branchKey
import com.example.ratonean2_app.branch.detail.presentation.view.BranchDetailScreen
import com.example.ratonean2_app.branch.domain.model.Branch
import com.example.ratonean2_app.list.presentation.view.ListScreen
import com.example.ratonean2_app.map.presentation.state.MapIntent
import com.example.ratonean2_app.map.presentation.state.MapViewState
import com.example.ratonean2_app.map.presentation.view.MapScreen
import com.example.ratonean2_app.map.presentation.viewmodel.MapViewModel
import com.example.ratonean2_app.navigation.presentation.components.DrawerContent
import com.example.ratonean2_app.navigation.presentation.components.OfflineBanner
import com.example.ratonean2_app.navigation.presentation.components.SearchBarHeader
import com.example.ratonean2_app.navigation.presentation.components.TabButton
import com.example.ratonean2_app.product.domain.model.Product
import com.example.ratonean2_app.product.presentation.components.ProductCard
import com.example.ratonean2_app.profile.ProfileScreen
import com.example.ratonean2_app.shoppinglist.presentation.ShoppingListScreen
import com.example.ratonean2_app.shoppinglist.presentation.compare.CompareListsScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = koinViewModel(),
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var query by rememberSaveable { mutableStateOf("") }
    val uiState by mapViewModel.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val connectivity = remember(context) {
        com.example.ratonean2_app.core.network.ConnectivityObserver(context.applicationContext)
    }
    val isOnline by connectivity.isOnline.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabInHome by rememberSaveable { mutableStateOf("map") }


    LaunchedEffect(Unit) {
        mapViewModel.events.collect { message ->
            // P1.1: VER LISTA ahora navega. Antes se ignoraba el SnackbarResult.
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "VER LISTA",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                navController.navigate("shopping_list") {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerContent(
                currentRoute = currentRoute,
                onDestinationClicked = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                },
                containerColor = Color.Transparent
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("home") {
                        // Scaffold propio: search + tabs en topBar real (antes overlay
                        // flotante + padding mágico de 160dp en Lista que se rompía
                        // con font-scale/landscape/foldables).
                        Scaffold(
                            topBar = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (!isOnline) OfflineBanner()
                                    SearchBarHeader(
                                        query = query,
                                        onQueryChange = { query = it },
                                        drawerState = drawerState,
                                        uiState = uiState,
                                        onClearClick = { query = "" },
                                        onIntent = { intent -> mapViewModel.onIntent(intent) },
                                    )
                                    HomeTabSelector(
                                        selectedTab = selectedTabInHome,
                                        onTabSelected = { tab -> selectedTabInHome = tab }
                                    )
                                }
                            },
                            containerColor = Color.Transparent
                        ) { innerPadding ->
                            HomePagerContent(
                                uiState = uiState,
                                onIntent = { intent -> mapViewModel.onIntent(intent) },
                                selectedTab = selectedTabInHome,
                                onTabChanged = { tab -> selectedTabInHome = tab },
                                onBranchClick = { branch ->
                                    navController.navigate(
                                        Screen.BranchDetail.createRoute(branch.branchKey, branch.name)
                                    )
                                },
                                onShoppingListClick = {
                                    navController.navigate("shopping_list") {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onMarkerClick = { branchKey, branchName ->
                                    navController.navigate(
                                        Screen.BranchDetail.createRoute(branchKey, branchName)
                                    )
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                    composable("profile") {
                        ProfileScreen(onBack = { navController.popBackStack() })
                    }
                    composable("shopping_list") {
                        ShoppingListScreen(
                            onBack = { navController.popBackStack() },
                            onCompare = {
                                // launchSingleTop: el doble-tap ya no apila N compares.
                                navController.navigate(Screen.CompareLists.route) {
                                    launchSingleTop = true
                                }
                            },
                            onSearchProducts = {
                                navController.navigate("home") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable(Screen.CompareLists.route) {
                        CompareListsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = Screen.BranchDetail.route,
                        arguments = listOf(
                            androidx.navigation.navArgument("branchKey") { type = androidx.navigation.NavType.StringType },
                            androidx.navigation.navArgument("branchName") {
                                type = androidx.navigation.NavType.StringType
                                defaultValue = ""
                            }
                        )
                    ) { backStackEntry ->
                        val branchKey = backStackEntry.arguments?.getString("branchKey") ?: ""
                        // Query-arg: Navigation ya decodifica (antes URLDecoder manual).
                        val branchName = backStackEntry.arguments?.getString("branchName") ?: ""
                        // P0.6: guard + decode moderno. Antes navegaba a detail con id="" (404)
                        // y URLDecoder deprecated lanzaba con % malformado.
                        if (branchKey.isBlank()) {
                            LaunchedEffect(Unit) { navController.popBackStack() }
                            return@composable
                        }
                        BranchDetailScreen(
                            branchKey = branchKey,
                            branchName = branchName,
                            onBack = { navController.popBackStack() },
                            onAddProduct = { product ->
                                mapViewModel.onIntent(MapIntent.AddProductToCart(product))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTabSelector(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.padding(top = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabButton("Mapa", selectedTab == "map") { onTabSelected("map") }
            TabButton("Lista", selectedTab == "list") { onTabSelected("list") }
        }
    }
}

@Composable
fun HomePagerContent(
    uiState: MapViewState,
    onIntent: (intent: MapIntent) -> Unit,
    selectedTab: String,
    onTabChanged: (String) -> Unit,
    onBranchClick: (Branch) -> Unit,
    onShoppingListClick: () -> Unit,
    onMarkerClick: (branchKey: String, branchName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val products = uiState.searchProducts
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { 2 })

    // Resetea el carrusel cuando cambia el resultado (antes quedaba el índice
    // viejo apuntando a otro producto o fuera de rango).
    LaunchedEffect(products) { currentIndex = 0 }

    LaunchedEffect(selectedTab) {
        val target = if (selectedTab == "map") 0 else 1
        if (pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        onTabChanged(if (pagerState.currentPage == 0) "map" else "list")
    }

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> MapScreen(
                    uiState = uiState,
                    onIntent = onIntent,
                    onMarkerClick = onMarkerClick
                )
                1 -> ListScreen(
                    uiState = uiState,
                    onIntent = onIntent,
                    onBranchClick = onBranchClick,
                    onShoppingListClick = onShoppingListClick
                )
            }
        }

        if (selectedTab == "map" && products.isNotEmpty()) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (products.size > 1) {
                    IconButton(
                        onClick = { currentIndex = if (currentIndex > 0) currentIndex - 1 else products.lastIndex },
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Anterior", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                val currentProduct = products.getOrNull(currentIndex)
                currentProduct?.let { product ->
                    ProductCard(
                        storeLogoRes = R.drawable.markers_ratonean2,
                        productImageUrl = product.imageUrl ?: "",
                        productName = product.description,
                        productPrice = "$${product.listPrice}",
                        onAddClick = { onIntent(MapIntent.AddProductToCart(product)) },
                        modifier = Modifier.width(280.dp)
                    )
                }

                if (products.size > 1) {
                    IconButton(
                        onClick = { currentIndex = if (currentIndex < products.lastIndex) currentIndex + 1 else 0 },
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Siguiente", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
