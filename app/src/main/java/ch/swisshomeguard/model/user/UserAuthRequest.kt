package ch.swisshomeguard.model.user

import com.google.gson.annotations.SerializedName

data class UserAuthRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("language") val language: String
)