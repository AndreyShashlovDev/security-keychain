package com.sprinter.keychain.views

import android.support.annotation.AnimRes
import android.support.annotation.StringRes

interface AuthorizationView : BaseView {

    fun animationWrongPin(@AnimRes animationResId: Int)

    fun enabledPinKeyboard(enabled: Boolean)

    fun setMessageText(@StringRes messageResId: Int)

    fun hideMessageText()

    fun resetPinDots()

    fun activatePinDot(position: Int, activated: Boolean)

    fun hideFingerprint()

}