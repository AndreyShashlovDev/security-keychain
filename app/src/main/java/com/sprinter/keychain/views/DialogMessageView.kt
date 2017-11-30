package com.sprinter.keychain.views

import android.os.Bundle
import android.support.annotation.StringRes
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.sprinter.keychain.dialogs.TupleDialogResult

@StateStrategyType(SkipStrategy::class)
interface DialogMessageView {

    fun showDialogMessage(message: String, positiveBtn: String? = null, negativeBtn: String? = null,
            neutralBtn: String? = null, requestCode: Int = 0, bundle: Bundle? = null)

    fun showDialogMessage(@StringRes messageResId: Int, @StringRes positiveBtnResId: Int = 0,
            @StringRes negativeBtnResId: Int = 0, @StringRes neutralBtnResId: Int = 0,
            requestCode: Int = 0, bundle: Bundle? = null)

    fun showDialogInput(message: String, value: String, maxLen: Int = Int.MAX_VALUE,
            requestCode: Int = 0, bundle: Bundle? = null)

    fun showDialogInput(message: Int, value: String, maxLen: Int = Int.MAX_VALUE,
            requestCode: Int = 0, bundle: Bundle? = null)

    fun onDialogMessageResult(result: TupleDialogResult)

}
