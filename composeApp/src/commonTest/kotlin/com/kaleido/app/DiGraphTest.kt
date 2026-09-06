package com.kaleido.app

import com.kaleido.app.data.local.InMemoryKeyValueStore
import com.kaleido.app.data.local.KeyValueStore
import com.kaleido.app.di.coreModule
import com.kaleido.app.di.viewModelModule
import com.kaleido.app.domain.CatalogRepository
import com.kaleido.app.ui.catalog.CatalogViewModel
import com.kaleido.app.ui.compare.CompareViewModel
import com.kaleido.app.ui.insights.InsightsViewModel
import com.kaleido.app.ui.store.ShelfViewModel
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class DiGraphTest {

    private val fakePlatformModule = module {
        single<HttpClientEngine> { MockEngine { respond("[]", HttpStatusCode.OK) } }
        single<KeyValueStore> { InMemoryKeyValueStore() }
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun every_declared_dependency_can_be_resolved() {
        val koin = startKoin {
            modules(fakePlatformModule, coreModule, viewModelModule)
        }.koin

        assertNotNull(koin.get<CatalogRepository>())
        assertNotNull(koin.get<ShelfViewModel>())
        assertNotNull(koin.get<CatalogViewModel>())
        assertNotNull(koin.get<CompareViewModel>())
        assertNotNull(koin.get<InsightsViewModel>())
        assertNotNull(koin.get<com.kaleido.app.ui.detail.DetailViewModel> { parametersOf(1) })
    }
}
