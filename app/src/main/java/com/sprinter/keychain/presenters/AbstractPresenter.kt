package com.sprinter.keychain.presenters

import android.support.annotation.CheckResult

import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import com.trello.rxlifecycle2.LifecycleTransformer
import com.trello.rxlifecycle2.RxLifecycle

import io.reactivex.subjects.BehaviorSubject

abstract class AbstractPresenter<View : MvpView> : MvpPresenter<View>() {

    private var mLifecycleSubject = BehaviorSubject.create<PresenterEvent>()

    private enum class PresenterEvent {
        DETACH_VIEW, DESTROY
    }

    @CheckResult protected fun <T> bindUntilDestroy(): LifecycleTransformer<T> {
        return RxLifecycle.bindUntilEvent(mLifecycleSubject, PresenterEvent.DESTROY)
    }

    @CheckResult protected fun <T> bindUntilDetach(): LifecycleTransformer<T> {
        return RxLifecycle.bindUntilEvent(mLifecycleSubject, PresenterEvent.DETACH_VIEW)
    }

    override fun detachView(view: View) {
        mLifecycleSubject.onNext(PresenterEvent.DETACH_VIEW)
        super.detachView(view)
    }

    override fun attachView(view: View) {
        super.attachView(view)
        if (!mLifecycleSubject.hasObservers()) {
            mLifecycleSubject = BehaviorSubject.create()
        }
    }

    override fun onDestroy() {
        mLifecycleSubject.onNext(PresenterEvent.DESTROY)
        super.onDestroy()
    }

}
