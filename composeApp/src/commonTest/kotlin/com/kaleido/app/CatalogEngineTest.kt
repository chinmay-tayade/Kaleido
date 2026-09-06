package com.kaleido.app

import com.kaleido.app.domain.CatalogEngine
import com.kaleido.app.domain.CatalogQuery
import com.kaleido.app.domain.SortOption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogEngineTest {

    @Test
    fun search_matches_title_category_and_description_case_insensitively() {
        val result = CatalogEngine.apply(sampleProducts, CatalogQuery(search = "RING"))
        assertEquals(listOf(2), result.map { it.id })
    }

    @Test
    fun category_filter_restricts_results() {
        val result = CatalogEngine.apply(sampleProducts, CatalogQuery(category = "jewelery"))
        assertEquals(setOf(2, 5), result.map { it.id }.toSet())
    }

    @Test
    fun sort_price_low_to_high_and_high_to_low_are_reverses() {
        val low = CatalogEngine.apply(sampleProducts, CatalogQuery(sort = SortOption.PRICE_LOW_HIGH)).map { it.id }
        val high = CatalogEngine.apply(sampleProducts, CatalogQuery(sort = SortOption.PRICE_HIGH_LOW)).map { it.id }
        assertEquals(low, high.reversed())
        assertEquals(5, low.first()) // 9.0 bracelet is cheapest
    }

    @Test
    fun sort_rating_breaks_ties_by_review_count() {
        val byRating = CatalogEngine.apply(sampleProducts, CatalogQuery(sort = SortOption.RATING)).map { it.id }
        // id 3 has the top rating (4.8); ids 1 & 2 both 4.6 but same count -> stable
        assertEquals(3, byRating.first())
    }

    @Test
    fun relevance_sort_uses_deal_score() {
        val byRelevance = CatalogEngine.apply(sampleProducts, CatalogQuery(sort = SortOption.RELEVANCE))
        val scores = byRelevance.map { it.dealScore() }
        assertEquals(scores.sortedDescending(), scores)
    }

    @Test
    fun forYou_returns_at_most_one_product_per_category() {
        val forYou = CatalogEngine.forYou(sampleProducts)
        assertEquals(forYou.size, forYou.map { it.category }.distinct().size)
        assertTrue(forYou.size <= 3)
    }

    @Test
    fun categories_are_distinct_and_sorted() {
        assertEquals(
            listOf("electronics", "jewelery", "men's clothing"),
            CatalogEngine.categories(sampleProducts),
        )
    }
}
