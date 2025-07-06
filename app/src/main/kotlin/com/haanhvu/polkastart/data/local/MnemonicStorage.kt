package com.haanhvu.polkastart.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

fun storeMnemonic(context: Context, mnemonic: String) {
    val prefs = getSecurePrefs(context)
    prefs.edit().putString("mnemonic", mnemonic).apply()
}

fun getStoredMnemonic(context: Context): String? {
    val prefs = getSecurePrefs(context)
    return prefs.getString("mnemonic", null)
}

fun getSecurePrefs(context: Context): SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
