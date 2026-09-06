package com.kaleido.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.kaleido.app.core.asPrice
import com.kaleido.app.domain.Product
import com.kaleido.app.ui.theme.commerce
import com.kaleido.app.ui.theme.sdp
import com.kaleido.app.ui.theme.spectrumFor
import com.kaleido.app.ui.theme.ssp

/** Deal price + struck MRP + green "N% off" — the row every store shows. */
@Composable
fun PriceRow(
    product: Product,
    modifier: Modifier = Modifier,
    priceSize: androidx.compose.ui.unit.TextUnit = 16.ssp,
    showOff: Boolean = true,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.sdp),
    ) {
        Text(
            product.price.asPrice(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            fontSize = priceSize,
        )
        Text(
            product.listPrice.asPrice(),
            style = MaterialTheme.typography.labelMedium,
            fontSize = (priceSize.value * 0.72).ssp,
            color = MaterialTheme.commerce.priceStrike,
            textDecoration = TextDecoration.LineThrough,
            modifier = Modifier.padding(bottom = 1.sdp),
        )
        if (showOff) {
            Text(
                "${product.discountPercent}% off",
                style = MaterialTheme.typography.labelMedium,
                fontSize = (priceSize.value * 0.72).ssp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.commerce.savings,
                modifier = Modifier.padding(bottom = 1.sdp),
            )
        }
    }
}

/** Corner ribbon on a product image. */
@Composable
fun DiscountTag(percent: Int, modifier: Modifier = Modifier) {
    Text(
        "$percent%\nOFF",
        style = MaterialTheme.typography.labelSmall,
        fontSize = 9.ssp,
        lineHeight = 10.ssp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(bottomEnd = 8.sdp))
            .background(MaterialTheme.commerce.savings)
            .padding(horizontal = 6.sdp, vertical = 3.sdp),
    )
}

/** Small outlined "value" badge derived from the deal score. */
@Composable
fun DealScoreBadge(score: Int, modifier: Modifier = Modifier) {
    val color = when {
        score >= 75 -> MaterialTheme.commerce.rating
        score >= 55 -> MaterialTheme.commerce.star
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.sdp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.sdp, vertical = 2.sdp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.sdp),
    ) {
        Text("◆", color = color, fontSize = 8.ssp)
        Text(
            "$score value",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.ssp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontSize = 16.ssp)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.ssp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (actionText != null && onAction != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clip(RoundedCornerShape(6.sdp)),
            ) {
                Text(
                    actionText,
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 12.ssp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.sdp))
                        .padding(4.sdp),
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.sdp),
                )
            }
        }
    }
}

/** Circular category chip with a coloured initial — the row of icons at the top of every store home. */
@Composable
fun CategoryAvatar(
    category: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = spectrumFor(category)
    Column(
        modifier = modifier.padding(horizontal = 2.sdp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(52.sdp)
                .clip(CircleShape)
                .background(if (selected) color else color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                category.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.ssp,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) Color.White else color,
            )
        }
        Text(
            category,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.ssp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.sdp).size(width = 60.sdp, height = 14.sdp),
        )
    }
}

/** Gradient promo strip for the home screen. */
@Composable
fun HeroBanner(
    headline: String,
    sub: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.sdp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                    ),
                ),
            )
            .padding(horizontal = 18.sdp, vertical = 16.sdp),
    ) {
        Column {
            Text(
                headline,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 19.ssp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Text(
                sub,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.ssp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 2.sdp),
            )
        }
    }
}
