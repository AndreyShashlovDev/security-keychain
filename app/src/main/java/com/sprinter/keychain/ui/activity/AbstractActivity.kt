package com.sprinter.keychain.ui.activity

import android.content.Context
import android.os.Bundle
import android.support.annotation.StringRes
import android.view.WindowManager
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.sprinter.keychain.R
import com.sprinter.keychain.context.AppContext
import com.sprinter.keychain.context.AppDelegate
import com.sprinter.keychain.dialogs.InputTextDialogFragment
import com.sprinter.keychain.dialogs.MessageDialogFragment
import com.sprinter.keychain.dialogs.TupleDialogResult
import com.sprinter.keychain.utils.AndroidUtils
import com.sprinter.keychain.utils.BindLayout
import com.sprinter.keychain.utils.RxUtils
import com.sprinter.keychain.views.ActivityBaseView
import io.reactivex.disposables.Disposable
import timber.log.Timber
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

abstract class AbstractActivity<E : MvpPresenter<*>> : MvpAppCompatActivity(), ActivityBaseView {

    protected lateinit var appContext: AppContext
    private var dialogDisposable: Disposable? = null
    private var backPressedListener: ActivityBaseView.OnBackPressedListener? = null;

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    @ProvidePresenter protected open fun providePresenter(): E? {
        appContext = (applicationContext as AppDelegate).appContext!!

        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE)

        val layout: BindLayout? = javaClass.getAnnotation(BindLayout::class.java)
        if (layout != null) {
            setContentView(layout.value)
        }
    }

    override fun onBackPressed() {
        AndroidUtils.hideSoftKeyboard(this)
        if (backPressedListener == null || backPressedListener?.onBackPressed()!!) {
            super.onBackPressed();
        }
    }

    override fun showDialogMessage(@StringRes messageResId: Int, @StringRes positiveBtnResId: Int,
            @StringRes negativeBtnResId: Int, @StringRes neutralBtnResId: Int, requestCode: Int,
            bundle: Bundle?) {
        val message = getString(messageResId)
        val positive = if (positiveBtnResId != 0) getString(positiveBtnResId) else null
        val negative = if (negativeBtnResId != 0) getString(negativeBtnResId) else null
        val neutral = if (neutralBtnResId != 0) getString(neutralBtnResId) else null

        showDialogMessage(message, positive, negative, neutral, requestCode, bundle)
    }

    override fun showDialogMessage(message: String, positiveBtn: String?, negativeBtn: String?,
            neutralBtn: String?, requestCode: Int, bundle: Bundle?) {

        AndroidUtils.hideSoftKeyboard(this)

        dialogDisposable = MessageDialogFragment.showDialog(supportFragmentManager,
                getString(R.string.app_name), message, positiveBtn, negativeBtn, neutralBtn,
                requestCode, bundle).compose(RxUtils::async).subscribe(
                this::onDialogMessageResult, { throwable -> Timber.e(throwable, "dialog") })
    }

    override fun showDialogInput(message: String, value: String, maxLen: Int, requestCode: Int,
            bundle: Bundle?) {
        dialogDisposable = InputTextDialogFragment.showDialog(supportFragmentManager, message,
                value, maxLen, requestCode, bundle).compose(RxUtils::async).subscribe(
                this::onDialogMessageResult, { throwable -> Timber.e(throwable, "dialog") })
    }

    override fun showDialogInput(message: Int, value: String, maxLen: Int, requestCode: Int,
            bundle: Bundle?) {
        val messageText = getString(message)
        showDialogInput(messageText, value, maxLen, requestCode, bundle)
    }

    open override fun onDialogMessageResult(result: TupleDialogResult) {
    }

    override fun setOnBackPressedListener(listener: ActivityBaseView.OnBackPressedListener?) {
        backPressedListener = listener
    }

    override fun onStop() {
        MessageDialogFragment.dismissDialog(supportFragmentManager)
        InputTextDialogFragment.dismissDialog(supportFragmentManager)
        dialogDisposable?.dispose()
        super.onStop()
    }

    public override fun onDestroy() {
        AndroidUtils.hideSoftKeyboard(this)

        super.onDestroy()
    }

}
