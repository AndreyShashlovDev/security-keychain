package com.sprinter.keychain.views

import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(SkipStrategy::class)
interface MainView : BaseView {

    fun visibilityPreloader(visible: Boolean)

    fun visibilityToolbar(visible: Boolean)

}
