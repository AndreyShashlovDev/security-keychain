package com.sprinter.keychain.repositories.source.models

import java.io.Serializable

class CategoryItem constructor(val id: Long, val categoryId: Long, var title: String,
        val items: List<Pair<String, String>>) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) return false

        other as CategoryItem

        if (id != other.id) return false
        if (categoryId != other.categoryId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + categoryId.hashCode()
        return result
    }

}