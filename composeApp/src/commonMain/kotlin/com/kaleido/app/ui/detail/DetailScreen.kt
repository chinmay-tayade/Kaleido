package com.kaleido.app.ui.detail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleido.app.core.asPrice
import com.kaleido.app.ui.components.DealScoreBadge
import com.kaleido.app.ui.components.MessageState
import com.kaleido.app.ui.components.Pill
import com.kaleido.app.ui.components.ProductImage
import com.kaleido.app.ui.components.RatingStars
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
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
    quantityInCart: (Int) -> Int,
    onAddToCart: (Int) -> Unit,
    onRecordView: (Int) -> Unit,
    viewModel: DetailViewModel = koinViewModel { parametersOf(productId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(productId) { onRecordView(productId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> MessageState(
                title = "Something went wrong",
                subtitle = state.error!!,
                actionLabel = "Retry",
                onAction = viewModel::load,
                modifier = Modifier.padding(padding),
            )
            else -> {
                val product = state.product!!
                val qty = quantityInCart(product.id)
                Column(
                    Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
                ) {
                    ProductImage(
                        url = product.imageUrl,
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    )
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Pill(product.category)
                        Text(product.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RatingStars(product.rating, product.ratingCount)
                            DealScoreBadge(product.dealScore())
                        }
                        Text(
                            product.price.asPrice(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { onAddToCart(product.id) }, modifier = Modifier.weight(1f)) {
                                Text(if (qty > 0) "In cart ($qty) · Add more" else "Add to cart")
                            }
                            FilledTonalButton(onClick = { onToggleWishlist(product.id) }) {
                                Icon(
                                    if (isWishlisted(product.id)) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Wishlist",
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { onToggleCompare(product.id) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(if (inCompare(product.id)) "In compare" else "Add to compare")
                            }
                            if (inCompare(product.id)) {
                                OutlinedButton(onClick = onOpenCompare) { Text("View") }
                            }
                        }

                        Text("About this item", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(product.description, style = MaterialTheme.typography.bodyMedium)

                        if (state.related.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text("More in ${product.category}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(state.related, key = { it.id }) { r ->
                                    Column(
                                        Modifier.width(120.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        ProductImage(
                                            url = r.imageUrl,
                                            contentDescription = r.title,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                        )
                                        Text(r.title, style = MaterialTheme.typography.labelSmall, maxLines = 2)
                                        Text(r.price.asPrice(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        OutlinedButton(onClick = { onOpenProduct(r.id) }) { Text("View") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
