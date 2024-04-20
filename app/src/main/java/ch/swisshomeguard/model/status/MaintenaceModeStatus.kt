package ch.swisshomeguard.model.status

import com.google.gson.annotations.SerializedName

data class MaintenaceModeStatus(
    @SerializedName("is_maintenance") val isMaintenanceEnabled: Boolean,
)