package ch.swisshomeguard.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import ch.swisshomeguard.TOKEN_TAG

/**
 * Saves and reads the Homeguard token in EncryptedSharedPreferences
 */
object HomeguardTokenUtils {

    private lateinit var encryptedSharedPrefs: SharedPreferences
    private const val HOMEGUARD_TOKEN_KEY = "TOKEN"
    private const val ENCRYPTED_SHARED_PREFERENCES_FILE = "EncryptedSharedPreferences"

    fun init(context: Context) {
        encryptedSharedPrefs = getEncryptedSharedPreferences(context)
    }

    fun saveHomeguardToken(token: String) {
        Log.d(TOKEN_TAG, "Save Homeguard token")
        encryptedSharedPrefs.edit()
            .putString(HOMEGUARD_TOKEN_KEY, token)
            .apply()
    }

    fun readHomeguardToken(): String? {
        Log.d(TOKEN_TAG, "Read Homeguard token")
        return encryptedSharedPrefs.getString(HOMEGUARD_TOKEN_KEY, null)
    }

    fun deleteHomeguardToken() {
        Log.d(TOKEN_TAG, "Delete Homeguard token")
        encryptedSharedPrefs.edit()
            .remove(HOMEGUARD_TOKEN_KEY)
            .apply()
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