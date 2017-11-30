package com.sprinter.keychain.repositories.keys

import android.support.annotation.WorkerThread
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.sprinter.keychain.repositories.source.models.Category
import com.sprinter.keychain.repositories.source.models.CategoryItem
import com.sprinter.keychain.repositories.source.preferences.Preferences
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import java.lang.Exception
import java.util.concurrent.CopyOnWriteArrayList

internal class KeysRepositoryImpl(private val preferences: Preferences) : KeysRepository {

    data class ResultCategory(val category: Category?, val categories: MutableList<Category>)

    private val gson: Gson = GsonBuilder().create()
    private val typeToken = object : TypeToken<List<Category>>() {}.type
    private val categoryBehavior: BehaviorSubject<List<Category>> = BehaviorSubject.create()

    override fun syncCategory(): Observable<List<Category>> = categoryBehavior.hide()

    @WorkerThread private fun saveKeysToPreference(data: List<Category>) {
        val json = gson.toJson(data, typeToken)
        preferences.put(Preferences.COLLECTION_CATEGORY, json)

        categoryBehavior.onNext(data)
    }

    override fun loadKeys(): Single<List<Category>> {
        return Single.fromCallable({
            try {
                val json = preferences.getString(Preferences.COLLECTION_CATEGORY)
                val result: List<Category> = gson.fromJson<List<Category>>(json,
                        typeToken) ?: ArrayList()
                return@fromCallable result
            } catch (e: Exception) {
                when (e) {
                    is JsonSyntaxException, is JsonParseException, is JsonIOException -> {
                        return@fromCallable ArrayList<Category>()
                    }
                    else -> throw e
                }
            }
        })
    }

    override fun createCategory(title: String): Single<Category> {
        return Single.fromCallable({
            val id = preferences.getLong(Preferences.INCREMENT_CATEGORY) + 1
            val category = Category(id, title, ArrayList<CategoryItem>())

            preferences.put(Preferences.INCREMENT_CATEGORY, id)
            val categories = loadKeys().blockingGet() as MutableList
            categories.add(category)

            saveKeysToPreference(categories)

            return@fromCallable category
        })
    }

    override fun updateCategory(category: Category): Completable {
        return Completable.fromAction({
            val categories = loadKeys().blockingGet() as MutableList
            val pos: Int = categories.indexOf(category)

            if (pos >= 0) {
                categories[pos] = category
                saveKeysToPreference(categories)
            } else {
                throw IllegalArgumentException("category not found")
            }
        })
    }

    override fun removeCategory(id: Long): Completable {
        return Completable.fromAction({
            val result = getCategoryResult(id)
            if (result.category != null) {
                result.categories.remove(result.category!!)
                saveKeysToPreference(result.categories)
            } else {
                throw IllegalArgumentException("category not found")
            }
        })
    }

    override fun getCategoryById(id: Long): Single<Category> {
        return Single.fromCallable({
            val result = getCategoryResult(id)

            return@fromCallable result.category ?: throw IllegalArgumentException(
                    "category id not found")
        })
    }

    @WorkerThread private fun getCategoryResult(id: Long): ResultCategory {
        val categories = loadKeys().blockingGet() as MutableList
        val category: Category? = categories.firstOrNull { it.id == id }

        return ResultCategory(category, categories)
    }

    override fun createCategoryItem(categoryId: Long, title: String): Single<CategoryItem> {
        return Single.fromCallable({
            val result = getCategoryResult(categoryId)

            val category = result.category ?: throw IllegalArgumentException(
                    "category id not found!")

            val id = preferences.getLong(Preferences.INCREMENT_CATEGORY_ITEM) + 1
            val categoryItem = CategoryItem(id, categoryId, title, CopyOnWriteArrayList())

            preferences.put(Preferences.INCREMENT_CATEGORY_ITEM, id)
            (category.items as MutableList).add(categoryItem)

            saveKeysToPreference(result.categories)

            return@fromCallable categoryItem
        })
    }

    override fun updateCategoryItem(item: CategoryItem): Completable {
        return Completable.fromAction({
            val result = getCategoryResult(item.categoryId)
            result.category ?: throw IllegalArgumentException("category id not found!")

            val pos = result.category.items.indexOf(item)
            if (pos > -1) {
                (result.category.items as MutableList)[pos] = item
                saveKeysToPreference(result.categories)
            }
        })
    }

    override fun removeCategoryItem(categoryId: Long, itemId: Long): Completable {
        return Completable.fromAction({
            val result = getCategoryResult(categoryId)
            result.category ?: throw IllegalArgumentException("category id not found!")
            val item: CategoryItem? = result.category.items.single { item -> item.id == itemId }
            (result.category.items as MutableList).remove(item)
            saveKeysToPreference(result.categories)
        })
    }

    override fun getCategoryItemById(categoryId: Long, categoryItemId: Long): Single<CategoryItem> {
        return Single.fromCallable({
            val result = getCategoryResult(categoryId)

            result.category ?: throw IllegalArgumentException("category id not found!")
            val item: CategoryItem? = result.category.items.single { item -> item.id == categoryItemId }
            return@fromCallable item ?: throw IllegalArgumentException(
                    "category item id not found!")
        })
    }

}