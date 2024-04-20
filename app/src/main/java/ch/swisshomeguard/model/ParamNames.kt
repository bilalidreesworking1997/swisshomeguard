package ch.swisshomeguard.model


import com.google.gson.annotations.SerializedName

data class ParamNames(
    @SerializedName("page")
    val page: String,
    @SerializedName("per_page")
    val perPage: String
)