package ch.swisshomeguard.model.system


import com.google.gson.annotations.SerializedName

data class Address(
    @SerializedName("country")
    val country: Country,
    @SerializedName("email")
    val email: String,
    @SerializedName("first_last_name")
    val firstLastName: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("mobile")
    val mobile: Any,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("place")
    val place: String,
    @SerializedName("street")
    val street: String,
    @SerializedName("street_nr")
    val streetNr: String,
    @SerializedName("zip")
    val zip: String
)