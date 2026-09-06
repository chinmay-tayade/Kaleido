package com.kaleido.app

import com.kaleido.app.data.remote.ProductDto
import com.kaleido.app.domain.Product

fun product(
    id: Int,
    title: String = "Product $id",
    price: Double = 20.0,
    category: String = "general",
    rating: Double = 4.0,
    ratingCount: Int = 100,
    description: String = "desc $id",
) = Product(id, title, price, description, category, "https://img/$id.png", rating, ratingCount)

fun dto(
    id: Int,
    title: String = "Product $id",
    price: Double = 20.0,
    category: String = "general",
    rate: Double = 4.0,
    count: Int = 100,
) = ProductDto(id, title, price, "desc $id", category, "https://img/$id.png", rate, count)

val sampleProducts: List<Product> = listOf(
    product(1, "Cheap tee", price = 10.0, category = "men's clothing", rating = 4.6, ratingCount = 400),
    product(2, "Gold ring", price = 695.0, category = "jewelery", rating = 4.6, ratingCount = 400),
    product(3, "SSD drive", price = 64.0, category = "electronics", rating = 4.8, ratingCount = 900),
    product(4, "Slim jacket", price = 56.0, category = "men's clothing", rating = 2.1, ratingCount = 30),
    product(5, "Bracelet", price = 9.0, category = "jewelery", rating = 3.9, ratingCount = 70),
)
