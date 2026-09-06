package com.kaleido.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductPricingTest {

    @Test
    fun discount_percent_stays_in_a_believable_range() {
        (1..20).forEach { id ->
            val p = product(id = id, price = 10.0 + id, rating = 3.0 + id % 3, ratingCount = id * 40)
            assertTrue(p.discountPercent in 5..70, "id=$id -> ${p.discountPercent}%")
        }
    }

    @Test
    fun list_price_is_always_above_the_sale_price_and_savings_are_positive() {
        (1..20).forEach { id ->
            val p = product(id = id, price = 5.0 * id, rating = 4.0, ratingCount = 200)
            assertTrue(p.listPrice > p.price, "id=$id list=${p.listPrice} price=${p.price}")
            assertTrue(p.savings > 0.0)
        }
    }

    @Test
    fun pricing_is_deterministic_for_the_same_product() {
        val a = product(id = 7, price = 42.0, rating = 4.4, ratingCount = 333)
        val b = product(id = 7, price = 42.0, rating = 4.4, ratingCount = 333)
        assertEquals(a.discountPercent, b.discountPercent)
        assertEquals(a.listPrice, b.listPrice)
    }

    @Test
    fun a_stronger_deal_score_implies_a_deeper_headline_discount() {
        val greatDeal = product(id = 2, price = 15.0, rating = 4.9, ratingCount = 5000)
        val poorDeal = product(id = 2, price = 400.0, rating = 2.5, ratingCount = 20)
        assertTrue(greatDeal.dealScore() > poorDeal.dealScore())
        assertTrue(greatDeal.discountPercent > poorDeal.discountPercent)
    }
}
