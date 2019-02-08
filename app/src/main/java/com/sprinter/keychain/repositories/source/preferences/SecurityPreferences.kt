package com.sprinter.keychain.repositories.source.preferences

interface SecurityPreferences : Preferences {

    companion object {

        const val KEYSTORE_ALIAS_NAME = "KeyChain"

    }

}
