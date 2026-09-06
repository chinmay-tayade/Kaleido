package com.kaleido.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
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
import com.kaleido.app.ui.theme.ProvideResponsiveScale
import com.kaleido.app.ui.theme.ResponsiveContainer
import com.kaleido.app.ui.theme.commerce
import com.kaleido.app.ui.theme.sdp
import com.kaleido.app.ui.theme.ssp
import com.kaleido.app.core.asPrice
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    setSingletonImageLoaderFactory { context -> newImageLoader(context) }
    KaleidoTheme {
        ProvideResponsiveScale {
            val navController = rememberNavController()
            val shelf: ShelfViewModel = koinInject()
            val cart by shelf.cart.collectAsStateWithLifecycle()
            val cartQuantities by shelf.cartQuantities.collectAsStateWithLifecycle()
            val wishlistIds by shelf.wishlistIds.collectAsStateWithLifecycle()
            val compareIds by shelf.compareIds.collectAsStateWithLifecycle()

            var search by rememberSaveable { mutableStateOf("") }
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            fun confirmAddedToCart() = scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Added to cart",
                    actionLabel = "VIEW CART",
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    navController.navigate(Routes.CART) { launchSingleTop = true }
                }
            }

            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val topLevelRoutes = TopLevel.entries.map { it.route }
            val showChrome = currentRoute == null || currentRoute in topLevelRoutes

            fun open(route: String) = navController.navigate(route) { launchSingleTop = true }

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (showChrome) {
                        BrandBar(
                            search = search,
                            onSearch = { search = it },
                            showSearch = currentRoute == Routes.CATALOG,
                            cartCount = cart.itemCount,
                            onCart = { open(Routes.CART) },
                        )
                    }
                },
                bottomBar = {
                    if (showChrome) Column {
                        if (cart.itemCount > 0 && currentRoute != Routes.CART) {
                            ViewCartBar(
                                itemCount = cart.itemCount,
                                total = cart.subtotal - cart.savingsFromDeals,
                                onClick = { open(Routes.CART) },
                            )
                        }
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                        ) {
                            TopLevel.entries.forEach { dest ->
                                val selected =
                                    backStackEntry?.destination?.hierarchy?.any { it.route == dest.route } == true
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
                                            Icon(dest.icon(), contentDescription = dest.label, modifier = Modifier.size(22.sdp))
                                        }
                                    },
                                    label = { Text(dest.label, fontSize = 10.ssp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    ),
                                )
                            }
                        }
                    }
                },
            ) { padding ->
                ResponsiveContainer {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.CATALOG,
                        modifier = Modifier,
                    ) {
                        composable(Routes.CATALOG) {
                            CatalogScreen(
                                search = search,
                                onSearch = { search = it },
                                onProductClick = { open(Routes.detail(it)) },
                                isWishlisted = { it in wishlistIds },
                                onToggleWishlist = shelf::toggleWishlist,
                                onOpenCompare = { open(Routes.COMPARE) },
                                compareCount = compareIds.size,
                                cartQuantities = cartQuantities,
                                onCartAdd = { shelf.addToCart(it); confirmAddedToCart() },
                                onCartIncrement = { shelf.setQuantity(it, (cartQuantities[it] ?: 0) + 1) },
                                onCartDecrement = { shelf.setQuantity(it, (cartQuantities[it] ?: 0) - 1) },
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
                                onOpenCart = { open(Routes.CART) },
                                quantityInCart = shelf::quantityOf,
                                onAddToCart = { shelf.addToCart(it); confirmAddedToCart() },
                                onRecordView = shelf::recordView,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewCartBar(itemCount: Int, total: Double, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.commerce.add)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.sdp, vertical = 11.sdp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                "$itemCount item${if (itemCount == 1) "" else "s"}",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 11.ssp,
            )
            Text(
                total.asPrice(),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 15.ssp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "View cart",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 13.ssp,
                fontWeight = FontWeight.ExtraBold,
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.sdp),
            )
        }
    }
}

@Composable
private fun BrandBar(
    search: String,
    onSearch: (String) -> Unit,
    showSearch: Boolean,
    cartCount: Int,
    onCart: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.sdp, vertical = 8.sdp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "kaleido",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.ssp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                " store",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.ssp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(Modifier.weight(1f))
            BadgedBox(
                badge = { if (cartCount > 0) Badge { Text("$cartCount") } },
                modifier = Modifier.size(26.sdp),
            ) {
                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = "Cart",
                    modifier = Modifier
                        .size(22.sdp)
                        .clickable(onClick = onCart),
                )
            }
        }
        if (showSearch) {
            SearchField(
                value = search,
                onValueChange = onSearch,
                modifier = Modifier.padding(top = 8.sdp).fillMaxWidth(),
            )
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search for products, brands and more",
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.sdp))
            .padding(horizontal = 10.sdp, vertical = 9.sdp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.sdp),
        )
        Box(Modifier.weight(1f).padding(horizontal = 8.sdp)) {
            if (value.isEmpty()) {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.ssp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.ssp,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Clear",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.sdp).clickable { onValueChange("") },
            )
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
