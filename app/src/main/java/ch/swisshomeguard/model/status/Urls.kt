package ch.swisshomeguard.model.status


import com.google.gson.annotations.SerializedName

data class Urls(
    @SerializedName("event")
    val event: String,
    @SerializedName("system")
    val system: String
)