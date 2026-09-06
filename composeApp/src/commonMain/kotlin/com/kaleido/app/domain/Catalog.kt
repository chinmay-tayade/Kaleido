package com.kaleido.app.domain

/** Sorting options for the catalog, client-side. */
enum class SortOption(val label: String) {
    RELEVANCE("For you"),
    PRICE_LOW_HIGH("Price ↑"),
    PRICE_HIGH_LOW("Price ↓"),
    RATING("Top rated"),
    NAME("A–Z"),
}

/** Everything the catalog screen needs to turn a raw product list into a view. */
data class CatalogQuery(
    val search: String = "",
    val category: String? = null,
    val sort: SortOption = SortOption.RELEVANCE,
)

/**
 * Pure catalog logic: filter by search + category, then sort. Kept out of the
 * ViewModel so it is trivially unit-testable and identical on every platform.
 */
object CatalogEngine {

    fun categories(products: List<Product>): List<String> =
        products.map { it.category }.distinct().sorted()

    fun apply(products: List<Product>, query: CatalogQuery): List<Product> {
        val term = query.search.trim().lowercase()
        val filtered = products.filter { p ->
            val matchesTerm = term.isEmpty() ||
                p.title.lowercase().contains(term) ||
                p.category.lowercase().contains(term) ||
                p.description.lowercase().contains(term)
            val matchesCategory = query.category == null || p.category == query.category
            matchesTerm && matchesCategory
        }
        return when (query.sort) {
            SortOption.RELEVANCE -> filtered.sortedByDescending { it.dealScore() }
            SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.price }
            SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.price }
            SortOption.RATING -> filtered.sortedWith(
                compareByDescending<Product> { it.rating }.thenByDescending { it.ratingCount },
            )
            SortOption.NAME -> filtered.sortedBy { it.title.lowercase() }
        }
    }

    /**
     * "For You" rail: the highest deal-score product from each category, so the
     * strip is diverse instead of showing five phone cases in a row.
     */
    fun forYou(products: List<Product>, limit: Int = 10): List<Product> =
        products.groupBy { it.category }
            .values
            .mapNotNull { group -> group.maxByOrNull { it.dealScore() } }
            .sortedByDescending { it.dealScore() }
            .take(limit)
}
