package com.sprinter.keychain.presenters

import android.os.Bundle
import android.os.Vibrator
import com.arellomobile.mvp.InjectViewState
import com.sprinter.keychain.R
import com.sprinter.keychain.context.Router
import com.sprinter.keychain.managers.authorization.AuthorizationManager
import com.sprinter.keychain.managers.authorization.AuthorizationState
import com.sprinter.keychain.managers.authorization.PinAuthorization
import com.sprinter.keychain.ui.views.PinDotsView
import com.sprinter.keychain.ui.views.PinGridView
import com.sprinter.keychain.utils.RxUtils
import com.sprinter.keychain.views.AuthorizationView
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

@InjectViewState
class AuthorizationPresenter constructor(private val authManager: AuthorizationManager,
        private val router: Router, private val vibrator: Vibrator) :
        AbstractPresenter<AuthorizationView>(), PinGridView.OnPinKeyboardClickListener {

    private val VIBRATE_DURATION_MS_ON_CLICK: Long = 50
    private val VIBRATE_DURATION_MS_PIN_WRONG: Long = 300

    private var pinBuilder: StringBuilder = StringBuilder()
    private var needRepeatPin: Boolean = false
    private var repeatedPin: String = ""
    private var authSubscribe: Disposable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        authManager.setupStrategy(AuthorizationManager.AUTHORIZATION_STRATEGY_PIN_CODE)

        if (!authManager.hasAuthData()) {
            viewState.hideFingerprint();
            prepareToCreatePin()
        }
    }

    override fun attachView(view: AuthorizationView) {
        super.attachView(view)

        authManager.setupStrategy(AuthorizationManager.AUTHORIZATION_STRATEGY_PIN_CODE)

        if (authManager.hasAuthData()) {
            authManager.setupStrategy(AuthorizationManager.AUTHORIZATION_STRATEGY_FINGERPRINT)
            authManager.signIn(Bundle.EMPTY)

            if (!authManager.isAvailable) {
                authManager.setupStrategy(AuthorizationManager.AUTHORIZATION_STRATEGY_PIN_CODE);
                viewState.hideFingerprint();
            }
            viewState.hideMessageText()
        }

        authSubscribe?.dispose()

        authSubscribe = authManager.state.compose(RxUtils::async).compose(
                bindUntilDestroy()).subscribe(this::onChangeAuthState)
    }

    override fun detachView(view: AuthorizationView) {
        super.detachView(view)
        authSubscribe?.dispose()
    }

    private fun onChangeAuthState(state: AuthorizationState) {
        if (state.state === AuthorizationState.AUTHORIZATION.SUCCESS) {
            router.openCategoriesScreen()
        } else {
            onWrongPin()
        }
    }

    override fun onPinKeyboardClick(tag: Any?) {
        var activated = false
        var position = 0

        if (tag is String) {
            activated = true
            position = pinBuilder.length
            pinBuilder.append(tag as String?)

        } else if (tag is Int) {
            if (tag == R.drawable.ic_backspace && pinBuilder.isNotEmpty()) {
                pinBuilder.deleteCharAt(pinBuilder.length - 1)
                activated = false
                position = pinBuilder.length
            }
        }

        viewState.activatePinDot(position, activated)
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VIBRATE_DURATION_MS_ON_CLICK)
        }

        if (pinBuilder.length == PinDotsView.COUNT_DOTS + 1) {
            authManager.setupStrategy(AuthorizationManager.AUTHORIZATION_STRATEGY_PIN_CODE)
            viewState.enabledPinKeyboard(false)
            Observable.empty<Any>().delay(300, TimeUnit.MILLISECONDS).compose(
                    RxUtils::async).doOnComplete(this::validatePin).subscribe()
        }
    }

    private fun validatePin() {
        if (authManager.hasAuthData()) {
            authManager.signIn(PinAuthorization.createBundle(pinBuilder.toString()))

        } else if (needRepeatPin) {
            needRepeatPin = false
            repeatedPin = pinBuilder.toString()
            pinBuilder.delete(0, pinBuilder.length)
            viewState.resetPinDots()
            viewState.enabledPinKeyboard(true)
            viewState.setMessageText(R.string.pin_msg_pin_repeat)

        } else {
            if (repeatedPin.contentEquals(pinBuilder.toString())) {
                authManager.signUp(PinAuthorization.createBundle(pinBuilder.toString()))

            } else {
                repeatedPin = ""
                onWrongPin()
                prepareToCreatePin()
            }
        }
    }

    private fun prepareToCreatePin() {
        viewState.setMessageText(R.string.pin_msg_pin_not_exist)
        needRepeatPin = true
        viewState.enabledPinKeyboard(true)
    }

    private fun onWrongPin() {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VIBRATE_DURATION_MS_PIN_WRONG)
        }
        pinBuilder.delete(0, pinBuilder.length)
        viewState.animationWrongPin(R.anim.anim_shake)
        viewState.enabledPinKeyboard(true)
    }

}