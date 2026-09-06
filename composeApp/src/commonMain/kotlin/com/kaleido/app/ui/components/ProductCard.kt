package com.kaleido.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.kaleido.app.domain.Product
import com.kaleido.app.ui.theme.commerce
import com.kaleido.app.ui.theme.sdp
import com.kaleido.app.ui.theme.spectrumFor
import com.kaleido.app.ui.theme.ssp

@Composable
fun ProductImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    pad: Boolean = true,
) {
    Box(modifier.background(Color.White), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize().then(if (pad) Modifier.padding(10.sdp) else Modifier),
        )
    }
}

/** Grid card — the store's bread and butter. */
@Composable
fun ProductCard(
    product: Product,
    wishlisted: Boolean,
    onClick: () -> Unit,
    onWishlist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.sdp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.sdp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.sdp))
            .clickable(onClick = onClick),
    ) {
        Box {
            ProductImage(
                url = product.imageUrl,
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 14.sdp, topEnd = 14.sdp)),
            )
            if (product.discountPercent >= 15) {
                DiscountTag(product.discountPercent, Modifier.align(Alignment.TopStart))
            }
            WishlistDot(
                wishlisted = wishlisted,
                onClick = onWishlist,
                modifier = Modifier.align(Alignment.TopEnd).padding(6.sdp),
            )
            if (product.isTopRated) {
                Pill(
                    "★ Bestseller",
                    modifier = Modifier.align(Alignment.BottomStart).padding(6.sdp),
                    container = MaterialTheme.commerce.badge.copy(alpha = 0.14f),
                    content = MaterialTheme.commerce.badge,
                )
            }
        }

        Column(
            Modifier.padding(horizontal = 10.sdp, vertical = 9.sdp),
            verticalArrangement = Arrangement.spacedBy(5.sdp),
        ) {
            Text(
                product.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.ssp,
                letterSpacing = 0.5.ssp,
                color = spectrumFor(product.category),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                product.title,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.ssp,
                lineHeight = 15.ssp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(30.sdp),
            )
            RatingChip(product.rating, product.ratingCount)
            PriceRow(product, priceSize = 15.ssp)
        }
    }
}

@Composable
fun WishlistDot(
    wishlisted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(if (wishlisted) 1.12f else 1f, label = "heart")
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shape = CircleShape,
        modifier = modifier.size(28.sdp).clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (wishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (wishlisted) "Remove from wishlist" else "Save to wishlist",
                tint = if (wishlisted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size((17.0 * scale).sdp),
            )
        }
    }
}
