package com.kaleido.app.ui.store

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleido.app.core.asPrice
import com.kaleido.app.domain.CartLine
import com.kaleido.app.ui.components.MessageState
import com.kaleido.app.ui.components.ProductImage
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
            subtitle = "Add products and your basket is saved on-device — no account needed.",
            actionLabel = "Start shopping",
            onAction = onBrowse,
        )
        return
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(summary.lines, key = { it.product.id }) { line ->
                CartRow(
                    line = line,
                    onClick = { onProductClick(line.product.id) },
                    onDecrement = { shelf.setQuantity(line.product.id, line.quantity - 1) },
                    onIncrement = { shelf.setQuantity(line.product.id, line.quantity + 1) },
                    onRemove = { shelf.setQuantity(line.product.id, 0) },
                )
            }
        }

        Card(Modifier.fillMaxWidth().padding(12.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryLine("Items", summary.itemCount.toString())
                SummaryLine("Subtotal", summary.subtotal.asPrice())
                if (summary.savingsFromDeals > 0.0) {
                    SummaryLine(
                        "Smart-basket savings",
                        "-${summary.savingsFromDeals.asPrice()}",
                        highlight = true,
                    )
                }
                HorizontalDivider()
                SummaryLine(
                    "Estimated total",
                    (summary.subtotal - summary.savingsFromDeals).asPrice(),
                    bold = true,
                )
                TextButton(onClick = shelf::clearCart, modifier = Modifier.align(Alignment.End)) {
                    Text("Clear cart")
                }
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, bold: Boolean = false, highlight: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
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
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductImage(
            url = line.product.imageUrl,
            contentDescription = line.product.title,
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(line.product.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Text(line.lineTotal.asPrice(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement) { Icon(Icons.Filled.Remove, contentDescription = "Decrease") }
                Text("${line.quantity}", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onIncrement) { Icon(Icons.Filled.Add, contentDescription = "Increase") }
            }
        }
        IconButton(onClick = onRemove) { Icon(Icons.Filled.Delete, contentDescription = "Remove") }
    }
}
