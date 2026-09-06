package com.example.app.network.models

import com.google.gson.annotations.SerializedName

data class ProductDto(

    @SerializedName("id")
    val id :Int,

    @SerializedName("category")
    val category: String ,

    @SerializedName("title")
    val title :String,

    @SerializedName("price")
    val price : Double,

    @SerializedName("description")
    val description : String,

    @SerializedName("image")
    val image: String,

    @SerializedName("rating")
    val rating : RatingDto?
)


