package com.sprinter.keychain.presenters

import com.arellomobile.mvp.InjectViewState
import com.sprinter.keychain.context.AppContext
import com.sprinter.keychain.utils.RxUtils
import com.sprinter.keychain.views.MainView

@InjectViewState
class MainPresenter(private val appContext: AppContext) : AbstractPresenter<MainView>() {

    private var appContextInitialized: Boolean = false;

    private var pauseTime: Long = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.visibilityToolbar(false)
        viewState.visibilityPreloader(true)
        appContext.hasInitialized()
                .compose(RxUtils::async)
                .compose(bindUntilDestroy())
                .subscribe {
                    appContext.router().openAuthorizationScreen()
                    viewState.visibilityPreloader(false)
                    appContextInitialized = true
                }
    }

    override fun detachView(view: MainView) {
        super.detachView(view)
        pauseTime = System.currentTimeMillis()
    }

    override fun attachView(view: MainView) {
        super.attachView(view)
        if (appContextInitialized && (System.currentTimeMillis() - pauseTime) > WAIT_TIME_MS_FOR_LOCK) {
            appContext.router().openAuthorizationScreen()
        }
    }

    companion object {

        const val WAIT_TIME_MS_FOR_LOCK = 15000

    }

}
