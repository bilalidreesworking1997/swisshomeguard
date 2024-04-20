package ch.swisshomeguard.model.system


import ch.swisshomeguard.model.Paginator
import ch.swisshomeguard.model.Query
import com.google.gson.annotations.SerializedName

data class SystemsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("query") val query: Query,
    @SerializedName("paginator") val paginator: Paginator,
    @SerializedName("data") val systems: List<HomeguardSystem>
)