package ch.swisshomeguard.model.user

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("email_verified_at") val email_verified_at: String,
    @SerializedName("first_name") val first_name: String,
    @SerializedName("last_name") val last_name: String,
    @SerializedName("phone_nr") val phone_nr: String,
    @SerializedName("is_admin") val is_admin: Boolean,
    @SerializedName("first_last_name") val first_last_name: String
)