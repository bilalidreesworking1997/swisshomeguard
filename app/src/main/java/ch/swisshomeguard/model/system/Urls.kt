package ch.swisshomeguard.model.system


import com.google.gson.annotations.SerializedName

data class Urls(
    @SerializedName("system")
    val system: String
)