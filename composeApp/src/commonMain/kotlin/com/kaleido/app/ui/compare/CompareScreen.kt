package com.kaleido.app.ui.compare

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleido.app.ui.components.MessageState
import com.kaleido.app.ui.components.ProductImage
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CompareScreen(
    onBrowse: () -> Unit,
    contentPadding: PaddingValues,
    viewModel: CompareViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.products.size < 2) {
        MessageState(
            title = "Pick at least two products",
            subtitle = "Use “Add to compare” on a product to line them up side by side — price, rating, reviews and deal score.",
            actionLabel = "Browse the catalog",
            onAction = onBrowse,
        )
        return
    }

    val colWidth = 140.dp
    val labelWidth = 104.dp

    Column(
        Modifier.fillMaxSize().padding(
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding(),
        ),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Compare ${state.products.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = viewModel::clear) { Text("Clear all") }
        }

        Column(Modifier.horizontalScroll(rememberScrollState()).padding(16.dp)) {
            // header: images + titles + remove buttons
            Row {
                Spacer(labelWidth)
                state.products.forEach { p ->
                    Column(
                        Modifier.width(colWidth).padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ProductImage(
                            url = p.imageUrl,
                            contentDescription = p.title,
                            modifier = Modifier.width(colWidth).height(90.dp).clip(RoundedCornerShape(10.dp)),
                        )
                        Text(
                            p.title,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                        )
                        IconButton(onClick = { viewModel.remove(p.id) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove ${p.title}")
                        }
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            state.rows.forEach { row ->
                Row(Modifier.padding(vertical = 6.dp)) {
                    Text(
                        row.label,
                        modifier = Modifier.width(labelWidth),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    row.values.forEachIndexed { i, value ->
                        val verdict = row.verdicts.getOrNull(i) ?: CompareVerdict.NEUTRAL
                        Text(
                            text = value,
                            modifier = Modifier.width(colWidth).padding(horizontal = 4.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = if (verdict == CompareVerdict.BEST) FontWeight.Bold else FontWeight.Normal,
                            color = when (verdict) {
                                CompareVerdict.BEST -> MaterialTheme.colorScheme.tertiary
                                CompareVerdict.WORST -> MaterialTheme.colorScheme.onSurfaceVariant
                                CompareVerdict.NEUTRAL -> MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun Spacer(width: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.layout.Spacer(Modifier.width(width))
}
