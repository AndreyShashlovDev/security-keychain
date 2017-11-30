package com.sprinter.keychain.adapters

import android.support.annotation.IdRes

interface NestedItemClickListener {

    fun onItemClick(rootPosition: Int, subPosition: Int, @IdRes id: Int, tag: String?)

}
