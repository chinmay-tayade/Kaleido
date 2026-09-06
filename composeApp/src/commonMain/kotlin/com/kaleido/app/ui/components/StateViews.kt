package com.kaleido.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.kaleido.app.ui.theme.sdp
import com.kaleido.app.ui.theme.ssp

@Composable
fun MessageState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.sdp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 16.ssp,
            textAlign = TextAlign.Center,
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 13.ssp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.sdp),
        )
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 13.ssp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 18.sdp)
                    .clip(RoundedCornerShape(10.sdp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 24.sdp, vertical = 11.sdp),
            )
        }
    }
}

/** Shimmering placeholder block. */
@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "alpha",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.sdp))
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Composable
fun CatalogSkeletonGrid(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 10.sdp, end = 10.sdp,
            top = contentPadding.calculateTopPadding() + 8.sdp,
            bottom = 16.sdp,
        ),
        horizontalArrangement = Arrangement.spacedBy(10.sdp),
        verticalArrangement = Arrangement.spacedBy(10.sdp),
    ) {
        items(8) {
            Column(verticalArrangement = Arrangement.spacedBy(8.sdp)) {
                ShimmerBox(Modifier.fillMaxWidth().height(150.sdp))
                ShimmerBox(Modifier.fillMaxWidth().height(12.sdp))
                ShimmerBox(Modifier.fillMaxWidth(0.6f).height(12.sdp))
                ShimmerBox(Modifier.fillMaxWidth(0.4f).height(14.sdp))
            }
        }
    }
}
