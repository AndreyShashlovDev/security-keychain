package com.sprinter.keychain.managers.authorization

import android.content.Context
import android.os.Build
import android.support.annotation.WorkerThread

object AuthorizationManagerFactory {

    @WorkerThread
    fun createDefault(context: Context): AuthorizationManager {
        val manager = AuthorizationManagerImpl(context);
        manager.addStrategy(
                AuthorizationManager.AUTHORIZATION_STRATEGY_PIN_CODE,
                PinAuthorization(context)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.addStrategy(
                    AuthorizationManager.AUTHORIZATION_STRATEGY_FINGERPRINT,
                    FingerprintAuthorization(context)
            )
        }

        return manager
    }

}
