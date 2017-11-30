package com.sprinter.keychain.managers.authorization

import android.os.Bundle

class AuthorizationState
internal constructor(@AuthorizationManager.AuthorizationType val type: Int,
        val state: AUTHORIZATION, val message: String?, val bundle: Bundle? = null) {

    enum class AUTHORIZATION {
        SUCCESS, LOGOUT, FAIL
    }

    internal constructor(@AuthorizationManager.AuthorizationType type: Int, state: AUTHORIZATION,
            bundle: Bundle?) : this(type, state, null as String?, bundle) {
    }

}
