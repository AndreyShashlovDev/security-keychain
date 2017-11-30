package com.sprinter.keychain.repositories.keys

import com.sprinter.keychain.repositories.source.models.Category
import com.sprinter.keychain.repositories.source.models.CategoryItem
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface KeysRepository {

    fun syncCategory(): Observable<List<Category>>

    fun loadKeys(): Single<List<Category>>

    fun createCategory(title: String): Single<Category>

    fun updateCategory(category: Category): Completable

    fun removeCategory(id: Long): Completable

    fun getCategoryById(id: Long): Single<Category>

    fun createCategoryItem(categoryId: Long, title: String): Single<CategoryItem>

    fun updateCategoryItem(item: CategoryItem): Completable

    fun removeCategoryItem(categoryId: Long, itemId: Long): Completable

    fun getCategoryItemById(categoryId: Long, categoryItemId: Long): Single<CategoryItem>

}
