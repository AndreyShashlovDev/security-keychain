package com.sprinter.keychain.ui.fragments

import android.os.Bundle
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.sprinter.keychain.R
import com.sprinter.keychain.context.AppContext
import com.sprinter.keychain.context.AppDelegate
import com.sprinter.keychain.dialogs.InputTextDialogFragment
import com.sprinter.keychain.dialogs.MessageDialogFragment
import com.sprinter.keychain.dialogs.TupleDialogResult
import com.sprinter.keychain.ui.views.ToolbarView
import com.sprinter.keychain.utils.AndroidUtils
import com.sprinter.keychain.utils.BindLayout
import com.sprinter.keychain.utils.RxUtils
import com.sprinter.keychain.views.BaseView
import com.sprinter.keychain.views.ToolBar
import io.reactivex.disposables.Disposable
import timber.log.Timber

abstract class AbstractFragment<E : MvpPresenter<*>> : MvpAppCompatFragment(), BaseView {

    protected lateinit var appContext: AppContext
    private var dialogDisposable: Disposable? = null

    private val injectLayoutAnnotation: BindLayout?
        get() {
            var annotation: BindLayout?
            var typeToLookUp: Class<*> = javaClass
            while (true) {
                annotation = typeToLookUp.getAnnotation(BindLayout::class.java) as BindLayout

                if (annotation != null) {
                    break
                }
                typeToLookUp = typeToLookUp.superclass
                if (typeToLookUp == null) {
                    break
                }
            }

            return annotation
        }

    protected val isRootFragment: Boolean
        get() = fragmentManager!!.backStackEntryCount <= 1

    override fun onBackPressed() {
        activity!!.onBackPressed()
    }

    protected fun getToolBar(): ToolbarView? {
        return if (activity is ToolBar) (activity as ToolBar).getToolBar() else null
    }

    @ProvidePresenter protected open fun providePresenter(): E? {
        val appDelegate = activity!!.applicationContext as AppDelegate
        appContext = appDelegate.appContext!!

        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val annotation: BindLayout? = injectLayoutAnnotation
        return if (annotation != null) {
            inflateAndInject(annotation.value, inflater, container)

        } else {
            super.onCreateView(inflater, container, savedInstanceState)
        }
    }

    private fun inflateAndInject(layoutId: Int, inflater: LayoutInflater,
            container: ViewGroup?): View {
        return inflater.inflate(layoutId, container, false)
    }

    override fun showDialogMessage(message: String, positiveBtn: String?, negativeBtn: String?,
            neutralBtn: String?, requestCode: Int, bundle: Bundle?) {
        AndroidUtils.hideSoftKeyboard(activity!!)

        dialogDisposable = MessageDialogFragment.showDialog(fragmentManager!!,
                getString(R.string.app_name), message, positiveBtn, negativeBtn, neutralBtn,
                requestCode, bundle).compose(RxUtils::async).subscribe(
                this::onDialogMessageResult, { throwable -> Timber.e(throwable, "dialog") })
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

    override fun showDialogInput(message: String, value: String, maxLen: Int, requestCode: Int,
            bundle: Bundle?) {
        dialogDisposable = InputTextDialogFragment.showDialog(fragmentManager!!, message, value,
                maxLen, requestCode, bundle).compose(RxUtils::async).subscribe(
                this::onDialogMessageResult) { throwable -> Timber.e(throwable, "dialog") }
    }

    override fun showDialogInput(message: Int, value: String, maxLen: Int, requestCode: Int,
            bundle: Bundle?) {
        val messageText = getString(message)
        showDialogInput(messageText, value, maxLen, requestCode, bundle)
    }

    open override fun onDialogMessageResult(result: TupleDialogResult) {
        // override if need
    }

    override fun onStop() {
        MessageDialogFragment.dismissDialog(fragmentManager!!)
        InputTextDialogFragment.dismissDialog(fragmentManager!!)
        dialogDisposable?.dispose();
        super.onStop()
    }

}
