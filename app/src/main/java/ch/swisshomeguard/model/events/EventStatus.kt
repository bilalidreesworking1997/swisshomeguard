package ch.swisshomeguard.model.events


import com.google.gson.annotations.SerializedName

data class EventStatus(
    @SerializedName("code")
    val code: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)