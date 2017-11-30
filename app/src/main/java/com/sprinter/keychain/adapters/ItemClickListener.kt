package com.sprinter.keychain.adapters

import android.support.annotation.IdRes

interface ItemClickListener {

    fun onItemClick(position: Int, @IdRes id: Int, tag: String?)

}
