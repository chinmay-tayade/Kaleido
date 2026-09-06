package com.kaleido.app

import com.kaleido.app.data.local.InMemoryKeyValueStore
import com.kaleido.app.data.local.ShelfStore
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShelfStoreTest {

    private val json = Json

    private fun store(backing: InMemoryKeyValueStore = InMemoryKeyValueStore()) =
        ShelfStore(backing, json) to backing

    @Test
    fun wishlist_toggles_on_and_off() {
        val (shelf, _) = store()
        shelf.toggleWishlist(3)
        assertEquals(setOf(3), shelf.wishlist.value)
        shelf.toggleWishlist(3)
        assertTrue(shelf.wishlist.value.isEmpty())
    }

    @Test
    fun cart_quantity_is_clamped_and_zero_removes_the_line() {
        val (shelf, _) = store()
        shelf.addToCart(1)
        shelf.addToCart(1)
        assertEquals(2, shelf.cart.value[1])
        shelf.setCartQuantity(1, 0)
        assertTrue(shelf.cart.value.isEmpty())
        shelf.setCartQuantity(2, 5000)
        assertEquals(99, shelf.cart.value[2])
    }

    @Test
    fun compare_selection_keeps_at_most_three_newest() {
        val (shelf, _) = store()
        listOf(1, 2, 3, 4).forEach(shelf::toggleCompare)
        assertEquals(listOf(2, 3, 4), shelf.compare.value)
    }

    @Test
    fun recently_viewed_is_most_recent_first_and_deduped() {
        val (shelf, _) = store()
        shelf.recordView(1)
        shelf.recordView(2)
        shelf.recordView(1)
        assertEquals(listOf(1, 2), shelf.recentlyViewed.value)
    }

    @Test
    fun state_survives_a_fresh_instance_on_the_same_backing_store() {
        val backing = InMemoryKeyValueStore()
        store(backing).first.apply {
            toggleWishlist(7)
            addToCart(9)
        }
        val (reloaded, _) = store(backing)
        assertEquals(setOf(7), reloaded.wishlist.value)
        assertEquals(1, reloaded.cart.value[9])
    }
}
