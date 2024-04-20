package ch.swisshomeguard.model.system


import com.google.gson.annotations.SerializedName

data class HomeguardSystem(
    @SerializedName("id") val id: Int,
    @SerializedName("nr") val nr: String,
    @SerializedName("urls") val urls: Urls,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: Address,
    @SerializedName("policeStation") val policeStation: PoliceStation,
    @SerializedName("cameras") val cameras: List<SystemDevice>,
    @SerializedName("systemUserRole") val systemUserRole: SystemUserRole,
) {
    override fun toString() = "$name [$nr]"
}