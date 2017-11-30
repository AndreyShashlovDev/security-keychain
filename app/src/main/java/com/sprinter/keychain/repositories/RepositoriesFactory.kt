package com.sprinter.keychain.repositories

import android.content.Context

object RepositoriesFactory {

    fun createDefault(context: Context): Repositories {
        return RepositoriesImpl(context)
    }

}