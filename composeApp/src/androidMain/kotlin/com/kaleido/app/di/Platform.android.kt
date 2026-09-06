package com.kaleido.app.di

import android.content.Context
import com.kaleido.app.data.local.KeyValueStore
import com.kaleido.app.data.local.SharedPrefsKeyValueStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<KeyValueStore> { SharedPrefsKeyValueStore(get<Context>()) }
}
