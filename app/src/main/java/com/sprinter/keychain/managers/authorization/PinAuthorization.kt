package com.sprinter.keychain.managers.authorization

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import com.sprinter.keychain.utils.CipherHelper

class PinAuthorization(context: Context) : AbstractAuthorization(context) {

    private val preferences: SharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(
            context.applicationContext)

    override val type: Int
        @AuthorizationManager.AuthorizationType get() = AuthorizationManager.AUTHORIZATION_STRATEGY_PIN_CODE

    override val isAvailable: Boolean
        get() = true

    override fun signUp(bundle: Bundle) {
        val pincode = bundle.getString(KEY_ALIAS)

        if (TextUtils.isEmpty(pincode)) {
            sendResultFail()
        } else {
            val encodedPinCode = CipherHelper.generateSha256x2Base64(pincode)
            preferences.edit().putString(KEY_ALIAS, encodedPinCode).apply()
            sendResultSuccess()
        }
    }

    override fun signIn(bundle: Bundle) {
        val pincode = bundle.getString(KEY_ALIAS)

        if (TextUtils.isEmpty(pincode)) {
            sendResultFail()

        } else {
            val encodedPinCode = CipherHelper.generateSha256x2Base64(pincode)
            val savedPinCode = preferences.getString(KEY_ALIAS, null)

            if (TextUtils.isEmpty(encodedPinCode) || TextUtils.isEmpty(
                    savedPinCode) || encodedPinCode != savedPinCode) {
                sendResultFail()
            } else {
                sendResultSuccess()
            }
        }
    }

    override fun hasAuthData(): Boolean {
        return !TextUtils.isEmpty(preferences.getString(KEY_ALIAS, null))
    }

    override fun logout() {
        preferences.edit().putString(KEY_ALIAS, "").apply()
        super.logout()
    }

    override fun restoreAuthorization() {
        throw UnsupportedOperationException("method not implemented")
    }

    override fun destroy() {
    }

    companion object {

        private const val KEY_ALIAS = "com.bitclave.base.pincode"

        fun createBundle(pincode: String): Bundle {
            val bundle = Bundle()
            bundle.putString(KEY_ALIAS, pincode)
            return bundle
        }
    }

}
