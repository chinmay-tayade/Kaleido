package com.kaleido.app.di

import com.kaleido.app.data.CatalogRepositoryImpl
import com.kaleido.app.data.local.CatalogCache
import com.kaleido.app.data.local.ShelfStore
import com.kaleido.app.data.remote.FakeStoreApi
import com.kaleido.app.domain.CatalogRepository
import com.kaleido.app.ui.catalog.CatalogViewModel
import com.kaleido.app.ui.compare.CompareViewModel
import com.kaleido.app.ui.detail.DetailViewModel
import com.kaleido.app.ui.insights.InsightsViewModel
import com.kaleido.app.ui.store.ShelfViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/** Engine + [com.kaleido.app.data.local.KeyValueStore] come from the platform. */
expect val platformModule: Module

val coreModule: Module = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
            coerceInputValues = true
        }
    }
    single {
        HttpClient(get<HttpClientEngine>()) {
            expectSuccess = true
            install(ContentNegotiation) { json(get<Json>()) }
            install(Logging) { level = LogLevel.INFO }
        }
    }
    single { FakeStoreApi(get(), get()) }
    single { CatalogCache(get(), get()) }
    single { ShelfStore(get(), get()) }
    single<CatalogRepository> { CatalogRepositoryImpl(get(), get()) }
}

val viewModelModule: Module = module {
    single { ShelfViewModel(get(), get()) }
    viewModel { CatalogViewModel(get()) }
    viewModel { (id: Int) -> DetailViewModel(id, get()) }
    viewModel { CompareViewModel(get(), get()) }
    viewModel { InsightsViewModel(get()) }
}

private var koinStarted = false

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    if (koinStarted) return
    koinStarted = true
    startKoin {
        appDeclaration()
        modules(platformModule, coreModule, viewModelModule)
    }
}
