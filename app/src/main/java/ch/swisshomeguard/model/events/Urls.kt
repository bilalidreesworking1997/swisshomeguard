package ch.swisshomeguard.model.events


import com.google.gson.annotations.SerializedName

data class Urls(
    @SerializedName("preview") val preview: String?,
    @SerializedName("stream") val stream: String?,
)