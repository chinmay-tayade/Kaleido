package com.kaleido.app

import kotlin.test.Test
import kotlin.test.assertTrue

class DealScoreTest {

    @Test
    fun cheaper_product_with_same_rating_scores_higher() {
        val cheap = product(1, price = 20.0, rating = 4.5, ratingCount = 500)
        val pricey = product(2, price = 200.0, rating = 4.5, ratingCount = 500)
        assertTrue(cheap.dealScore() > pricey.dealScore())
    }

    @Test
    fun better_rated_product_at_same_price_scores_higher() {
        val good = product(1, price = 50.0, rating = 4.8, ratingCount = 500)
        val meh = product(2, price = 50.0, rating = 3.0, ratingCount = 500)
        assertTrue(good.dealScore() > meh.dealScore())
    }

    @Test
    fun more_reviews_increase_confidence_and_score() {
        val trusted = product(1, price = 50.0, rating = 4.5, ratingCount = 2000)
        val unproven = product(2, price = 50.0, rating = 4.5, ratingCount = 5)
        assertTrue(trusted.dealScore() > unproven.dealScore())
    }

    @Test
    fun score_is_always_within_bounds() {
        listOf(
            product(1, price = 0.01, rating = 5.0, ratingCount = 100000),
            product(2, price = 99999.0, rating = 0.0, ratingCount = 0),
            product(3, price = 50.0, rating = 4.0, ratingCount = 100),
        ).forEach {
            val s = it.dealScore()
            assertTrue(s in 0..100, "score $s out of range")
        }
    }

    @Test
    fun topRated_requires_high_rating_and_enough_reviews() {
        assertTrue(product(1, rating = 4.4, ratingCount = 300).isTopRated)
        assertTrue(!product(2, rating = 4.4, ratingCount = 10).isTopRated)
        assertTrue(!product(3, rating = 3.9, ratingCount = 300).isTopRated)
    }
}
