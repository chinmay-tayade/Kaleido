package com.kaleido.app.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleido.app.core.asPrice
import com.kaleido.app.domain.DataStatus
import com.kaleido.app.domain.Product
import com.kaleido.app.domain.SortOption
import com.kaleido.app.ui.components.CatalogSkeletonGrid
import com.kaleido.app.ui.components.CategoryAvatar
import com.kaleido.app.ui.components.HeroBanner
import com.kaleido.app.ui.components.MessageState
import com.kaleido.app.ui.components.Pill
import com.kaleido.app.ui.components.PriceRow
import com.kaleido.app.ui.components.ProductCard
import com.kaleido.app.ui.components.ProductImage
import com.kaleido.app.ui.components.RatingChip
import com.kaleido.app.ui.components.SectionHeader
import com.kaleido.app.ui.theme.commerce
import com.kaleido.app.ui.theme.sdp
import com.kaleido.app.ui.theme.ssp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    search: String,
    onSearch: (String) -> Unit,
    onProductClick: (Int) -> Unit,
    isWishlisted: (Int) -> Boolean,
    onToggleWishlist: (Int) -> Unit,
    onOpenCompare: () -> Unit,
    compareCount: Int,
    cartQuantities: Map<Int, Int>,
    onCartAdd: (Int) -> Unit,
    onCartIncrement: (Int) -> Unit,
    onCartDecrement: (Int) -> Unit,
    contentPadding: PaddingValues,
    viewModel: CatalogViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    var sortSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(search) { viewModel.onSearch(search) }

    val atBottom by remember {
        derivedStateOf {
            val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= gridState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(atBottom) { if (atBottom) viewModel.loadMore() }

    val browsing = state.query.search.isBlank() && state.query.category == null

    if (state.isFirstLoad) {
        CatalogSkeletonGrid(contentPadding)
        return
    }
    if (state.isEmpty && browsing) {
        MessageState(
            title = "Can't reach the store",
            subtitle = "You're offline and there's no saved copy yet. Reconnect and pull to retry.",
            actionLabel = "Retry",
            onAction = viewModel::refresh,
        )
        return
    }

    PullToRefreshBox(
        isRefreshing = state.status == DataStatus.Loading && state.results.isNotEmpty(),
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 10.sdp, end = 10.sdp,
            top = contentPadding.calculateTopPadding() + 8.sdp,
            bottom = contentPadding.calculateBottomPadding() + 16.sdp,
        ),
        horizontalArrangement = Arrangement.spacedBy(10.sdp),
        verticalArrangement = Arrangement.spacedBy(10.sdp),
    ) {
        fun fullSpan(content: @Composable () -> Unit) =
            item(span = { GridItemSpan(maxLineSpan) }) { content() }

        if (state.categories.isNotEmpty()) {
            fullSpan {
                val rows = if (state.categories.size > 4) 2 else 1
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(rows),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((if (rows == 2) 156 else 78).sdp),
                    horizontalArrangement = Arrangement.spacedBy(10.sdp),
                    verticalArrangement = Arrangement.spacedBy(8.sdp),
                    contentPadding = PaddingValues(vertical = 4.sdp),
                ) {
                    items(state.categories, key = { it }) { category ->
                        CategoryAvatar(
                            category = category,
                            selected = state.query.category == category,
                            onClick = { viewModel.onCategory(category) },
                        )
                    }
                }
            }
        }

        if (browsing) {
            fullSpan {
                HeroBanner(
                    headline = "Everyday finds, real value",
                    sub = "Ranked by our deal score — rating vs. price, done for you",
                    modifier = Modifier.fillMaxWidth().padding(top = 4.sdp),
                )
            }
            if (state.forYou.isNotEmpty()) {
                fullSpan {
                    Column(Modifier.padding(top = 6.sdp)) {
                        SectionHeader("Deals of the day", subtitle = "Best value picked from each category")
                        Spacer(Modifier.height(8.sdp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.sdp)) {
                            items(state.forYou, key = { it.id }) { p ->
                                DealCard(
                                    product = p,
                                    cartQuantity = cartQuantities[p.id] ?: 0,
                                    onClick = { onProductClick(p.id) },
                                    onAdd = { onCartAdd(p.id) },
                                    onIncrement = { onCartIncrement(p.id) },
                                    onDecrement = { onCartDecrement(p.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        fullSpan {
            Column(Modifier.padding(top = 6.sdp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = state.query.category ?: (if (browsing) "All products" else "Results"),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 15.ssp,
                    )
                    Text(
                        "${state.results.size} items",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 11.ssp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(8.sdp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                    FilterPill(
                        text = "Sort: ${state.query.sort.label}",
                        selected = state.query.sort != SortOption.RELEVANCE,
                        onClick = { sortSheetOpen = true },
                        leadingIcon = Icons.AutoMirrored.Filled.Sort,
                    )
                    if (compareCount > 0) {
                        FilterPill(
                            text = "Compare ($compareCount)",
                            selected = true,
                            onClick = onOpenCompare,
                            leadingIcon = Icons.AutoMirrored.Filled.CompareArrows,
                        )
                    }
                }
                if (state.isOffline) {
                    Pill(
                        "Offline · showing your saved copy",
                        modifier = Modifier.padding(top = 8.sdp),
                        container = MaterialTheme.colorScheme.surfaceVariant,
                        content = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (state.isEmpty) {
            fullSpan {
                MessageState(
                    title = "No matches",
                    subtitle = "Try a different search or clear the category filter.",
                    modifier = Modifier.height(220.sdp),
                )
            }
        }

        items(state.results, key = { it.id }) { product ->
            ProductCard(
                product = product,
                wishlisted = isWishlisted(product.id),
                cartQuantity = cartQuantities[product.id] ?: 0,
                onClick = { onProductClick(product.id) },
                onWishlist = { onToggleWishlist(product.id) },
                onAdd = { onCartAdd(product.id) },
                onIncrement = { onCartIncrement(product.id) },
                onDecrement = { onCartDecrement(product.id) },
                modifier = Modifier.animateItem(),
            )
        }

        if (state.status == DataStatus.Loading && state.results.isNotEmpty()) {
            fullSpan {
                Box(Modifier.fillMaxWidth().padding(16.sdp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 2.sdp, modifier = Modifier.size(22.sdp))
                }
            }
        }
    }
    }

    if (sortSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { sortSheetOpen = false },
            sheetState = sheetState,
        ) {
            Text(
                "Sort by",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.ssp,
                modifier = Modifier.padding(start = 20.sdp, bottom = 4.sdp),
            )
            SortOption.entries.forEach { option ->
                val selected = state.query.sort == option
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.onSort(option)
                            sortSheetOpen = false
                        }
                        .padding(horizontal = 20.sdp, vertical = 12.sdp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        option.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 14.ssp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    if (selected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.sdp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.sdp))
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.sdp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
            )
            .border(
                1.sdp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(20.sdp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.sdp, vertical = 7.sdp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(15.sdp).padding(end = 4.sdp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 12.ssp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DealCard(
    product: Product,
    cartQuantity: Int,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Column(
        Modifier
            .width(138.sdp)
            .clip(RoundedCornerShape(12.sdp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.sdp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.sdp))
            .clickable(onClick = onClick)
            .padding(8.sdp),
        verticalArrangement = Arrangement.spacedBy(5.sdp),
    ) {
        Box {
            ProductImage(
                url = product.imageUrl,
                contentDescription = product.title,
                modifier = Modifier.fillMaxWidth().height(96.sdp).clip(RoundedCornerShape(8.sdp)),
            )
            com.kaleido.app.ui.components.DiscountTag(
                product.discountPercent,
                Modifier.align(Alignment.TopStart),
            )
        }
        Text(
            product.title,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 11.ssp,
            lineHeight = 13.ssp,
            maxLines = 2,
            modifier = Modifier.height(26.sdp),
        )
        RatingChip(product.rating, product.ratingCount)
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    product.price.asPrice(),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.ssp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    product.listPrice.asPrice(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.ssp,
                    color = MaterialTheme.commerce.priceStrike,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                )
            }
            com.kaleido.app.ui.components.AddButton(
                quantity = cartQuantity,
                onAdd = onAdd,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
            )
        }
    }
}
