package com.kaleido.app.ui.catalog

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleido.app.domain.Product
import com.kaleido.app.domain.SortOption
import com.kaleido.app.ui.components.CatalogSkeletonGrid
import com.kaleido.app.ui.components.MessageState
import com.kaleido.app.ui.components.Pill
import com.kaleido.app.ui.components.ProductCard
import com.kaleido.app.ui.components.ProductImage
import com.kaleido.app.core.asPrice
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onProductClick: (Int) -> Unit,
    isWishlisted: (Int) -> Boolean,
    onToggleWishlist: (Int) -> Unit,
    contentPadding: PaddingValues,
    viewModel: CatalogViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    val atBottom by remember {
        derivedStateOf {
            val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= gridState.layoutInfo.totalItemsCount - 3
        }
    }
    androidx.compose.runtime.LaunchedEffect(atBottom) { if (atBottom) viewModel.loadMore() }

    when {
        state.isFirstLoad -> CatalogSkeletonGrid(contentPadding)
        state.isEmpty && state.query.search.isBlank() && state.query.category == null ->
            MessageState(
                title = "Nothing to show yet",
                subtitle = "We couldn't reach the store and there's no saved copy. Check your connection.",
                actionLabel = "Retry",
                onAction = viewModel::refresh,
            )
        else -> LazyVerticalGrid(
            columns = GridCells.Adaptive(170.dp),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    SearchField(state.query.search, viewModel::onSearch)
                    Spacer(Modifier.height(10.dp))
                    CategoryRow(state.categories, state.query.category, viewModel::onCategory)
                    Spacer(Modifier.height(4.dp))
                    SortRow(state.query.sort, viewModel::onSort)
                    if (state.isOffline) OfflineNote()
                }
            }

            if (state.forYou.isNotEmpty() && state.query.search.isBlank() && state.query.category == null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ForYouRail(state.forYou, onProductClick)
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = if (state.query.category != null) "${state.query.category} · ${state.results.size}"
                    else "All products · ${state.results.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                )
            }

            if (state.isEmpty) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    MessageState(
                        title = "No matches",
                        subtitle = "Try a different search or clear the category filter.",
                        modifier = Modifier.height(200.dp),
                    )
                }
            }

            gridItems(state.results, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    wishlisted = isWishlisted(product.id),
                    onClick = { onProductClick(product.id) },
                    onWishlist = { onToggleWishlist(product.id) },
                )
            }

            if (state.status == com.kaleido.app.domain.DataStatus.Loading && state.results.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search products, categories…") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryRow(categories: List<String>, selected: String?, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortRow(selected: SortOption, onSelect: (SortOption) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(SortOption.entries) { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option.label) },
            )
        }
    }
}

@Composable
private fun OfflineNote() {
    Pill(
        text = "Offline · showing your saved copy",
        modifier = Modifier.padding(top = 10.dp),
        container = MaterialTheme.colorScheme.surfaceVariant,
        content = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ForYouRail(products: List<Product>, onClick: (Int) -> Unit) {
    Column {
        Text("Picked for you", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Best value in each category",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(products, key = { it.id }) { p ->
                Column(
                    Modifier.width(130.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ProductImage(
                        url = p.imageUrl,
                        contentDescription = p.title,
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                    )
                    Text(p.title, style = MaterialTheme.typography.labelMedium, maxLines = 2)
                    Text(
                        p.price.asPrice(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
