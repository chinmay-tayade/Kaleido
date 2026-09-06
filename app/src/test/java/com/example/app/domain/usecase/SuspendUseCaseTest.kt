package com.example.app.domain.usecase

import com.example.app.core.common.Result
import com.example.app.util.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SuspendUseCaseTest {

    private val dispatchers = TestDispatcherProvider()

    private val doubleIt = object : SuspendUseCase<Int, Int>(dispatchers) {
        override suspend fun execute(params: Int): Int {
            if (params < 0) error("negative")
            return params * 2
        }
    }

    @Test
    fun `returns Success with mapped value`() = runTest {
        val result = doubleIt(21)
        assertEquals(Result.Success(42), result)
    }

    @Test
    fun `wraps thrown exception in Error`() = runTest {
        val result = doubleIt(-1)
        assertTrue(result is Result.Error)
    }
}
