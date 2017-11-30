package com.sprinter.keychain.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object AndroidUtils {

    fun <T> getNotNullArray(array: List<T>?): List<T> {
        return array ?: emptyList<T>()
    }

    fun getNotNullText(text: String?): String {
        return text ?: ""
    }

    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (activity.currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!
                    .windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    fun hideSoftKeyboard(view: View) {
        val imm = view.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (!imm.isActive) {
            return
        }

        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun setClipboard(context: Context, label: String, text: String) {
        val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText(label, text);
        clipboard.primaryClip = clip
    }

}
