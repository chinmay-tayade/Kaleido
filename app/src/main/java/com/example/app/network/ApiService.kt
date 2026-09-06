package com.example.app.network

import com.example.app.network.models.ProductDto
import com.google.gson.internal.NumberLimits
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit : Int? = null
    ) : List<ProductDto>


    @GET("product/{id}")
    suspend fun getProductById(

        @Path("id") id : Int
    ) : ProductDto
}