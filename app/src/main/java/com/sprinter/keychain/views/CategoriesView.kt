package com.sprinter.keychain.views

import android.support.annotation.IdRes
import com.sprinter.keychain.repositories.source.models.Category

interface CategoriesView : BaseView {

    fun visibilityLoading(visible: Boolean)

    fun setCategoriesList(categories: List<Category>)

    fun visibilityEmptyListMessage(visible: Boolean)

    fun showMenu(position: Int, @IdRes id: Int)

}
