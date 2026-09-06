package com.kaleido.app.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleido.app.core.asPrice
import com.kaleido.app.core.oneDecimal
import com.kaleido.app.domain.CategoryInsight
import com.kaleido.app.domain.RatingBucket
import com.kaleido.app.ui.components.MessageState
import com.kaleido.app.ui.components.SectionHeader
import com.kaleido.app.ui.theme.commerce
import com.kaleido.app.ui.theme.sdp
import com.kaleido.app.ui.theme.spectrumFor
import com.kaleido.app.ui.theme.ssp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InsightsScreen(
    contentPadding: PaddingValues,
    viewModel: InsightsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (!state.ready) {
        MessageState(
            title = "No data to analyse yet",
            subtitle = "Insights appear once the catalog has loaded at least once.",
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 12.sdp, end = 12.sdp,
            top = contentPadding.calculateTopPadding() + 10.sdp,
            bottom = contentPadding.calculateBottomPadding() + 24.sdp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.sdp),
    ) {
        item {
            Text(
                "Catalog insights",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.ssp,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.sdp)) {
                StatCard("Products", state.overview.totalProducts.toString(), spectrumFor("a"), Modifier.weight(1f))
                StatCard("Categories", state.overview.categories.toString(), spectrumFor("bb"), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.sdp)) {
                StatCard("Avg price", state.overview.averagePrice.asPrice(), spectrumFor("ccc"), Modifier.weight(1f))
                StatCard("Avg rating", "${state.overview.averageRating.oneDecimal()} ★", MaterialTheme.commerce.star, Modifier.weight(1f))
            }
        }

        item {
            SectionCard("Rating distribution", "How well-reviewed the catalog is") {
                RatingHistogram(state.ratingHistogram)
            }
        }

        item {
            SectionCard("Average price by category", "Where the money is") {
                val max = state.categories.maxOfOrNull { it.averagePrice } ?: 1.0
                Column(verticalArrangement = Arrangement.spacedBy(10.sdp)) {
                    state.categories.forEach { c -> PriceBar(c.category, c.averagePrice, max) }
                }
            }
        }

        item { SectionHeader("Category breakdown", modifier = Modifier.padding(top = 4.sdp)) }
        items(state.categories, key = { it.category }) { insight -> CategoryInsightCard(insight) }
    }
}

@Composable
private fun StatCard(label: String, value: String, accent: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.sdp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.sdp),
    ) {
        Box(Modifier.height(3.sdp).fillMaxWidth(0.3f).clip(RoundedCornerShape(2.sdp)).background(accent))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 20.ssp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 8.sdp),
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 11.ssp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.sdp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.sdp),
        verticalArrangement = Arrangement.spacedBy(12.sdp),
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontSize = 15.ssp)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.ssp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        content()
    }
}

@Composable
private fun RatingHistogram(buckets: List<RatingBucket>) {
    val max = (buckets.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    Row(
        Modifier.fillMaxWidth().height(120.sdp),
        horizontalArrangement = Arrangement.spacedBy(8.sdp),
        verticalAlignment = Alignment.Bottom,
    ) {
        buckets.forEach { bucket ->
            Column(
                Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(bucket.count.toString(), style = MaterialTheme.typography.labelSmall, fontSize = 10.ssp)
                Box(
                    Modifier
                        .padding(vertical = 3.sdp)
                        .fillMaxWidth(0.7f)
                        .fillMaxHeight((bucket.count.toFloat() / max).coerceIn(0.02f, 1f))
                        .clip(RoundedCornerShape(topStart = 5.sdp, topEnd = 5.sdp))
                        .background(MaterialTheme.colorScheme.primary),
                )
                Text(
                    bucket.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.ssp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PriceBar(label: String, value: Double, max: Double) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium, fontSize = 11.ssp, maxLines = 1)
            Text(value.asPrice(), style = MaterialTheme.typography.labelMedium, fontSize = 11.ssp, fontWeight = FontWeight.Bold)
        }
        Box(
            Modifier
                .padding(top = 4.sdp)
                .fillMaxWidth()
                .height(9.sdp)
                .clip(RoundedCornerShape(5.sdp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                Modifier
                    .fillMaxWidth((value / max).toFloat().coerceIn(0.02f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.sdp))
                    .background(spectrumFor(label)),
            )
        }
    }
}

@Composable
private fun CategoryInsightCard(insight: CategoryInsight) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.sdp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.sdp),
        verticalArrangement = Arrangement.spacedBy(4.sdp),
    ) {
        Text(insight.category, style = MaterialTheme.typography.titleSmall, fontSize = 13.ssp)
        Text(
            "${insight.count} products · avg ${insight.averagePrice.asPrice()} · " +
                "${insight.minPrice.asPrice()}–${insight.maxPrice.asPrice()}",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.ssp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Best value: ${insight.bestValue.title}  (${insight.bestValue.price.asPrice()})",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.ssp,
            color = MaterialTheme.commerce.savings,
            fontWeight = FontWeight.Medium,
        )
    }
}
