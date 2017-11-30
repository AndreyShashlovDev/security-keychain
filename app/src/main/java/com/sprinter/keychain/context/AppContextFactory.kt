package com.sprinter.keychain.context

import android.content.Context


object AppContextFactory {

    fun createDefault(context: Context): AppContext {
        return AppContextImpl(context);
    }

}