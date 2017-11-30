package com.sprinter.keychain.ui.activity

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.sprinter.keychain.R
import com.sprinter.keychain.presenters.MainPresenter
import com.sprinter.keychain.ui.views.ToolbarView
import com.sprinter.keychain.utils.BindLayout
import com.sprinter.keychain.views.MainView
import com.sprinter.keychain.views.ToolBar
import kotlinx.android.synthetic.main.activity_main.*


@BindLayout(R.layout.activity_main)
class MainActivity : AbstractActivity<MainPresenter>(), MainView, ToolBar {

    @InjectPresenter lateinit var presenter: MainPresenter

    @ProvidePresenter override fun providePresenter(): MainPresenter {
        super.providePresenter()
        return MainPresenter(appContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getToolBar().setTitle(R.string.app_name)
    }

    override fun visibilityPreloader(visible: Boolean) {
        preloader.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun visibilityToolbar(visible: Boolean) {
        getToolBar().visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun getToolBar(): ToolbarView {
        return this.toolbar
    }

}
