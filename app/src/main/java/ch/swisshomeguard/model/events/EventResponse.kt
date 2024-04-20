package ch.swisshomeguard.model.events


import ch.swisshomeguard.model.Paginator
import ch.swisshomeguard.model.Query
import com.google.gson.annotations.SerializedName

data class EventResponse(
    @SerializedName("data")
    val events: List<Event>,
    @SerializedName("paginator")
    val paginator: Paginator,
    @SerializedName("query")
    val query: Query,
    @SerializedName("success")
    val success: Boolean
)