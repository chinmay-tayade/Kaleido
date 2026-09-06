package com.kaleido.app.data.remote

import com.kaleido.app.domain.Product
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Plain network model, parsed by hand from [JsonElement].
 *
 * We deliberately don't put `@Serializable` on this class: the kotlinx.serialization
 * compiler plugin currently crashes generating the `$serializer` for it on the
 * Kotlin/Wasm target, and manual mapping over a handful of fields is cheap,
 * explicit and identical on every platform.
 */
data class ProductDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val ratingRate: Double,
    val ratingCount: Int,
)

private fun JsonObject.str(key: String): String = this[key]?.jsonPrimitive?.content.orEmpty()
private fun JsonObject.dbl(key: String): Double = this[key]?.jsonPrimitive?.double ?: 0.0
private fun JsonObject.integer(key: String): Int = this[key]?.jsonPrimitive?.int ?: 0

fun JsonElement.toProductDto(): ProductDto {
    val obj = jsonObject
    val rating = obj["rating"]?.jsonObject
    return ProductDto(
        id = obj.integer("id"),
        title = obj.str("title"),
        price = obj.dbl("price"),
        description = obj.str("description"),
        category = obj.str("category"),
        image = obj.str("image"),
        ratingRate = rating?.dbl("rate") ?: 0.0,
        ratingCount = rating?.integer("count") ?: 0,
    )
}

fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title.trim(),
    price = price,
    description = description.trim(),
    category = category.trim(),
    imageUrl = image,
    rating = ratingRate,
    ratingCount = ratingCount,
)

fun ProductDto.toJson(): JsonObject = buildJsonObject {
    put("id", id)
    put("title", title)
    put("price", price)
    put("description", description)
    put("category", category)
    put("image", image)
    put("rating", buildJsonObject {
        put("rate", ratingRate)
        put("count", ratingCount)
    })
}

fun List<ProductDto>.encode(json: Json): String {
    val array: JsonArray = buildJsonArray { this@encode.forEach { add(it.toJson()) } }
    return json.encodeToString(JsonArray.serializer(), array)
}

fun decodeProducts(json: Json, raw: String): List<ProductDto> =
    json.parseToJsonElement(raw).jsonArray.map { it.toProductDto() }
