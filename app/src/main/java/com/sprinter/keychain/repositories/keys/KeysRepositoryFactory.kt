package com.sprinter.keychain.repositories.keys

import com.sprinter.keychain.repositories.source.preferences.Preferences

object KeysRepositoryFactory {

    fun createDefault(preferences: Preferences): KeysRepository {
        return KeysRepositoryImpl(preferences)
    }

}