package com.kaleido.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * The user's personal, fully-offline state: wishlist, cart quantities, recently
 * viewed, and compare selection. Every mutation is written straight back to
 * [KeyValueStore], so it survives process death and airplane mode alike.
 */
class ShelfStore(
    private val store: KeyValueStore,
    private val json: Json,
) {
    // Declared first: the state flows below read through these during init.
    private val listSerializer = ListSerializer(Int.serializer())
    private val mapSerializer = MapSerializer(Int.serializer(), Int.serializer())

    private val _wishlist = MutableStateFlow(readIntSet(WISHLIST))
    val wishlist: StateFlow<Set<Int>> = _wishlist.asStateFlow()

    private val _cart = MutableStateFlow(readIntMap(CART))
    /** productId -> quantity */
    val cart: StateFlow<Map<Int, Int>> = _cart.asStateFlow()

    private val _recentlyViewed = MutableStateFlow(readIntList(RECENTS))
    val recentlyViewed: StateFlow<List<Int>> = _recentlyViewed.asStateFlow()

    private val _compare = MutableStateFlow(readIntList(COMPARE))
    val compare: StateFlow<List<Int>> = _compare.asStateFlow()

    fun toggleWishlist(id: Int) {
        _wishlist.value = _wishlist.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
        writeIntSet(WISHLIST, _wishlist.value)
    }

    fun setCartQuantity(id: Int, quantity: Int) {
        _cart.value = _cart.value.toMutableMap().apply {
            if (quantity <= 0) remove(id) else put(id, quantity.coerceAtMost(MAX_QTY))
        }
        writeIntMap(CART, _cart.value)
    }

    fun addToCart(id: Int, delta: Int = 1) =
        setCartQuantity(id, (_cart.value[id] ?: 0) + delta)

    fun clearCart() {
        _cart.value = emptyMap()
        store.remove(CART)
    }

    fun recordView(id: Int) {
        _recentlyViewed.value = (listOf(id) + _recentlyViewed.value.filter { it != id }).take(MAX_RECENTS)
        writeIntList(RECENTS, _recentlyViewed.value)
    }

    fun toggleCompare(id: Int) {
        val current = _compare.value
        _compare.value = when {
            id in current -> current - id
            current.size >= MAX_COMPARE -> current.drop(1) + id
            else -> current + id
        }
        writeIntList(COMPARE, _compare.value)
    }

    fun clearCompare() {
        _compare.value = emptyList()
        store.remove(COMPARE)
    }

    // --- serialization helpers -------------------------------------------------

    private fun readIntSet(key: String) = readIntList(key).toSet()
    private fun readIntList(key: String): List<Int> =
        store.getString(key)?.let { runCatching { json.decodeFromString(listSerializer, it) }.getOrNull() } ?: emptyList()

    private fun readIntMap(key: String): Map<Int, Int> =
        store.getString(key)?.let { runCatching { json.decodeFromString(mapSerializer, it) }.getOrNull() } ?: emptyMap()

    private fun writeIntSet(key: String, value: Set<Int>) = store.putString(key, json.encodeToString(listSerializer, value.toList()))
    private fun writeIntList(key: String, value: List<Int>) = store.putString(key, json.encodeToString(listSerializer, value))
    private fun writeIntMap(key: String, value: Map<Int, Int>) = store.putString(key, json.encodeToString(mapSerializer, value))

    private companion object {
        const val WISHLIST = "kaleido.wishlist.v1"
        const val CART = "kaleido.cart.v1"
        const val RECENTS = "kaleido.recents.v1"
        const val COMPARE = "kaleido.compare.v1"
        const val MAX_RECENTS = 12
        const val MAX_COMPARE = 3
        const val MAX_QTY = 99
    }
}
