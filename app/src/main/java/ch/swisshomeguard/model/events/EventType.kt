package ch.swisshomeguard.model.events


import com.google.gson.annotations.SerializedName

data class EventType(
    @SerializedName("code")
    val code: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)