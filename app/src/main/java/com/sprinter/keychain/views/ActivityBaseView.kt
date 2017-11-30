package com.sprinter.keychain.views

interface ActivityBaseView : BaseView {

    interface OnBackPressedListener {

        fun onBackPressed(): Boolean

    }

    fun setOnBackPressedListener(listener: OnBackPressedListener?)

}
