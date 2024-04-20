package ch.swisshomeguard.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import ch.swisshomeguard.FCM_TAG

/**
 * Saves and read the Firebase token in SharedPreferences
 */
object FirebaseTokenUtils {

    private lateinit var sharedPrefs: SharedPreferences
    private const val FIREBASE_TOKEN_KEY = "firebaseToken"

    fun init(context: Context) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun saveFirebaseTokenIfChanged(firebaseToken: String) {
        if (checkIfFirebaseTokenHasChanged(firebaseToken)) {
            saveFirebaseToken(firebaseToken)
        }
    }

    fun readFirebaseToken(): String? {
        Log.d(FCM_TAG, "Read Firebase token")
        return sharedPrefs.getString(FIREBASE_TOKEN_KEY, null)
    }

    fun clearDefaultSharedPreferences() {
        Log.d(FCM_TAG, "Clear Firebase token and all SharedPrefs")
        sharedPrefs.edit().clear().apply()
    }

    private fun checkIfFirebaseTokenHasChanged(newToken: String): Boolean {
        val oldToken = readFirebaseToken()
        return oldToken != newToken
    }

    private fun saveFirebaseToken(firebaseToken: String) {
        Log.d(FCM_TAG, "Save Firebase token")
        with(sharedPrefs.edit()) {
            putString(FIREBASE_TOKEN_KEY, firebaseToken)
            commit()
        }
    }
}