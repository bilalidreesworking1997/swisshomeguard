package ch.swisshomeguard.model.user

import com.google.gson.annotations.SerializedName

data class UserAuthResponse(
    @SerializedName("access_token") val access_token: String,
    @SerializedName("token_type") val token_type: String,
    @SerializedName("expires_in") val expires_in: Int,
    @SerializedName("refresh_in") val refresh_in: Int,
    @SerializedName("user") val user: User,
    @SerializedName("now") val now: String,
    @SerializedName("expires_on") val expires_on: String,
    @SerializedName("refresh_on") val refresh_on: String
)