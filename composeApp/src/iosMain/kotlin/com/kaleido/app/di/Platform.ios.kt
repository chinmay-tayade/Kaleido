package com.kaleido.app.di

import com.kaleido.app.data.local.KeyValueStore
import com.kaleido.app.data.local.UserDefaultsKeyValueStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single<KeyValueStore> { UserDefaultsKeyValueStore() }
}
