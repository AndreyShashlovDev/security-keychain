package com.sprinter.keychain.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.text.TextUtils

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class MessageDialogFragment : AbstractDialogFragment() {

    private val dialogSubject: PublishSubject<TupleDialogResult> = PublishSubject.create()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val requestCode: Int = arguments?.getInt(ARGS_REQUEST_CODE, 0) ?: 0
        val bundle = arguments?.getBundle(ARGS_REQUEST_BUNDLE)

        val builder = AlertDialog.Builder(context!!).setTitle(
                arguments?.getString(ARGS_TITLE, "")).setPositiveButton(
                arguments?.getString(ARGS_POSITIVE_BUTTON_TEXT)) { _, which ->
            dialogSubject.onNext(TupleDialogResult(which, requestCode, bundle))
        }

        if (!TextUtils.isEmpty(arguments?.getString(ARGS_NEGATIVE_BUTTON_TEXT))) {
            builder.setNegativeButton(arguments?.getString(ARGS_NEGATIVE_BUTTON_TEXT)) { _, which ->
                dialogSubject.onNext(TupleDialogResult(which, requestCode, bundle))
            }
        }
        if (!TextUtils.isEmpty(arguments?.getString(ARGS_NEUTRAL_BUTTON_TEXT))) {
            builder.setNeutralButton(arguments?.getString(ARGS_NEUTRAL_BUTTON_TEXT)) { _, which ->
                dialogSubject.onNext(TupleDialogResult(which, requestCode, bundle))
            }
        }

        builder.setCancelable(false)
                .setMessage(arguments?.getString(ARGS_MESSAGE, ""))
                .create()
        this.isCancelable = false

        return builder.create()
    }

    companion object {

        private val FRAGMENT_TAG = MessageDialogFragment::class.java.simpleName
        private const val ARGS_TITLE = "ARGS_TITLE"
        private const val ARGS_MESSAGE = "ARGS_MESSAGE"
        private const val ARGS_POSITIVE_BUTTON_TEXT = "ARGS_POSITIVE_BUTTON_TEXT"
        private const val ARGS_NEGATIVE_BUTTON_TEXT = "ARGS_NEGATIVE_BUTTON_TEXT"
        private const val ARGS_NEUTRAL_BUTTON_TEXT = "ARGS_NEUTRAL_BUTTON_TEXT"
        private const val ARGS_REQUEST_CODE = "ARGS_REQUEST_CODE"
        private const val ARGS_REQUEST_BUNDLE = "ARGS_REQUEST_BUNDLE"

        @JvmOverloads
        fun showDialog(fragmentManager: FragmentManager, title: String, message: String,
                positiveButtonText: String? = null, negativeButtonText: String? = null,
                neutralButtonText: String? = null, requestCode: Int = 0,
                bundle: Bundle?): Observable<TupleDialogResult> {
            dismissDialog(fragmentManager)
            val fragment = MessageDialogFragment()

            val args = Bundle()
            args.putInt(ARGS_REQUEST_CODE, requestCode)
            args.putString(ARGS_TITLE, title)
            args.putString(ARGS_MESSAGE, message)

            args.putString(ARGS_POSITIVE_BUTTON_TEXT,
                    if (TextUtils.isEmpty(positiveButtonText)) "OK"
                    else positiveButtonText)

            if (!TextUtils.isEmpty(negativeButtonText)) {
                args.putString(ARGS_NEGATIVE_BUTTON_TEXT, negativeButtonText)
            }

            if (!TextUtils.isEmpty(neutralButtonText)) {
                args.putString(ARGS_NEUTRAL_BUTTON_TEXT, neutralButtonText)
            }

            if (bundle != null) {
                args.putBundle(ARGS_REQUEST_BUNDLE, bundle)
            }
            fragment.arguments = args
            val subject = fragment.dialogSubject

            try {
                fragment.show(fragmentManager, FRAGMENT_TAG)
            } catch (e: Exception) {
                subject.onError(e)
                Timber.w(e, FRAGMENT_TAG)
            }

            return subject.hide();
        }

        fun dismissDialog(fragmentManager: FragmentManager) {
            val fragment: MessageDialogFragment? = fragmentManager.findFragmentByTag(
                    FRAGMENT_TAG) as? MessageDialogFragment
            fragment?.dialogSubject?.onComplete()

            try {
                fragment?.dismissAllowingStateLoss()
            } catch (e: Exception) {
                Timber.w(e, FRAGMENT_TAG)
            }
        }
    }

}
