package com.kaleido.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Thin typed wrapper over the public Fake Store API (no auth). Parsing is done by
 * hand from the JSON tree — see [ProductDto] for why we avoid `@Serializable`.
 */
class FakeStoreApi(
    private val client: HttpClient,
    private val json: Json,
    private val baseUrl: String = "https://fakestoreapi.com",
) {
    suspend fun products(limit: Int? = null): List<ProductDto> {
        val body = client.get(baseUrl) {
            url { appendPathSegments("products") }
            if (limit != null) parameter("limit", limit)
        }.bodyAsText()
        return decodeProducts(json, body)
    }

    suspend fun product(id: Int): ProductDto {
        val body = client.get(baseUrl) {
            url { appendPathSegments("products", id.toString()) }
        }.bodyAsText()
        return json.parseToJsonElement(body).toProductDto()
    }

    suspend fun categories(): List<String> {
        val body = client.get(baseUrl) {
            url { appendPathSegments("products", "categories") }
        }.bodyAsText()
        return json.parseToJsonElement(body).jsonArray.map { it.jsonPrimitive.content }
    }
}
