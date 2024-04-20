package ch.swisshomeguard.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SharedPreferencesUtil {
    private lateinit var encryptedSharedPrefs: SharedPreferences
    private const val SHOULD_RELOAD = "ShouldReload"
    private const val IS_RECENT = "IsRecent"
    private const val ENCRYPTED_SHARED_PREFERENCES_FILE = "EncryptedSharedPreferences"

    fun init(context: Context) {
        encryptedSharedPrefs = getEncryptedSharedPreferences(context)
    }

    fun saveShouldReload(shouldReload: Boolean) {
        encryptedSharedPrefs.edit()
            .putBoolean(SHOULD_RELOAD, shouldReload)
            .apply()
    }

    fun readShouldReload(): Boolean {
        return encryptedSharedPrefs.getBoolean(SHOULD_RELOAD, true)
    }

    fun saveIsFromRecent(shouldReload: Boolean) {
        encryptedSharedPrefs.edit()
            .putBoolean(IS_RECENT, shouldReload)
            .apply()
    }

    fun readIsFromRecent(): Boolean {
        return encryptedSharedPrefs.getBoolean(IS_RECENT, false)
    }

    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        // TODO this always creates a new file?  What if it already exists?
        // TODO erase encrypted share preferences file and create a new one if it cannot be read
        return EncryptedSharedPreferences.create(
            ENCRYPTED_SHARED_PREFERENCES_FILE,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}