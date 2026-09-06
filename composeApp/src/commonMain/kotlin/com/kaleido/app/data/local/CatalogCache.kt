package com.kaleido.app.data.local

import com.kaleido.app.data.remote.ProductDto
import com.kaleido.app.data.remote.decodeProducts
import com.kaleido.app.data.remote.encode
import kotlinx.serialization.json.Json

/** Persists the last successfully fetched catalog so the app opens instantly and works offline. */
class CatalogCache(
    private val store: KeyValueStore,
    private val json: Json,
) {
    fun read(): List<ProductDto> {
        val raw = store.getString(KEY) ?: return emptyList()
        return runCatching { decodeProducts(json, raw) }.getOrDefault(emptyList())
    }

    fun write(products: List<ProductDto>) {
        store.putString(KEY, products.encode(json))
    }

    private companion object {
        const val KEY = "kaleido.catalog.v1"
    }
}
