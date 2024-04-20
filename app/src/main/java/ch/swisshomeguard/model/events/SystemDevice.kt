package ch.swisshomeguard.model.events


import com.google.gson.annotations.SerializedName

data class SystemDevice(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("urls") val urls: UrlsX,
    @SerializedName("isDetectingNow") val isDetectingNow: Boolean?
)