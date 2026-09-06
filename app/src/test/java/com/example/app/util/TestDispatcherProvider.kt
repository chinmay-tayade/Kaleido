package com.example.app.util

import com.example.app.core.common.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

/**
 * [DispatcherProvider] that routes every dispatcher to a single [TestDispatcher]
 * so ViewModel / use-case tests run deterministically.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherProvider(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : DispatcherProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
}
