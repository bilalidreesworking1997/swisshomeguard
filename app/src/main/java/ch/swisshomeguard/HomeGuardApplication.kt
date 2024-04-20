package ch.swisshomeguard

import android.app.Application
import ch.swisshomeguard.utils.FirebaseTokenUtils
import ch.swisshomeguard.utils.HomeguardTokenUtils
import ch.swisshomeguard.utils.SharedPreferencesUtil
import ch.swisshomeguard.utils.createNotificationChannel


class HomeGuardApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        HomeguardTokenUtils.init(this)
        FirebaseTokenUtils.init(this)
        SharedPreferencesUtil.init(this)
        createNotificationChannel(this)
    }

    override fun onTrimMemory(level: Int) {
        if (level == TRIM_MEMORY_UI_HIDDEN) { // Works for Activity
            // Get called every-time when application went to background.
            SharedPreferencesUtil.saveIsFromRecent(true)
            try {
                android.os.Process.killProcess(android.os.Process.myPid());
            } catch (e: Exception) {
            }

        } else if (level == TRIM_MEMORY_COMPLETE) { // Works for FragmentActivty
        }
        super.onTrimMemory(level)
    }
}