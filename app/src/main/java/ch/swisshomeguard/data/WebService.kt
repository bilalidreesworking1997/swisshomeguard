package ch.swisshomeguard.data

import android.util.Log
import ch.swisshomeguard.BASE_URL
import ch.swisshomeguard.BASE_URL_API
import ch.swisshomeguard.TOKEN_REFRESH_ERROR
import ch.swisshomeguard.TOKEN_TAG
import ch.swisshomeguard.model.events.EventResponse
import ch.swisshomeguard.model.notifications.NotificationEnabledSet
import ch.swisshomeguard.model.notifications.NotificationEnabledStatusResponse
import ch.swisshomeguard.model.notifications.NotificationSetup
import ch.swisshomeguard.model.player.VideoChannelResponse
import ch.swisshomeguard.model.status.*
import ch.swisshomeguard.model.system.SystemsResponse
import ch.swisshomeguard.model.user.ForgotPasswordRequest
import ch.swisshomeguard.model.user.UserAuthRequest
import ch.swisshomeguard.model.user.UserAuthResponse
import ch.swisshomeguard.utils.HomeguardTokenUtils
import com.google.gson.JsonElement
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.IOException

interface LoginService {

    @POST("auth/login")
    suspend fun login(@Body body: UserAuthRequest): UserAuthResponse

    @POST("password/email")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): JsonElement

    companion object {
        fun create(): LoginService {
            val builder = OkHttpClient.Builder()
            val client = builder.build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL_API)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LoginService::class.java)
        }
    }
}

interface WebService {

    @GET("user/system")
    suspend fun fetchSystems(): SystemsResponse

    @GET("user/system/{systemId}/event")
    suspend fun fetchEvents(
        @Path("systemId") systemId: Int,
        @Query("page") page: Int,
        @Query("filter[eventFrom]") eventFrom: String? = null,
        @Query("filter[eventTo]") eventTo: String? = null,
        @Query("filter[systemDevice.id]") systemDeviceIds: String? = null,
        @Query("sort") sort: String? = "-event_created_at"
    ): EventResponse

    @GET("user/system/{systemId}/event")
    suspend fun fetchEventDetails(
        @Path("systemId") systemId: Int,
        @Query("filter[id]") eventIds: String? = null
    ): EventResponse

    @GET("user/system/armed/{systemId}")
    suspend fun fetchSystemStatus(@Path("systemId") systemId: Int): SystemStatusResponse

    @PUT("user/system/armed/{systemId}")
    suspend fun setRecordingStatus(
        @Path("systemId") systemId: Int,
        @Body systemStatus: RecordingStatus
    ): SystemStatusResponse

    @PUT("user/system/armed/{systemId}")
    suspend fun setAlarmCentralStatus(
        @Path("systemId") systemId: Int,
        @Body systemStatus: AlarmCentralStatus
    ): SystemStatusResponse

    @PUT("user/system/armed/{systemId}")
    suspend fun setAlarmSignalStatus(
        @Path("systemId") systemId: Int,
        @Body systemStatus: AlarmSignalStatus
    ): SystemStatusResponse

    @PUT("user/system/armed/{systemId}")
    suspend fun setCalendarStatus(
        @Path("systemId") systemId: Int,
        @Body scheduleStatus: ScheduleStatus,
    ): SystemStatusResponse

    @PUT("user/system/armed/{systemId}")
    suspend fun setMaintenanceModeStatus(
        @Path("systemId") systemId: Int,
        @Body systemStatus: MaintenaceModeStatus
    ): SystemStatusResponse

    @POST("user/notification")
    suspend fun storeFirebaseToken(@Body notificationSetup: NotificationSetup)

    @DELETE("user/notification/{firebaseToken}")
    suspend fun deleteFirebaseToken(@Path("firebaseToken") firebaseToken: String)

    @GET("user/setting")
    suspend fun fetchNotificationStatus(): NotificationEnabledStatusResponse

    @PUT("user/setting")
    suspend fun setNotificationStatus(@Body notificationEnabledSet: NotificationEnabledSet)

    //    @GET("$BASE_URL/{streamChannelUrl}")
//    suspend fun fetchVideo(@Path("streamChannelUrl") streamChannelUrl: String): VideoChannelResponse

    @GET("user/stream/video/{id}/channel/2")
    suspend fun fetchVideo(@Path("id") streamChannelUrl: String): VideoChannelResponse

//    @GET("$BASE_URL/{keepAliveUrl}")
//    suspend fun keepVideoAlive(@Path("keepAliveUrl") keepAliveUrl: String): VideoChannelResponse

    @GET("user/stream/video/{id}/channel/2/keepAlive")
    suspend fun keepVideoAlive(@Path("id") keepAliveUrl: String): VideoChannelResponse

    @POST("auth/logout")
    suspend fun logout()

    @GET("admin/customer")
    suspend fun dummyRequest(): String

    companion object {
        // TODO get webService if already created
        fun create(): WebService {
            val builder = OkHttpClient.Builder()
            builder.addInterceptor(AuthorizationInterceptor(HomeguardTokenUtils))
            builder.authenticator(TokenAuthenticator(TokenService.create()))
            val client = builder.build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL_API)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WebService::class.java)
        }
    }
}

interface TokenService {

    @POST("auth/refresh")
    fun refreshToken(): Call<UserAuthResponse>

    companion object {
        fun create(): TokenService {
            val builder = OkHttpClient.Builder()
            builder.addInterceptor(AuthorizationInterceptor(HomeguardTokenUtils))
            val client = builder.build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL_API)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TokenService::class.java)
        }
    }
}

class AuthorizationInterceptor(private val homeguardTokenUtils: HomeguardTokenUtils) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder()
            .addHeader("Authorization", "Bearer ${homeguardTokenUtils.readHomeguardToken()}")
            .build()
        return chain.proceed(request)
    }
}

class TokenAuthenticator(private val tokenService: TokenService) : Authenticator {

    @Throws(IOException::class)
    override fun authenticate(route: Route?, response: Response): Request? {

        // Refresh your access_token using a synchronous api request
        val newAccessToken = tokenService.refreshToken().execute().body()
        if (newAccessToken != null) {
            Log.d(TOKEN_TAG, "Token refreshed")
            HomeguardTokenUtils.saveHomeguardToken(newAccessToken.access_token)
        } else {
            Log.d(TOKEN_TAG, TOKEN_REFRESH_ERROR)
            HomeguardTokenUtils.deleteHomeguardToken()

            // Throw exception to prevent further token refresh attempts
            // The exception will be propagated to repository, viewmodel and fragment
            throw IOException(TOKEN_REFRESH_ERROR)
        }
        // Replace Authorization header of rejected request and retry it
        return response.request.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", "Bearer ${HomeguardTokenUtils.readHomeguardToken()}")
            .build()
    }
}