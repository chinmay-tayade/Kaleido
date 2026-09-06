package com.kaleido.app.ui.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleido.app.ui.components.MessageState
import com.kaleido.app.ui.components.ProductImage
import com.kaleido.app.ui.theme.commerce
import com.kaleido.app.ui.theme.sdp
import com.kaleido.app.ui.theme.ssp
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
            subtitle = "Use \"Add to compare\" on any product to line them up — price, rating, reviews and deal score, best in each row highlighted.",
            actionLabel = "Browse the catalog",
            onAction = onBrowse,
        )
        return
    }

    val colWidth = 128.sdp
    val labelWidth = 92.sdp

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                top = contentPadding.calculateTopPadding() + 8.sdp,
                bottom = contentPadding.calculateBottomPadding(),
            ),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.sdp, vertical = 4.sdp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Compare ${state.products.size} products",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 15.ssp,
            )
            Text(
                "Clear all",
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.ssp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = viewModel::clear).padding(6.sdp),
            )
        }

        Column(
            Modifier
                .padding(12.sdp)
                .clip(RoundedCornerShape(12.sdp))
                .background(MaterialTheme.colorScheme.surface)
                .horizontalScroll(rememberScrollState())
                .padding(14.sdp),
        ) {
            Row {
                Spacer(Modifier.width(labelWidth))
                state.products.forEach { p ->
                    Column(
                        Modifier.width(colWidth).padding(horizontal = 4.sdp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box {
                            ProductImage(
                                url = p.imageUrl,
                                contentDescription = p.title,
                                modifier = Modifier.width(colWidth).height(84.sdp).clip(RoundedCornerShape(8.sdp)),
                            )
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Remove ${p.title}",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.sdp)
                                    .clickable { viewModel.remove(p.id) },
                            )
                        }
                        Text(
                            p.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.ssp,
                            lineHeight = 12.ssp,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.sdp),
                        )
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 10.sdp), color = MaterialTheme.colorScheme.outlineVariant)

            state.rows.forEachIndexed { index, row ->
                Row(Modifier.padding(vertical = 7.sdp)) {
                    Text(
                        row.label,
                        modifier = Modifier.width(labelWidth),
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 11.ssp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    row.values.forEachIndexed { i, value ->
                        val verdict = row.verdicts.getOrNull(i) ?: CompareVerdict.NEUTRAL
                        Text(
                            text = value,
                            modifier = Modifier.width(colWidth).padding(horizontal = 4.sdp),
                            textAlign = TextAlign.Center,
                            fontSize = 12.ssp,
                            fontWeight = if (verdict == CompareVerdict.BEST) FontWeight.ExtraBold else FontWeight.Normal,
                            color = when (verdict) {
                                CompareVerdict.BEST -> MaterialTheme.commerce.savings
                                CompareVerdict.WORST -> MaterialTheme.colorScheme.onSurfaceVariant
                                CompareVerdict.NEUTRAL -> MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
                if (index < state.rows.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
