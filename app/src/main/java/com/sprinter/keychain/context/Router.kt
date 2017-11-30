package com.sprinter.keychain.context

import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(SkipStrategy::class)
interface Router {

    fun openCategoriesScreen()

    fun openEditKeysScreen(categoryId: Long, categoryItemId: Long)

    fun openViewKeysScreen(categoryId: Long, categoryItemId: Long)

    fun openCreateKeysScreen(categoryId: Long)

    fun openAuthorizationScreen()

    fun activeBackPress(active: Boolean)

}
