package com.kaleido.app.domain

import kotlin.math.roundToInt

/**
 * Domain model for a catalog product. Pure Kotlin — no serialization or platform
 * types leak in here. Mapped from [com.kaleido.app.data.remote.ProductDto].
 */
data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val imageUrl: String,
    val rating: Double,
    val ratingCount: Int,
) {
    /**
     * "Deal score" 0..100 — a single, explainable number that rewards products
     * that are both well-rated and well-reviewed while punishing a high price.
     * Used for the "Best value" badge and the "For You" ranking.
     *
     *  score = ratingWeight * priceValue
     *    ratingWeight = rating/5 damped by review count (Bayesian-ish shrink)
     *    priceValue   = 1 / (1 + price / referencePrice)
     */
    fun dealScore(referencePrice: Double = REFERENCE_PRICE): Int {
        val confidence = ratingCount.toDouble() / (ratingCount + REVIEW_SHRINK)
        val ratingWeight = (rating / 5.0) * (0.35 + 0.65 * confidence)
        val priceValue = 1.0 / (1.0 + (price / referencePrice).coerceAtLeast(0.0))
        return (ratingWeight * priceValue * 140.0).coerceIn(0.0, 100.0).toInt()
    }

    val isTopRated: Boolean get() = rating >= 4.3 && ratingCount >= 200

    /**
     * The Fake Store API only gives a single price, but shoppers expect an
     * "MRP / list price" struck through with a discount %. We synthesise one
     * deterministically: the better the deal score, the deeper the headline
     * discount — so the badges still mean something.
     */
    val discountPercent: Int
        get() {
            val fromScore = 6 + (dealScore() * 0.52) // 6%..~58%
            val jitter = (id * 7 % 9) - 4            // ±4, stable per product
            return (fromScore + jitter).roundToInt().coerceIn(5, 70)
        }

    /** Struck-through "was" price implied by [discountPercent]. */
    val listPrice: Double
        get() {
            val raw = price / (1.0 - discountPercent / 100.0)
            // round up to a .99 so it reads like a real tag
            return (raw - 0.01).let { kotlin.math.ceil(it) } - 0.01
        }

    val savings: Double get() = (listPrice - price).coerceAtLeast(0.0)

    companion object {
        const val REFERENCE_PRICE = 60.0
        const val REVIEW_SHRINK = 60.0
    }
}
