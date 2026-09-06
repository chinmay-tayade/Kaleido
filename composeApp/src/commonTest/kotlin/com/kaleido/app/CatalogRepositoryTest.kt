package com.kaleido.app

import app.cash.turbine.test
import com.kaleido.app.data.CatalogRepositoryImpl
import com.kaleido.app.data.local.CatalogCache
import com.kaleido.app.data.local.InMemoryKeyValueStore
import com.kaleido.app.data.remote.FakeStoreApi
import com.kaleido.app.data.remote.ProductDto
import com.kaleido.app.domain.DataStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val productsJson = """
        [
          {"id":1,"title":"Tee","price":10.0,"description":"d","category":"men's clothing","image":"i","rating":{"rate":4.5,"count":100}},
          {"id":2,"title":"Ring","price":200.0,"description":"d","category":"jewelery","image":"i","rating":{"rate":4.9,"count":900}}
        ]
    """.trimIndent()

    private fun api(handler: (String) -> Pair<HttpStatusCode, String>): FakeStoreApi {
        val engine = MockEngine { request ->
            val (code, body) = handler(request.url.encodedPath)
            respond(body, code, headersOf("Content-Type", "application/json"))
        }
        return FakeStoreApi(HttpClient(engine), json)
    }

    private fun cache(seed: List<ProductDto> = emptyList()): CatalogCache {
        val store = InMemoryKeyValueStore()
        return CatalogCache(store, json).also { if (seed.isNotEmpty()) it.write(seed) }
    }

    @Test
    fun warm_start_emits_cached_products_before_any_network_call() = runTest {
        val repo = CatalogRepositoryImpl(
            api { HttpStatusCode.OK to "[]" },
            cache(seed = listOf(dto(7, "Cached"))),
        )
        assertEquals(listOf(7), repo.products.value.map { it.id })
        assertEquals(DataStatus.Offline, repo.status.value)
    }

    @Test
    fun refresh_success_updates_products_and_marks_live() = runTest {
        val repo = CatalogRepositoryImpl(api { HttpStatusCode.OK to productsJson }, cache())
        repo.products.test {
            assertEquals(emptyList(), awaitItem())
            repo.refresh(limit = 20)
            assertEquals(listOf(1, 2), awaitItem().map { it.id })
        }
        assertEquals(DataStatus.Live, repo.status.value)
    }

    @Test
    fun refresh_failure_keeps_cached_data_and_flips_to_offline() = runTest {
        val repo = CatalogRepositoryImpl(
            api { HttpStatusCode.InternalServerError to "boom" },
            cache(seed = listOf(dto(7, "Cached"))),
        )
        repo.refresh()
        assertEquals(listOf(7), repo.products.value.map { it.id })
        assertEquals(DataStatus.Offline, repo.status.value)
    }

    @Test
    fun product_falls_back_to_network_when_not_in_cache() = runTest {
        val single = """{"id":9,"title":"One","price":5.0,"description":"d","category":"c","image":"i","rating":{"rate":4.0,"count":10}}"""
        val repo = CatalogRepositoryImpl(api { HttpStatusCode.OK to single }, cache())
        assertEquals("One", repo.product(9)?.title)
    }

    @Test
    fun product_failure_returns_null_rather_than_throwing() = runTest {
        val repo = CatalogRepositoryImpl(api { HttpStatusCode.NotFound to "nope" }, cache())
        assertTrue(repo.product(123) == null)
    }
}
