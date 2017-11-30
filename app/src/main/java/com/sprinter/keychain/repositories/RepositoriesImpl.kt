package com.sprinter.keychain.repositories

import android.content.Context
import com.sprinter.keychain.repositories.source.preferences.PreferencesFactory
import com.sprinter.keychain.repositories.keys.KeysRepository
import com.sprinter.keychain.repositories.keys.KeysRepositoryFactory

internal class RepositoriesImpl(context: Context) : Repositories {

    private val mKeysRepository: KeysRepository

    init {
        mKeysRepository = KeysRepositoryFactory.createDefault(
                PreferencesFactory.createSecurity(context))
    }

    override fun getKeysRepository(): KeysRepository {
        return mKeysRepository
    }

}