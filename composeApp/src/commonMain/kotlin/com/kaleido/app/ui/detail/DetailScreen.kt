package com.kaleido.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleido.app.core.asPrice
import com.kaleido.app.core.compact
import com.kaleido.app.domain.Product
import com.kaleido.app.ui.components.DealScoreBadge
import com.kaleido.app.ui.components.MessageState
import com.kaleido.app.ui.components.PriceRow
import com.kaleido.app.ui.components.ProductImage
import com.kaleido.app.ui.components.RatingChip
import com.kaleido.app.ui.theme.commerce
import com.kaleido.app.ui.theme.sdp
import com.kaleido.app.ui.theme.ssp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DetailScreen(
    productId: Int,
    onBack: () -> Unit,
    onOpenProduct: (Int) -> Unit,
    isWishlisted: (Int) -> Boolean,
    onToggleWishlist: (Int) -> Unit,
    inCompare: (Int) -> Boolean,
    onToggleCompare: (Int) -> Unit,
    onOpenCompare: () -> Unit,
    onOpenCart: () -> Unit,
    quantityInCart: (Int) -> Int,
    onAddToCart: (Int) -> Unit,
    onRecordView: (Int) -> Unit,
    viewModel: DetailViewModel = koinViewModel { parametersOf(productId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(productId) { onRecordView(productId) }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.error != null -> MessageState(
                title = "Something went wrong",
                subtitle = state.error!!,
                actionLabel = "Retry",
                onAction = viewModel::load,
            )

            else -> {
                val product = state.product!!
                val qty = quantityInCart(product.id)

                Column(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    ) {
                        // ---- image + floating controls ----
                        Box {
                            ProductImage(
                                url = product.imageUrl,
                                contentDescription = product.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .background(Color.White),
                            )
                            CircleIcon(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack, Modifier.align(Alignment.TopStart).padding(12.sdp))
                            CircleIcon(
                                if (isWishlisted(product.id)) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                "Wishlist",
                                { onToggleWishlist(product.id) },
                                Modifier.align(Alignment.TopEnd).padding(12.sdp),
                                tint = if (isWishlisted(product.id)) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.sdp),
                            verticalArrangement = Arrangement.spacedBy(10.sdp),
                        ) {
                            Text(
                                product.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.ssp,
                                letterSpacing = 0.6.ssp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                product.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 16.ssp,
                                lineHeight = 21.ssp,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RatingChip(product.rating, product.ratingCount, compactStyle = false)
                                if (product.isTopRated) {
                                    Text(
                                        "  ·  Bestseller",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontSize = 11.ssp,
                                        color = MaterialTheme.commerce.badge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            PriceRow(product, priceSize = 22.ssp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                                Text(
                                    "You save ${product.savings.asPrice()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontSize = 11.ssp,
                                    color = MaterialTheme.commerce.savings,
                                    fontWeight = FontWeight.Bold,
                                )
                                DealScoreBadge(product.dealScore())
                            }
                            Text(
                                "Inclusive of all taxes",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.ssp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(Modifier.height(8.sdp))

                        // ---- delivery + offers ----
                        InfoCard {
                            IconLine(Icons.Filled.LocalShipping, "FREE delivery in 2–4 days", "To your saved address")
                            IconLine(
                                Icons.Filled.LocalOffer,
                                "${product.discountPercent}% instant discount applied",
                                "Bank offer · no coupon needed",
                            )
                            IconLine(Icons.Filled.Bolt, "Kaleido Assured", "Quality checked · easy 7-day returns")
                        }

                        Spacer(Modifier.height(8.sdp))

                        InfoCard {
                            Text("About this item", style = MaterialTheme.typography.titleSmall, fontSize = 14.ssp)
                            Text(
                                product.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 13.ssp,
                                lineHeight = 19.ssp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.sdp),
                            )
                            SpecRow("Category", product.category)
                            SpecRow("Rating", "${product.rating}  (${product.ratingCount.compact()} ratings)")
                            SpecRow("Deal score", "${product.dealScore()} / 100")
                        }

                        Spacer(Modifier.height(8.sdp))

                        InfoCard {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text("Compare with similar", style = MaterialTheme.typography.titleSmall, fontSize = 14.ssp)
                                    Text(
                                        if (inCompare(product.id)) "Added to your comparison" else "Line this up against others",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.ssp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                TextChip(
                                    text = if (inCompare(product.id)) "View" else "Add",
                                    onClick = { if (inCompare(product.id)) onOpenCompare() else onToggleCompare(product.id) },
                                )
                            }
                        }

                        if (state.related.isNotEmpty()) {
                            Spacer(Modifier.height(8.sdp))
                            Column(
                                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(16.sdp),
                            ) {
                                Text("More in ${product.category}", style = MaterialTheme.typography.titleSmall, fontSize = 14.ssp)
                                Spacer(Modifier.height(10.sdp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.sdp)) {
                                    items(state.related, key = { it.id }) { r ->
                                        RelatedItem(r) { onOpenProduct(r.id) }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.sdp))
                    }

                    BuyBar(
                        inCartQty = qty,
                        onAddToCart = { onAddToCart(product.id) },
                        onGoToCart = onOpenCart,
                    )
                }
            }
        }
    }
}

@Composable
private fun BuyBar(inCartQty: Int, onAddToCart: () -> Unit, onGoToCart: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.sdp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(10.sdp),
            horizontalArrangement = Arrangement.spacedBy(10.sdp),
        ) {
            CtaButton(
                text = if (inCartQty > 0) "In cart · $inCartQty" else "Add to cart",
                icon = Icons.Filled.ShoppingCart,
                background = MaterialTheme.commerce.ctaCart,
                onClick = onAddToCart,
                modifier = Modifier.weight(1f),
            )
            CtaButton(
                text = if (inCartQty > 0) "Go to cart" else "Buy now",
                icon = Icons.Filled.Bolt,
                background = MaterialTheme.commerce.ctaBuy,
                onClick = if (inCartQty > 0) onGoToCart else { { onAddToCart(); onGoToCart() } },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CtaButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(10.sdp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 13.sdp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.sdp))
        Text(
            text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 13.ssp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 7.sdp),
        )
    }
}

@Composable
private fun CircleIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    cd: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), shape = CircleShape, modifier = modifier) {
        Box(Modifier.size(34.sdp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = cd, tint = tint, modifier = Modifier.size(19.sdp))
        }
    }
}

