package com.kaleido.app.di

import com.kaleido.app.data.local.KeyValueStore
import com.kaleido.app.data.local.LocalStorageKeyValueStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { Js.create() }
    single<KeyValueStore> { LocalStorageKeyValueStore() }
}
