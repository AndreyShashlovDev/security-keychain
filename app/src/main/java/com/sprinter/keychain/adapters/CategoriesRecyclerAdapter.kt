package com.sprinter.keychain.adapters

import android.support.annotation.IdRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sprinter.keychain.R
import com.sprinter.keychain.holders.CategoryHolder
import com.sprinter.keychain.repositories.source.models.Category

class CategoriesRecyclerAdapter :
        AbstractRecyclerAdapter<Category, CategoryHolder>(emptyList<Category>()) {

    private val expanded: MutableSet<Int> = HashSet()

    private var listener: NestedItemClickListener? = null

    fun setNestedItemClickListener(listener: NestedItemClickListener?) {
        this.listener = listener;
    }

    override fun onInitViewHolder(parent: ViewGroup, inflater: LayoutInflater,
            viewType: Int): CategoryHolder {
        val view: View = inflater.inflate(R.layout.li_category, parent, false)

        return CategoryHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.expanded(expanded.contains(position))
        holder.setNestedItemClickListener(listener)
    }

    override fun getClickableItems(): IntArray {
        return CLICKABLE_LAYOUT_ITEMS
    }

    override fun onItemClick(position: Int, @IdRes id: Int, tag: String?) {
        if (id == R.id.liCategoryExpand || id == R.id.liCategoryTitle) {
            if (expanded.contains(position)) {
                expanded.remove(position)
            } else {
                expanded.add(position)
            }
            notifyItemChanged(position)

        } else {
            super.onItemClick(position, id, tag)
        }

    }

    companion object {

        @IdRes private val CLICKABLE_LAYOUT_ITEMS = intArrayOf(
                R.id.liCategoryExpand,
                R.id.liCategoryTitle,
                R.id.liCategoryMenu
        )

    }

}