package com.sprinter.keychain.managers.authorization

import android.os.Bundle

import io.reactivex.Observable

interface Authorization {

    val type: Int

    val isAvailable: Boolean

    val state: Observable<AuthorizationState>

    fun signUp(bundle: Bundle)

    fun signIn(bundle: Bundle)

    fun hasAuthData(): Boolean

    fun logout()

    fun restoreAuthorization()

}
