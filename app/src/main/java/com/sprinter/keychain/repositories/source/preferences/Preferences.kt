package com.sprinter.keychain.repositories.source.preferences

interface Preferences {

    fun deleteKey(key: String)

    fun put(key: String, value: String)

    fun getString(key: String): String?

    fun getString(key: String, defaultValue: String?): String?

    fun put(key: String, value: Boolean)

    fun getBoolean(key: String): Boolean

    fun put(key: String, value: Long)

    fun getLong(key: String): Long

    companion object {

        const val KEYSTORE_ALIAS_NAME = "KeyChain"

        const val COLLECTION_CATEGORY = "COLLECTION_CATEGORY"

        const val INCREMENT_CATEGORY = "INCREMENT_CATEGORY"
        const val INCREMENT_CATEGORY_ITEM = "INCREMENT_CATEGORY_ITEM"

    }

}
