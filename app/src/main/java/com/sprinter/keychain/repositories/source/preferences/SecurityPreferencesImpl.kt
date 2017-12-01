package com.sprinter.keychain.repositories.source.preferences

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.sprinter.keychain.utils.CipherHelper

internal class SecurityPreferencesImpl(context: Context) : SecurityPreferences {

    private val preferences: SharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(
            context.applicationContext)

    init {
        CipherHelper.initializeKeystore(SecurityPreferences.KEYSTORE_ALIAS_NAME, context)
    }

    override fun put(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    override fun getLong(key: String): Long {
        return preferences.getLong(key, 0)
    }

    override fun deleteKey(key: String) {
        preferences.edit().remove(key).apply()
    }

    override fun put(key: String, value: String) {
        val encodeString = CipherHelper.encryptAesByAndroidVersion(
                SecurityPreferences.KEYSTORE_ALIAS_NAME, value)
        preferences.edit().putString(key, encodeString).apply()
    }

    override fun getString(key: String): String {
        return getString(key, "")!!
    }

    override fun getString(key: String, defaultValue: String?): String? {
        val value = preferences.getString(key, defaultValue)
        val result = if (TextUtils.isEmpty(value)) value
        else CipherHelper.decryptAesByAndroidVersion(SecurityPreferences.KEYSTORE_ALIAS_NAME, value)

        return if (result == null && defaultValue != null) defaultValue else result;
    }

    override fun put(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    override fun getBoolean(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }

    private fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

}
