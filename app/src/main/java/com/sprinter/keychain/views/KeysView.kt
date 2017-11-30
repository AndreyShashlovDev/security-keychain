package com.sprinter.keychain.views

import android.support.annotation.StringRes

interface KeysView : BaseView {

    fun setKeyList(items: List<Pair<String, String>>)

    fun setCategoryTitle(title: String)

    fun setTitle(title: String)

    fun enabledTitle(enabled: Boolean)

    fun enabledKeysItemsList(enabled: Boolean)

    fun visibilityAddKeysButton(visible: Boolean)

    fun copyToClipboard(label: String, value: String)

    fun showToast(@StringRes messageId: Int)

    fun visibilityLoading(visible: Boolean)

    fun visibilityTitle(visible: Boolean)

    fun visibilityKeys(visible: Boolean)

}