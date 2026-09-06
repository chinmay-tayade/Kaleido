package com.kaleido.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.kaleido.app.ui.catalog.CatalogScreen
import com.kaleido.app.ui.compare.CompareScreen
import com.kaleido.app.ui.detail.DetailScreen
import com.kaleido.app.ui.insights.InsightsScreen
import com.kaleido.app.ui.nav.Routes
import com.kaleido.app.ui.nav.TopLevel
import com.kaleido.app.ui.store.CartScreen
import com.kaleido.app.ui.store.ShelfViewModel
import com.kaleido.app.ui.store.WishlistScreen
import com.kaleido.app.ui.theme.KaleidoTheme
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    setSingletonImageLoaderFactory { context -> newImageLoader(context) }
    KaleidoTheme {
        val navController = rememberNavController()
        val shelf: ShelfViewModel = koinInject()
        val cart by shelf.cart.collectAsStateWithLifecycle()
        val wishlistIds by shelf.wishlistIds.collectAsStateWithLifecycle()
        val compareIds by shelf.compareIds.collectAsStateWithLifecycle()

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val topLevelRoutes = TopLevel.entries.map { it.route }
        val showChrome = currentRoute == null || currentRoute in topLevelRoutes

        fun open(route: String) = navController.navigate(route) {
            launchSingleTop = true
        }

        Scaffold(
            topBar = {
                if (showChrome) {
                    TopAppBar(
                        title = { Text("Kaleido", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    )
                }
            },
            bottomBar = {
                if (showChrome) {
                    NavigationBar {
                        TopLevel.entries.forEach { dest ->
                            val selected = backStackEntry?.destination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    val count = when (dest) {
                                        TopLevel.Cart -> cart.itemCount
                                        TopLevel.Wishlist -> wishlistIds.size
                                        else -> 0
                                    }
                                    BadgedBox(badge = { if (count > 0) Badge { Text("$count") } }) {
                                        Icon(dest.icon(), contentDescription = dest.label)
                                    }
                                },
                                label = { Text(dest.label) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.CATALOG,
                modifier = Modifier.padding(),
            ) {
                composable(Routes.CATALOG) {
                    CatalogScreen(
                        onProductClick = { open(Routes.detail(it)) },
                        isWishlisted = { it in wishlistIds },
                        onToggleWishlist = shelf::toggleWishlist,
                        contentPadding = padding,
                    )
                }
                composable(Routes.WISHLIST) {
                    WishlistScreen(
                        onProductClick = { open(Routes.detail(it)) },
                        onBrowse = { open(Routes.CATALOG) },
                        contentPadding = padding,
                    )
                }
                composable(Routes.CART) {
                    CartScreen(
                        onProductClick = { open(Routes.detail(it)) },
                        onBrowse = { open(Routes.CATALOG) },
                        contentPadding = padding,
                    )
                }
                composable(Routes.INSIGHTS) {
                    InsightsScreen(contentPadding = padding)
                }
                composable(Routes.COMPARE) {
                    CompareScreen(
                        onBrowse = { open(Routes.CATALOG) },
                        contentPadding = padding,
                    )
                }
                composable(
                    route = Routes.DETAIL_PATTERN,
                    arguments = listOf(navArgument(Routes.DETAIL_ARG) { type = NavType.IntType }),
                ) { entry ->
                    val id = entry.arguments?.read { getIntOrNull(Routes.DETAIL_ARG) } ?: 1
                    DetailScreen(
                        productId = id,
                        onBack = { navController.popBackStack() },
                        onOpenProduct = { open(Routes.detail(it)) },
                        isWishlisted = { it in wishlistIds },
                        onToggleWishlist = shelf::toggleWishlist,
                        inCompare = { it in compareIds },
                        onToggleCompare = shelf::toggleCompare,
                        onOpenCompare = { open(Routes.COMPARE) },
                        quantityInCart = shelf::quantityOf,
                        onAddToCart = shelf::addToCart,
                        onRecordView = shelf::recordView,
                    )
                }
            }
        }
    }
}

private fun TopLevel.icon() = when (this) {
    TopLevel.Catalog -> Icons.Filled.Home
    TopLevel.Wishlist -> Icons.Outlined.FavoriteBorder
    TopLevel.Cart -> Icons.Filled.ShoppingCart
    TopLevel.Insights -> Icons.Filled.Insights
}

/** One image loader for every platform — Ktor fetches, so it works on iOS and Web too. */
private fun newImageLoader(context: PlatformContext): ImageLoader =
    ImageLoader.Builder(context)
        .components { add(KtorNetworkFetcherFactory()) }
        .crossfade(true)
        .build()
