package com.kaleido.app.ui.store

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleido.app.core.asPrice
import com.kaleido.app.domain.Product
import com.kaleido.app.ui.components.MessageState
import com.kaleido.app.ui.components.ProductImage
import com.kaleido.app.ui.components.RatingStars
import org.koin.compose.koinInject

@Composable
fun WishlistScreen(
    onProductClick: (Int) -> Unit,
    onBrowse: () -> Unit,
    contentPadding: PaddingValues,
    shelf: ShelfViewModel = koinInject(),
) {
    val items by shelf.wishlist.collectAsStateWithLifecycle()
    val recents by shelf.recentlyViewed.collectAsStateWithLifecycle()

    if (items.isEmpty() && recents.isEmpty()) {
        MessageState(
            title = "No saved products yet",
            subtitle = "Tap the heart on any product to keep it here — works offline.",
            actionLabel = "Browse the catalog",
            onAction = onBrowse,
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("Saved (${items.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(items, key = { it.id }) { product ->
            WishlistRow(
                product = product,
                onClick = { onProductClick(product.id) },
                onRemove = { shelf.toggleWishlist(product.id) },
                onAddToCart = { shelf.addToCart(product.id) },
            )
        }
        if (recents.isNotEmpty()) {
            item {
                Text(
                    "Recently viewed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            items(recents, key = { "recent-${it.id}" }) { product ->
                WishlistRow(
                    product = product,
                    onClick = { onProductClick(product.id) },
                    onRemove = null,
                    onAddToCart = { shelf.addToCart(product.id) },
                )
            }
        }
    }
}

@Composable
private fun WishlistRow(
    product: Product,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?,
    onAddToCart: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductImage(
            url = product.imageUrl,
            contentDescription = product.title,
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(product.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2)
            RatingStars(product.rating, product.ratingCount)
            Text(product.price.asPrice(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.End) {
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Favorite, contentDescription = "Remove", tint = MaterialTheme.colorScheme.secondary)
                }
            }
            OutlinedButton(onClick = onAddToCart) { Text("Add") }
        }
    }
}
