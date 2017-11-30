package com.sprinter.keychain.utils

import org.reactivestreams.Publisher

import java.util.concurrent.Executors

import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.MaybeSource
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object RxUtils {

    val SCHEDULER_SINGLE_THREAD = Schedulers.from(
            Executors.newSingleThreadExecutor())

    fun <R> async(observable: Observable<R>): ObservableSource<R> {
        return observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun <R> async(observable: Single<R>): SingleSource<R> {
        return observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun <R> sync(observable: Single<R>): SingleSource<R> {
        return observable.subscribeOn(SCHEDULER_SINGLE_THREAD)
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun async(completable: Completable): CompletableSource {
        return completable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun <R> async(maybe: Maybe<R>): MaybeSource<R> {
        return maybe.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun <R> async(flowable: Flowable<R>): Publisher<R> {
        return flowable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

}
