package ch.swisshomeguard.model


import com.google.gson.annotations.SerializedName

data class Paginator(
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("first_page_url")
    val firstPageUrl: String,
    @SerializedName("from")
    val from: Int,
    @SerializedName("next_page_url")
    val nextPageUrl: Any,
    @SerializedName("page_total")
    val pageTotal: Int,
    @SerializedName("param_names")
    val paramNames: ParamNames,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("prev_page_url")
    val prevPageUrl: Any,
    @SerializedName("to")
    val to: Int,
    @SerializedName("total")
    val total: Int
)