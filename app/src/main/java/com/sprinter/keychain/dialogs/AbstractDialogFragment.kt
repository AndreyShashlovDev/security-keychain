package com.sprinter.keychain.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import com.sprinter.keychain.utils.BindLayout

abstract class AbstractDialogFragment : DialogFragment(), OnDialogButtonClickListener {

    private val injectLayoutAnnotation: BindLayout?
        get() {
            var annotation: BindLayout?
            var typeToLookUp: Class<*>? = javaClass
            while (true) {
                annotation = typeToLookUp!!.getAnnotation(BindLayout::class.java)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val annotation = injectLayoutAnnotation
        return if (annotation != null) {
            inflateAndInject(annotation.value, inflater, container)
        } else {
            super.onCreateView(inflater, container, savedInstanceState)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = RelativeLayout(activity)
        root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        return dialog
    }

    protected fun inflateAndInject(layoutId: Int, inflater: LayoutInflater,
            container: ViewGroup?): View {
        return inflater.inflate(layoutId, container, false)
    }

    override fun onDialogButtonClick(requestCode: Int, buttonId: Int, baggage: Bundle?) {
        val fragment = this.targetFragment
        val activity = this.activity

        if (fragment is OnDialogButtonClickListener) {
            (fragment as OnDialogButtonClickListener).onDialogButtonClick(requestCode, buttonId,
                    baggage)
        } else if (activity is OnDialogButtonClickListener) {
            (activity as OnDialogButtonClickListener).onDialogButtonClick(requestCode, buttonId,
                    baggage)
        }
    }

    companion object {

        const val NO_REQUEST_CODE = -1
    }

}
