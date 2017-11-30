package com.sprinter.keychain.repositories.source.preferences

import android.content.Context
import com.sprinter.keychain.utils.CipherHelper

import io.reactivex.Completable

object PreferencesFactory {

    fun createSecurity(context: Context): SecurityPreferences {
        return SecurityPreferencesImpl(context)
    }

}
