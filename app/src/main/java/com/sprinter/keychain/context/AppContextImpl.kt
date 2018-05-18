package com.sprinter.keychain.context

import android.content.Context
import com.sprinter.keychain.managers.authorization.AuthorizationManager
import com.sprinter.keychain.managers.authorization.AuthorizationManagerFactory
import com.sprinter.keychain.repositories.Repositories
import com.sprinter.keychain.repositories.RepositoriesFactory
import com.sprinter.keychain.utils.RxUtils
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class AppContextImpl constructor(val context: Context) : AppContext {

    private val behaviorSubject: BehaviorSubject<AppContext> = BehaviorSubject.create<AppContext>()

    private val router: Router = RouterFactory.createDefault(context)
    private lateinit var authorizationManager: AuthorizationManager
    private lateinit var repositories: Repositories;

    init {
        Completable.fromAction({
            authorizationManager = AuthorizationManagerFactory.createDefault(context)
            repositories = RepositoriesFactory.createDefault(context)
            behaviorSubject.onNext(this)
        }).compose(RxUtils::async).subscribe()
    }

    override fun hasInitialized(): Observable<AppContext> {
        return behaviorSubject.hide()
    }

    override fun router(): Router {
        return router
    }

    override fun authorizationManager(): AuthorizationManager {
        return authorizationManager
    }

    override fun getRepositories(): Repositories {
        return repositories
    }

}
