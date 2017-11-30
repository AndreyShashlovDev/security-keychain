package com.sprinter.keychain.context

import com.sprinter.keychain.managers.authorization.AuthorizationManager
import com.sprinter.keychain.repositories.Repositories
import io.reactivex.Observable

interface AppContext {

    fun router(): Router

    fun authorizationManager(): AuthorizationManager

    fun hasInitialized(): Observable<AppContext>

    fun getRepositories() : Repositories

}
