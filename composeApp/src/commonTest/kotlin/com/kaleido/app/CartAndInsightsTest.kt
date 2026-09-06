package com.kaleido.app

import com.kaleido.app.domain.CartLine
import com.kaleido.app.domain.CartSummary
import com.kaleido.app.domain.InsightsEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CartAndInsightsTest {

    @Test
    fun cart_summary_totals_items_and_subtotal() {
        val lines = listOf(
            CartLine(product(1, price = 10.0), 2),
            CartLine(product(2, price = 5.5), 3),
        )
        val summary = CartSummary.from(lines)
        assertEquals(5, summary.itemCount)
        assertEquals(36.5, summary.subtotal)
    }

    @Test
    fun cart_savings_only_apply_to_strong_deals() {
        val strongDeal = product(1, price = 20.0, rating = 4.9, ratingCount = 5000) // high deal score
        val weakDeal = product(2, price = 500.0, rating = 3.0, ratingCount = 10)
        assertTrue(strongDeal.dealScore() >= 70)
        assertTrue(weakDeal.dealScore() < 70)

        val summary = CartSummary.from(listOf(CartLine(strongDeal, 1), CartLine(weakDeal, 1)))
        assertEquals(strongDeal.price * 0.10, summary.savingsFromDeals, absoluteTolerance = 0.0001)
    }

    @Test
    fun insights_overview_computes_averages_and_extremes() {
        val overview = InsightsEngine.overview(sampleProducts)
        assertEquals(5, overview.totalProducts)
        assertEquals(3, overview.categories)
        assertEquals(2, overview.priciest?.id) // gold ring at 695
    }

    @Test
    fun insights_rating_histogram_counts_every_product_once() {
        val total = InsightsEngine.ratingHistogram(sampleProducts).sumOf { it.count }
        assertEquals(sampleProducts.size, total)
    }

    @Test
    fun insights_by_category_picks_best_value_per_group() {
        val jewelery = InsightsEngine.byCategory(sampleProducts).first { it.category == "jewelery" }
        assertEquals(2, jewelery.count)
        // bracelet (id 5) is far cheaper -> better deal score than the 695 ring
        assertEquals(5, jewelery.bestValue.id)
    }
}
