package com.example.ratonean2_app.navigation.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import com.example.ratonean2_app.list.presentation.view.ListScreen
import com.example.ratonean2_app.map.presentation.state.MapIntent
import com.example.ratonean2_app.map.presentation.state.MapViewState
import com.example.ratonean2_app.map.presentation.view.MapScreen
import com.example.ratonean2_app.map.presentation.viewmodel.MapViewModel
import com.example.ratonean2_app.navigation.presentation.components.DrawerContent
import com.example.ratonean2_app.navigation.presentation.components.SearchBarHeader
import com.example.ratonean2_app.navigation.presentation.components.TabButton
import com.example.ratonean2_app.product.presentation.components.ProductCard
import com.example.ratonean2_app.profile.ProfileScreen
import com.example.ratonean2_app.shoppinglist.presentation.ShoppingListScreen
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

    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    val uiState by mapViewModel.state.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabInHome by remember { mutableStateOf("map") }

    LaunchedEffect(Unit) {
        mapViewModel.events.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "VER LISTA",
                duration = SnackbarDuration.Long
            )
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerContent(
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
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                        HomePagerContent(
                            uiState = uiState,
                            onIntent = { intent -> mapViewModel.onIntent(intent) },
                            isSearchActive = active,
                            selectedTab = selectedTabInHome,
                            onTabChanged = { tab -> selectedTabInHome = tab }
                        )
                    }
                    composable("profile") { ProfileScreen() }
                    composable("shopping_list") { ShoppingListScreen() }
                }
            }

            if (currentRoute == "home") {
                Column(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SearchBarHeader(
                        query = query,
                        onQueryChange = { query = it },
                        active = active,
                        onActiveChange = { active = it },
                        drawerState = drawerState,
                        uiState = uiState,
                        onClearClick = { query = "" },
                        onIntent = { intent -> mapViewModel.onIntent(intent) },
                    )

                    AnimatedVisibility(
                        visible = !active,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        HomeTabSelector(
                            selectedTab = selectedTabInHome,
                            onTabSelected = { selectedTabInHome = it }
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
    isSearchActive: Boolean,
    selectedTab: String,
    onTabChanged: (String) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { 2 })

    LaunchedEffect(selectedTab) {
        val target = if (selectedTab == "map") 0 else 1
        if (pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }
    
    LaunchedEffect(pagerState.currentPage) {
        onTabChanged(if (pagerState.currentPage == 0) "map" else "list")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> MapScreen(uiState = uiState, onIntent = onIntent)
                1 -> ListScreen(
                    uiState = uiState,
                    onIntent = onIntent,
                    modifier = Modifier.padding(top = 160.dp)
                )
            }
        }

        if (selectedTab == "map" && uiState.searchProducts.isNotEmpty() && !isSearchActive) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val products = uiState.searchProducts
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
