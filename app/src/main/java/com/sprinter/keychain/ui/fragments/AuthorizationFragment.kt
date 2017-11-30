package com.sprinter.keychain.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.support.annotation.AnimRes
import android.view.View
import android.view.animation.AnimationUtils
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.sprinter.keychain.R
import com.sprinter.keychain.presenters.AuthorizationPresenter
import com.sprinter.keychain.ui.views.ToolbarView
import com.sprinter.keychain.utils.BindLayout
import com.sprinter.keychain.views.AuthorizationView
import kotlinx.android.synthetic.main.fmt_authorization.*


@BindLayout(R.layout.fmt_authorization)
class AuthorizationFragment : AbstractFragment<AuthorizationPresenter>(), AuthorizationView {

    @InjectPresenter lateinit var presenter: AuthorizationPresenter;

    @ProvidePresenter override fun providePresenter(): AuthorizationPresenter {
        super.providePresenter()
        val authManager = appContext.authorizationManager()
        val router = appContext.router()
        val vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;

        return AuthorizationPresenter(authManager, router, vibrator)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pinKeyboard.setOnPincodeKeyboardListener(presenter)
    }

    override fun onResume() {
        super.onResume()
        getToolBar()?.visibility = View.GONE
        getToolBar()?.visibilityHomeButton(false)
        getToolBar()?.setStateHomeButton(ToolbarView.STATE_HOME_BUTTON_BACK)
    }

    override fun animationWrongPin(@AnimRes animationResId: Int) {
        pinDots.startAnimation(AnimationUtils.loadAnimation(context, animationResId))
        resetPinDots()
    }

    override fun enabledPinKeyboard(enabled: Boolean) {
        pinKeyboard.isEnabled = enabled
    }

    override fun setMessageText(messageResId: Int) {
        pinMessage.text = getString(messageResId)
        pinMessage.visibility = View.VISIBLE
    }

    override fun hideMessageText() {
        pinMessage.visibility = View.GONE;
    }

    override fun resetPinDots() {
        pinDots.reset()
    }

    override fun activatePinDot(position: Int, activated: Boolean) {
        pinDots.setActivated(position, activated)
    }

    override fun hideFingerprint() {
        pinKeyboard.hideFingerprint();
    }

    companion object {

        val FRAGMENT_TAG: String
            get() = AuthorizationFragment::class.java.simpleName

        fun newInstance(): AuthorizationFragment = AuthorizationFragment()

    }

}
