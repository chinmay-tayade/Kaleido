package com.kaleido.app.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleido.app.core.asPrice
import com.kaleido.app.core.oneDecimal
import com.kaleido.app.data.local.ShelfStore
import com.kaleido.app.domain.CatalogRepository
import com.kaleido.app.domain.Product

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class CompareVerdict { BEST, WORST, NEUTRAL }

data class CompareRow(
    val label: String,
    val values: List<String>,
    /** one verdict per product column, aligned with [values] */
    val verdicts: List<CompareVerdict>,
)

data class CompareUiState(
    val products: List<Product> = emptyList(),
    val rows: List<CompareRow> = emptyList(),
)

class CompareViewModel(
    private val repository: CatalogRepository,
    private val shelf: ShelfStore,
) : ViewModel() {

    val state: StateFlow<CompareUiState> =
        combine(repository.products, shelf.compare) { products, ids ->
            val selected = ids.mapNotNull { id -> products.firstOrNull { it.id == id } }
            CompareUiState(products = selected, rows = buildRows(selected))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompareUiState())

    fun remove(id: Int) = shelf.toggleCompare(id)
    fun clear() = shelf.clearCompare()

    private fun buildRows(products: List<Product>): List<CompareRow> {
        if (products.size < 2) return emptyList()
        return listOf(
            numericRow("Price", products.map { it.price }, lowerIsBetter = true) { it.asPrice() },
            numericRow("Rating", products.map { it.rating }, lowerIsBetter = false) { it.oneDecimal() },
            numericRow("Reviews", products.map { it.ratingCount.toDouble() }, lowerIsBetter = false) { it.toInt().toString() },
            numericRow("Deal score", products.map { it.dealScore().toDouble() }, lowerIsBetter = false) { it.toInt().toString() },
            CompareRow("Category", products.map { it.category }, products.map { CompareVerdict.NEUTRAL }),
        )
    }

    private fun numericRow(
        label: String,
        values: List<Double>,
        lowerIsBetter: Boolean,
        format: (Double) -> String,
    ): CompareRow {
        val best = if (lowerIsBetter) values.min() else values.max()
        val worst = if (lowerIsBetter) values.max() else values.min()
        val verdicts = values.map {
            when {
                it == best && best != worst -> CompareVerdict.BEST
                it == worst && best != worst -> CompareVerdict.WORST
                else -> CompareVerdict.NEUTRAL
            }
        }
        return CompareRow(label, values.map(format), verdicts)
    }
}
