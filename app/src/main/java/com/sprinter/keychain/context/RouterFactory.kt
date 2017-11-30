package com.sprinter.keychain.context

import android.content.Context

object RouterFactory {

    fun createDefault(context: Context): Router {
        return RouterImpl(context);
    }

}
