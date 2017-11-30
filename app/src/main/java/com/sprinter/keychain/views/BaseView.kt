package com.sprinter.keychain.views

import com.arellomobile.mvp.MvpView

interface BaseView : MvpView, DialogMessageView {

    fun onBackPressed()

}
