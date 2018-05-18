package com.sprinter.keychain.managers.authorization

import android.support.annotation.IntDef

interface AuthorizationManager : Authorization {

    @IntDef(AUTHORIZATION_STRATEGY_FINGERPRINT.toLong(), AUTHORIZATION_STRATEGY_PIN_CODE.toLong())
    @Retention(AnnotationRetention.SOURCE)
    annotation class AuthorizationType

    fun addStrategy(strategyId: Int, strategy: AbstractAuthorization)

    fun setupStrategy(strategyId: Int)

    companion object {

        const val AUTHORIZATION_STRATEGY_STUB = -1
        const val AUTHORIZATION_STRATEGY_FINGERPRINT = 1
        const val AUTHORIZATION_STRATEGY_PIN_CODE = 2
    }

}
