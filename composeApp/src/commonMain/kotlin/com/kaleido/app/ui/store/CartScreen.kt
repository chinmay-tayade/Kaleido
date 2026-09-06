package com.kaleido.app.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleido.app.core.asPrice
import com.kaleido.app.domain.CartLine
import com.kaleido.app.ui.components.MessageState
import com.kaleido.app.ui.components.ProductImage
import com.kaleido.app.ui.theme.commerce
import com.kaleido.app.ui.theme.sdp
import com.kaleido.app.ui.theme.ssp
import org.koin.compose.koinInject

@Composable
fun CartScreen(
    onProductClick: (Int) -> Unit,
    onBrowse: () -> Unit,
    contentPadding: PaddingValues,
    shelf: ShelfViewModel = koinInject(),
) {
    val summary by shelf.cart.collectAsStateWithLifecycle()

    if (summary.lines.isEmpty()) {
        MessageState(
            title = "Your cart is empty",
            subtitle = "Add products and your basket is saved on this device — no account needed.",
            actionLabel = "Start shopping",
            onAction = onBrowse,
        )
        return
    }

    val mrpTotal = summary.lines.sumOf { it.product.listPrice * it.quantity }
    val totalSaved = (mrpTotal - summary.subtotal) + summary.savingsFromDeals
    val payable = summary.subtotal - summary.savingsFromDeals

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 12.sdp, end = 12.sdp,
                top = contentPadding.calculateTopPadding() + 8.sdp,
                bottom = 12.sdp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.sdp),
        ) {
            item {
                Text(
                    "${summary.itemCount} item${if (summary.itemCount == 1) "" else "s"} in cart",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.ssp,
                )
            }
            items(summary.lines, key = { it.product.id }) { line ->
                CartRow(
                    line = line,
                    onClick = { onProductClick(line.product.id) },
                    onDecrement = { shelf.setQuantity(line.product.id, line.quantity - 1) },
                    onIncrement = { shelf.setQuantity(line.product.id, line.quantity + 1) },
                    onRemove = { shelf.setQuantity(line.product.id, 0) },
                )
            }
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.sdp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(14.sdp),
                    verticalArrangement = Arrangement.spacedBy(8.sdp),
                ) {
                    Text("Price details", style = MaterialTheme.typography.titleSmall, fontSize = 13.ssp)
                    SummaryLine("Price (${summary.itemCount} items)", mrpTotal.asPrice())
                    SummaryLine("Discount", "- ${(mrpTotal - summary.subtotal).asPrice()}", highlight = true)
                    if (summary.savingsFromDeals > 0.0) {
                        SummaryLine("Smart-basket bonus", "- ${summary.savingsFromDeals.asPrice()}", highlight = true)
                    }
                    SummaryLine("Delivery", "FREE", highlight = true)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SummaryLine("Total payable", payable.asPrice(), bold = true)
                    Text(
                        "You save ${totalSaved.asPrice()} on this order",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 11.ssp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.commerce.savings,
                    )
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 10.sdp) {
            Row(
                Modifier.fillMaxWidth().padding(12.sdp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(payable.asPrice(), style = MaterialTheme.typography.titleMedium, fontSize = 17.ssp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "View price details",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.ssp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    "Place order",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 14.ssp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.sdp))
                        .background(MaterialTheme.commerce.ctaBuy)
                        .clickable { shelf.clearCart() }
                        .padding(horizontal = 28.sdp, vertical = 13.sdp),
                )
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, bold: Boolean = false, highlight: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = if (bold) 14.ssp else 12.ssp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = if (bold) 15.ssp else 12.ssp,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (highlight) MaterialTheme.commerce.savings else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CartRow(
    line: CartLine,
    onClick: () -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onRemove: () -> Unit,
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
            url = line.product.imageUrl,
            contentDescription = line.product.title,
            modifier = Modifier.size(76.sdp).clip(RoundedCornerShape(10.sdp)),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.sdp)) {
            Text(
                line.product.title,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.ssp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.sdp)) {
                Text(
                    line.product.price.asPrice(),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 14.ssp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    line.product.listPrice.asPrice(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.ssp,
                    textDecoration = TextDecoration.LineThrough,
                    color = MaterialTheme.commerce.priceStrike,
                )
                Text(
                    "${line.product.discountPercent}% off",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.ssp,
                    color = MaterialTheme.commerce.savings,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                QtyButton(Icons.Filled.Remove, "Decrease", onDecrement)
                Text(
                    "${line.quantity}",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.ssp,
                    modifier = Modifier.padding(horizontal = 12.sdp),
                )
                QtyButton(Icons.Filled.Add, "Increase", onIncrement)
                Box(Modifier.weight(1f))
                Text(
                    "Remove",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 11.ssp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onRemove).padding(4.sdp),
                )
            }
        }
    }
}

@Composable
private fun QtyButton(icon: androidx.compose.ui.graphics.vector.ImageVector, cd: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(28.sdp)
            .clip(RoundedCornerShape(7.sdp))
            .border(1.sdp, MaterialTheme.colorScheme.outline, RoundedCornerShape(7.sdp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = cd, modifier = Modifier.size(15.sdp))
    }
}