@Composable
private fun InfoCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(16.sdp),
        verticalArrangement = Arrangement.spacedBy(10.sdp),
        content = content,
    )
}

@Composable
private fun IconLine(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, sub: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.sdp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.sdp))
        Column {
            Text(title, style = MaterialTheme.typography.labelLarge, fontSize = 12.ssp)
            Text(
                sub,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.ssp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(top = 2.sdp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.ssp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.sdp),
        )
        Text(value, style = MaterialTheme.typography.bodySmall, fontSize = 12.ssp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TextChip(text: String, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontSize = 12.ssp,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(8.sdp))
            .border(1.sdp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.sdp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.sdp, vertical = 6.sdp),
    )
}

@Composable
private fun RelatedItem(product: Product, onClick: () -> Unit) {
    Column(
        Modifier
            .width(118.sdp)
            .clip(RoundedCornerShape(10.sdp))
            .border(1.sdp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.sdp))
            .clickable(onClick = onClick)
            .padding(8.sdp),
        verticalArrangement = Arrangement.spacedBy(4.sdp),
    ) {
        ProductImage(
            url = product.imageUrl,
            contentDescription = product.title,
            modifier = Modifier.fillMaxWidth().height(92.sdp).clip(RoundedCornerShape(6.sdp)),
        )
        Text(product.title, style = MaterialTheme.typography.labelSmall, fontSize = 10.ssp, lineHeight = 12.ssp, maxLines = 2)
        Text(
            product.price.asPrice(),
            style = MaterialTheme.typography.labelLarge,
            fontSize = 12.ssp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}
