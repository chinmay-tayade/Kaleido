package com.kaleido.app.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * Offline-first catalog access.
 *
 * [products] is a hot stream backed by the local cache: collectors get the last
 * known catalog immediately (even with no network), and again whenever a
 * [refresh] succeeds. [DataStatus] rides alongside so the UI can show a
 * "showing saved copy" banner without inspecting exceptions.
 */
interface CatalogRepository {
    val products: StateFlow<List<Product>>
    val status: StateFlow<DataStatus>

    /** Pull the newest catalog from the network and update the cache. */
    suspend fun refresh(limit: Int = 20)

    /** Single product — cache first, network fallback. */
    suspend fun product(id: Int): Product?
}

enum class DataStatus {
    /** Never loaded anything yet. */
    Idle,

    /** A network call is in flight. */
    Loading,

    /** Showing data confirmed fresh from the network. */
    Live,

    /** Showing cached data because the last refresh failed. */
    Offline,
}
