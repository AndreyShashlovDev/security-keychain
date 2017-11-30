package com.sprinter.keychain.repositories

import com.sprinter.keychain.repositories.keys.KeysRepository

interface Repositories {

    fun getKeysRepository(): KeysRepository

}