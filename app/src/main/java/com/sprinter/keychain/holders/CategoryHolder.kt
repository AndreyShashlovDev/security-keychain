package com.sprinter.keychain.holders

import android.view.View
import com.sprinter.keychain.R
import com.sprinter.keychain.adapters.CategoryItemListAdapter
import com.sprinter.keychain.adapters.ItemClickListener
import com.sprinter.keychain.adapters.NestedItemClickListener
import com.sprinter.keychain.repositories.source.models.Category
import com.sprinter.keychain.repositories.source.models.CategoryItem
import kotlinx.android.synthetic.main.li_category.view.*

class CategoryHolder constructor(itemView: View) : AbstractHolder<Category>(itemView),
        ItemClickListener {

    private val itemsAdapter: CategoryItemListAdapter = CategoryItemListAdapter(ArrayList());

    private var listener: NestedItemClickListener? = null

    override fun bind(model: Category) {
        super.bind(model)
        itemView.liCategoryTitle.text = model.title

        itemView.liCategoryItems.adapter = itemsAdapter

        itemsAdapter.items.clear()

        for (item: CategoryItem in model.items) {
            itemsAdapter.items.add(item.title)
        }
        itemView.liCategoryEmptyListMessage.visibility = if (itemsAdapter.count <= 0) View.VISIBLE else View.GONE

        itemsAdapter.notifyDataSetChanged()
        itemsAdapter.setItemClickListener(this)
    }

    override fun onItemClick(position: Int, id: Int, tag: String?) {
        listener?.onItemClick(adapterPosition, position, id, tag)
    }

    fun setNestedItemClickListener(listener: NestedItemClickListener?) {
        this.listener = listener;
    }

    override fun unbind() {
        super.unbind()
        listener = null
        itemsAdapter.setItemClickListener(null)
    }

    fun expanded(expand: Boolean) {
        itemView.liCategoryExpand
                .setImageResource(if (expand) R.drawable.ic_expand_less else R.drawable.ic_expand_more)

        itemView.liCategoryListContainer.visibility = if (expand) View.VISIBLE else View.GONE
    }

}
