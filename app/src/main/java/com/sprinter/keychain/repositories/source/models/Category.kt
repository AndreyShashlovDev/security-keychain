package com.sprinter.keychain.repositories.source.models

class Category constructor(val id: Long, var title: String, val items: List<CategoryItem>) {

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) return false

        other as Category

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = id.hashCode()

}
