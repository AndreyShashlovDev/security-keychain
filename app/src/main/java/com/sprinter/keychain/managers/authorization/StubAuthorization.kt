package com.sprinter.keychain.managers.authorization

import android.content.Context
import android.os.Bundle

class StubAuthorization(context: Context) : AbstractAuthorization(context) {

    override fun destroy() {
    }

    override val type: Int
        get() = AuthorizationManager.AUTHORIZATION_STRATEGY_STUB

    override val isAvailable: Boolean
        get() = false

    override fun signIn(bundle: Bundle) {
        throw UnsupportedOperationException("method not implemented")
    }

    override fun hasAuthData(): Boolean {
        return false
    }

    override fun restoreAuthorization() {
        throw UnsupportedOperationException("method not implemented")
    }

}
