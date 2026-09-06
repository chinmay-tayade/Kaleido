package com.example.app.data.repository

import com.example.app.domain.repository.Repository
import com.example.app.network.ApiService
import com.example.app.network.models.ProductDto
import javax.inject.Inject

class RepositoryImpl @Inject constructor(

    private val apiService: ApiService
) : Repository {
    override suspend fun getProducts(limit: Int?): List<ProductDto> {

        return  apiService.getProducts(limit)
    }

    override suspend fun getProduct(id: Int): ProductDto {

        return apiService.getProductById(id)
    }

}