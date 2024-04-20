package ch.swisshomeguard.model.system


import com.google.gson.annotations.SerializedName

const val administrator = 1
const val user = 2
const val basicUser = 3

data class SystemUserRole(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
) {
    fun isBasicUser(): Boolean = id == basicUser
}