package ch.swisshomeguard.model.events


import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("id") val id: Int,
    @SerializedName("event_status_id") val eventStatusId: Int,
    @SerializedName("system_device_id") val systemDeviceId: Int,
    @SerializedName("detection_type_name") val detectionTypeName: String,
    @SerializedName("event_created_at") val eventCreatedAt: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("systemDevice") val systemDevice: SystemDevice,
    @SerializedName("eventRecordings") val eventRecordings: List<EventRecording>,
    @SerializedName("eventStatus") val eventStatus: EventStatus,
    @SerializedName("eventType") val eventType: EventType,
    @SerializedName("event_type_id") val eventTypeId: Int
)