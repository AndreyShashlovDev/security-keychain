package com.sprinter.keychain.managers.authorization

import android.content.Context
import android.os.Bundle
import android.util.SparseArray

import com.sprinter.keychain.utils.RxUtils

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

internal class AuthorizationManagerImpl(context: Context) : AuthorizationManager {

    private val mStrategySparseArray = SparseArray<AbstractAuthorization>()
    private var mAuthorization: AbstractAuthorization
    private val mPublishSubject: PublishSubject<AuthorizationState>

    override val type: Int
        get() = mAuthorization.type

    override val isAvailable: Boolean
        get() = mAuthorization.isAvailable

    override val state: Observable<AuthorizationState>
        get() = mPublishSubject.hide()

    init {
        mPublishSubject = PublishSubject.create()
        addStrategy(AuthorizationManager.AUTHORIZATION_STRATEGY_STUB,
                StubAuthorization(context))
        mAuthorization = mStrategySparseArray.get(AuthorizationManager.AUTHORIZATION_STRATEGY_STUB)
    }

    override fun addStrategy(strategyId: Int, strategy: AbstractAuthorization) {
        if (mStrategySparseArray.get(strategyId) == null) {
            strategy.state
                    .compose(RxUtils::async)
                    .subscribe(this::onUpdateAuthState)
        }
        mStrategySparseArray.put(strategyId, strategy)
    }

    override fun setupStrategy(strategyId: Int) {
        val lastAuth = mAuthorization
        val selectedAuth = mStrategySparseArray.get(strategyId) ?: throw IllegalArgumentException("strategy not found! use addStrategy before")

        if (mAuthorization != null || selectedAuth != mAuthorization) {
            mAuthorization = selectedAuth
        }

        if (lastAuth != null && mAuthorization !== lastAuth) {
            lastAuth.destroy()
        }
    }

    private fun onUpdateAuthState(state: AuthorizationState) {
        mPublishSubject.onNext(state)
    }

    override fun signUp(bundle: Bundle) {
        mAuthorization.signUp(bundle)
    }

    override fun signIn(bundle: Bundle) {
        mAuthorization.signIn(bundle)
    }

    override fun hasAuthData(): Boolean {
        return mAuthorization.hasAuthData()
    }

    override fun logout() {
        val size = mStrategySparseArray.size()
        var authorization: Authorization

        for (i in 0 until size) {
            authorization = mStrategySparseArray.valueAt(i)
            authorization.logout()
        }
    }

    override fun restoreAuthorization() {
        mAuthorization.restoreAuthorization()
    }

}
