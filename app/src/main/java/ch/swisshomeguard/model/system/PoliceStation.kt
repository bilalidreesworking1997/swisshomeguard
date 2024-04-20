package ch.swisshomeguard.model.system


import com.google.gson.annotations.SerializedName

data class PoliceStation(
    @SerializedName("description")
    val description: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("phone_nr")
    val phoneNr: String
)