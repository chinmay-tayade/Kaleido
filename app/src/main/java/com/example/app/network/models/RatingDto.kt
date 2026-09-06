package com.example.app.network.models

import com.google.gson.annotations.SerializedName

data class RatingDto(
    @SerializedName("rate")
    val rate : Double,

    @SerializedName("count")
    val count : Int
)