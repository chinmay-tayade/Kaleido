package com.example.app.domain.repository

import com.example.app.network.models.ProductDto

interface Repository{

    suspend fun getProducts(limit :Int? = null) : List<ProductDto>
    suspend fun getProduct(id:Int): ProductDto
}