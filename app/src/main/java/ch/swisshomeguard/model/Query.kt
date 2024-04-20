package ch.swisshomeguard.model


import com.google.gson.annotations.SerializedName

data class Query(
    @SerializedName("page")
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("sort")
    val sort: String
)