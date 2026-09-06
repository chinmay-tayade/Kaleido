package com.kaleido.app.domain

import kotlin.math.round

/** Aggregated stats for one category, powering the Insights screen. */
data class CategoryInsight(
    val category: String,
    val count: Int,
    val averagePrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val averageRating: Double,
    val bestValue: Product,
) {
    val priceSpread: Double get() = maxPrice - minPrice
}

/** Simple histogram bucket for the rating distribution chart. */
data class RatingBucket(val label: String, val count: Int)

object InsightsEngine {

    fun byCategory(products: List<Product>): List<CategoryInsight> =
        products.groupBy { it.category }
            .mapNotNull { (category, items) ->
                val best = items.maxByOrNull { it.dealScore() } ?: return@mapNotNull null
                CategoryInsight(
                    category = category,
                    count = items.size,
                    averagePrice = items.sumOf { it.price } / items.size,
                    minPrice = items.minOf { it.price },
                    maxPrice = items.maxOf { it.price },
                    averageRating = items.sumOf { it.rating } / items.size,
                    bestValue = best,
                )
            }
            .sortedByDescending { it.count }

    fun ratingHistogram(products: List<Product>): List<RatingBucket> {
        val edges = listOf(0.0 to 2.0, 2.0 to 3.0, 3.0 to 3.5, 3.5 to 4.0, 4.0 to 4.5, 4.5 to 5.01)
        val labels = listOf("<2", "2–3", "3–3.5", "3.5–4", "4–4.5", "4.5+")
        return edges.mapIndexed { i, (lo, hi) ->
            RatingBucket(labels[i], products.count { it.rating >= lo && it.rating < hi })
        }
    }

    /** Portfolio-level headline numbers for the top of the Insights screen. */
    fun overview(products: List<Product>): CatalogOverview = CatalogOverview(
        totalProducts = products.size,
        categories = products.map { it.category }.distinct().size,
        averagePrice = if (products.isEmpty()) 0.0 else round2(products.sumOf { it.price } / products.size),
        averageRating = if (products.isEmpty()) 0.0 else round2(products.sumOf { it.rating } / products.size),
        priciest = products.maxByOrNull { it.price },
        bestDeal = products.maxByOrNull { it.dealScore() },
    )

    private fun round2(v: Double) = round(v * 100) / 100
}

data class CatalogOverview(
    val totalProducts: Int,
    val categories: Int,
    val averagePrice: Double,
    val averageRating: Double,
    val priciest: Product?,
    val bestDeal: Product?,
)
