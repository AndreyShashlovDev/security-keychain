package com.sprinter.keychain.managers.authorization

import android.content.Context
import android.os.Bundle

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

abstract class AbstractAuthorization(context: Context) : Authorization {

    private val publishSubject: PublishSubject<AuthorizationState> = PublishSubject.create()
    protected val context: Context = context.applicationContext

    override val state: Observable<AuthorizationState>
        get() = publishSubject

    override fun signUp(bundle: Bundle) {
        Timber.i("signUp for %s not implemented! May be use signIn?", this.javaClass.simpleName)
    }

    override fun logout() {
        publishSubject.onNext(AuthorizationState(type, AuthorizationState.AUTHORIZATION.LOGOUT, ""))
    }

    protected fun sendResultSuccess(message: String? = null) {
        publishSubject.onNext(
                AuthorizationState(type, AuthorizationState.AUTHORIZATION.SUCCESS, message))
    }

    protected fun sendResultSuccess(bundle: Bundle?) {
        publishSubject.onNext(
                AuthorizationState(type, AuthorizationState.AUTHORIZATION.SUCCESS, bundle))
    }

    protected fun sendResultFail(message: String? = null) {
        publishSubject.onNext(
                AuthorizationState(type, AuthorizationState.AUTHORIZATION.FAIL, message))
    }

    protected fun sendResultFail(bundle: Bundle?) {
        publishSubject.onNext(
                AuthorizationState(type, AuthorizationState.AUTHORIZATION.FAIL, bundle))
    }

    internal abstract fun destroy()

    companion object {

        fun createBundle(): Bundle {
            return Bundle.EMPTY
        }

    }

}
