package com.kaleido.app.data

import com.kaleido.app.data.local.CatalogCache
import com.kaleido.app.data.remote.FakeStoreApi
import com.kaleido.app.data.remote.ProductDto
import com.kaleido.app.data.remote.toDomain
import com.kaleido.app.domain.CatalogRepository
import com.kaleido.app.domain.DataStatus
import com.kaleido.app.domain.Product
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Offline-first implementation:
 *
 *  1. On construction the cached catalog is emitted synchronously — a warm start
 *     never shows a blank spinner.
 *  2. [refresh] hits the network; on success the cache + stream update and
 *     status becomes [DataStatus.Live]; on failure we keep serving the cache and
 *     flip status to [DataStatus.Offline].
 */
class CatalogRepositoryImpl(
    private val api: FakeStoreApi,
    private val cache: CatalogCache,
) : CatalogRepository {

    private val _products = MutableStateFlow(cache.read().map(ProductDto::toDomain))
    override val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _status = MutableStateFlow(
        if (_products.value.isEmpty()) DataStatus.Idle else DataStatus.Offline,
    )
    override val status: StateFlow<DataStatus> = _status.asStateFlow()

    override suspend fun refresh(limit: Int) {
        _status.value = DataStatus.Loading
        try {
            val fresh: List<ProductDto> = api.products(limit).distinctBy { it.id }
            cache.write(fresh)
            _products.value = fresh.map(ProductDto::toDomain)
            _status.value = DataStatus.Live
        } catch (t: CancellationException) {
            throw t
        } catch (_: Throwable) {
            _status.value = if (_products.value.isEmpty()) DataStatus.Idle else DataStatus.Offline
        }
    }

    override suspend fun product(id: Int): Product? {
        _products.value.firstOrNull { it.id == id }?.let { return it }
        return try {
            api.product(id).toDomain()
        } catch (t: CancellationException) {
            throw t
        } catch (_: Throwable) {
            null
        }
    }
}
