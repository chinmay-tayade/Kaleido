package com.kaleido.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.kaleido.app.core.compact
import com.kaleido.app.core.oneDecimal
import com.kaleido.app.ui.theme.commerce
import com.kaleido.app.ui.theme.sdp
import com.kaleido.app.ui.theme.ssp

/** A small rounded label — categories, "Sponsored", "In stock", etc. */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.secondaryContainer,
    content: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        fontSize = 10.ssp,
        color = content,
        modifier = modifier
            .clip(RoundedCornerShape(6.sdp))
            .background(container)
            .padding(horizontal = 7.sdp, vertical = 3.sdp),
    )
}

/** Flipkart-style green rating chip: `4.3 ★  ·  1.2k`. */
@Composable
fun RatingChip(
    rating: Double,
    count: Int,
    modifier: Modifier = Modifier,
    compactStyle: Boolean = true,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(5.sdp))
                .background(MaterialTheme.commerce.rating)
                .padding(horizontal = 5.sdp, vertical = 2.sdp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                rating.oneDecimal(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.ssp,
                style = MaterialTheme.typography.labelSmall,
            )
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(start = 2.sdp).size(10.sdp),
            )
        }
        if (count > 0) {
            Text(
                text = if (compactStyle) "  ${count.compact()}" else "  ${count.compact()} ratings",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.ssp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Plain star + number, for tight spots. */
@Composable
fun RatingStars(
    rating: Double,
    count: Int,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.commerce.star,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.sdp),
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = tint, modifier = Modifier.size(13.sdp))
        Text(rating.oneDecimal(), style = MaterialTheme.typography.labelMedium, fontSize = 11.ssp)
        if (count > 0) {
            Text(
                "(${count.compact()})",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.ssp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
