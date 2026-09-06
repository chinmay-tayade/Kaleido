package com.kaleido.app.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleido.app.core.asPrice
import com.kaleido.app.domain.Product
import com.kaleido.app.ui.components.MessageState
import com.kaleido.app.ui.components.PriceRow
import com.kaleido.app.ui.components.ProductImage
import com.kaleido.app.ui.components.RatingChip
import com.kaleido.app.ui.components.SectionHeader
import com.kaleido.app.ui.theme.commerce
import com.kaleido.app.ui.theme.sdp
import com.kaleido.app.ui.theme.ssp
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
            subtitle = "Tap the heart on any product to keep it here — it works offline.",
            actionLabel = "Browse the catalog",
            onAction = onBrowse,
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 12.sdp, end = 12.sdp,
            top = contentPadding.calculateTopPadding() + 8.sdp,
            bottom = contentPadding.calculateBottomPadding() + 16.sdp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.sdp),
    ) {
        if (items.isNotEmpty()) {
            item { SectionHeader("Your wishlist", subtitle = "${items.size} saved") }
            items(items, key = { it.id }) { product ->
                WishlistRow(
                    product = product,
                    onClick = { onProductClick(product.id) },
                    onRemove = { shelf.toggleWishlist(product.id) },
                    onMoveToCart = {
                        shelf.addToCart(product.id)
                        shelf.toggleWishlist(product.id)
                    },
                )
            }
        }

        if (recents.isNotEmpty()) {
            item {
                Column(Modifier.padding(top = 8.sdp)) {
                    SectionHeader("Recently viewed")
                    Box(Modifier.height(10.sdp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.sdp)) {
                        items(recents, key = { "recent-${it.id}" }) { product ->
                            Column(
                                Modifier
                                    .width(120.sdp)
                                    .clip(RoundedCornerShape(10.sdp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { onProductClick(product.id) }
                                    .padding(8.sdp),
                                verticalArrangement = Arrangement.spacedBy(4.sdp),
                            ) {
                                ProductImage(
                                    url = product.imageUrl,
                                    contentDescription = product.title,
                                    modifier = Modifier.fillMaxWidth().height(90.sdp).clip(RoundedCornerShape(6.sdp)),
                                )
                                Text(
                                    product.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.ssp,
                                    lineHeight = 12.ssp,
                                    maxLines = 2,
                                )
                                Text(
                                    product.price.asPrice(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontSize = 12.ssp,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistRow(
    product: Product,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onMoveToCart: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.sdp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.sdp),
        horizontalArrangement = Arrangement.spacedBy(12.sdp),
    ) {
        ProductImage(
            url = product.imageUrl,
            contentDescription = product.title,
            modifier = Modifier.size(84.sdp).clip(RoundedCornerShape(10.sdp)),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.sdp)) {
            Text(
                product.title,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.ssp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
            )
            RatingChip(product.rating, product.ratingCount)
            PriceRow(product, priceSize = 14.ssp)
            Row(horizontalArrangement = Arrangement.spacedBy(16.sdp), modifier = Modifier.padding(top = 2.sdp)) {
                Text(
                    "Move to cart",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 11.ssp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onMoveToCart),
                )
                Text(
                    "Remove",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 11.ssp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onRemove),
                )
            }
        }
        Icon(
            Icons.Filled.Favorite,
            contentDescription = "Saved",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(18.sdp),
        )
    }
}
