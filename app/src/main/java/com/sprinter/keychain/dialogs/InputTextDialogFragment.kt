package com.sprinter.keychain.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.InputFilter
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class InputTextDialogFragment : AbstractDialogFragment(), DialogInterface.OnClickListener,
        DialogInterface.OnCancelListener {

    private lateinit var editText: EditText
    private val dialogSubject = PublishSubject.create<TupleDialogResult>()
    private var requestCode = 0
    private var bundle: Bundle? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        requestCode = arguments?.getInt(ARGS_REQUEST_CODE, 0) ?: 0
        bundle = arguments?.getBundle(ARGS_REQUEST_BUNDLE)

        editText = EditText(context)
        editText.setText(arguments?.getString(ARGS_VALUE, ""))
        editText.maxLines = 1

        val maxLength = arguments?.getInt(ARGS_MAX_LEN, Int.MAX_VALUE) ?: 0
        val arrayOfInputFilters = arrayOfNulls<InputFilter>(1)
        arrayOfInputFilters[0] = InputFilter.LengthFilter(maxLength)
        editText.filters = arrayOfInputFilters

        editText.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        editText.setTextColor(ContextCompat.getColor(context!!, android.R.color.black))

        val builder = AlertDialog.Builder(context!!)
        builder.setMessage(arguments?.getString(ARGS_MESSAGE, ""))
        builder.setView(editText)

        editText.post {
            val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            editText.setSelection(editText.text.length)
        }

        builder.setPositiveButton("OK", this)
        builder.setNegativeButton("Cancel", this)
        builder.setCancelable(false)

        isCancelable = false

        return builder.show()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (bundle == null) {
            bundle = Bundle()
        }
        bundle!!.putString(ARGS_VALUE, editText.text.toString())

        dialogSubject.onNext(TupleDialogResult(which, requestCode, bundle))
    }

    override fun onDestroyView() {
        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
        super.onDestroyView()
    }

    companion object {

        private val FRAGMENT_TAG = InputTextDialogFragment::class.java.simpleName

        const private val ARGS_MESSAGE = "ARGS_MESSAGE"
        const public val ARGS_VALUE = "ARGS_VALUE"
        const private val ARGS_MAX_LEN = "ARGS_MAX_LEN"
        const private val ARGS_REQUEST_CODE = "ARGS_REQUEST_CODE"
        const private val ARGS_REQUEST_BUNDLE = "ARGS_REQUEST_BUNDLE"

        fun showDialog(fragmentManager: FragmentManager, message: String, value: String,
                maxLen: Int, requestCode: Int, bundle: Bundle?): Observable<TupleDialogResult> {
            dismissDialog(fragmentManager)
            val fragment = InputTextDialogFragment()

            val args = Bundle()
            args.putString(ARGS_MESSAGE, message)
            args.putString(ARGS_VALUE, value)
            args.putInt(ARGS_MAX_LEN, maxLen)
            args.putInt(ARGS_REQUEST_CODE, requestCode)
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

            return subject
        }

        fun dismissDialog(fragmentManager: FragmentManager) {
            val fragment: InputTextDialogFragment? = fragmentManager.findFragmentByTag(
                    FRAGMENT_TAG) as? InputTextDialogFragment

            fragment?.dialogSubject?.onComplete()
            try {
                fragment?.dismissAllowingStateLoss()
            } catch (e: Exception) {
                Timber.w(e, FRAGMENT_TAG)
            }
        }
    }

}
