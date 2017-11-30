package com.sprinter.keychain.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.BaseSwipeAdapter
import com.sprinter.keychain.R
import kotlinx.android.synthetic.main.li_category_item.view.*


class CategoryItemListAdapter(val items: MutableList<String>) : BaseSwipeAdapter() {

    private var listener: ItemClickListener? = null

    fun setItemClickListener(listener: ItemClickListener?) {
        this.listener = listener;
    }

    override fun fillValues(position: Int, rootView: View?) {
        if (rootView == null) {
            return
        }

        rootView.liCategoryItemTitle.text = items[position]
        rootView.liCategoryItemContainer.setOnClickListener({ view ->
            if (rootView.swipeCategoryItem.openStatus == SwipeLayout.Status.Close) {
                listener?.onItemClick(getPosition(rootView), view.id, null)
            } else {
                rootView.swipeCategoryItem.close()
            }
        })

        rootView.liCategoryItemDelete.setOnClickListener({ view ->
            rootView.swipeCategoryItem.close()
            listener?.onItemClick(getPosition(rootView), view.id, null)
        })
        rootView.liCategoryItemEdit.setOnClickListener({ view ->
            rootView.swipeCategoryItem.close()
            listener?.onItemClick(getPosition(rootView), view.id, null)
        })
    }

    override fun getSwipeLayoutResourceId(position: Int): Int = R.id.swipeCategoryItem

    override fun generateView(position: Int, parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.li_category_item, null)
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): String {
        return items[position]
    }

    override fun getItemId(position: Int): Long = position.toLong()

    private fun getPosition(view: View): Int {
        val viewGroup = view.parent as ViewGroup
        val size = viewGroup.childCount
        var child: View

        for (i in 0 until size) {
            child = viewGroup.getChildAt(i)
            if (child === view) {
                return i
            }
        }
        return -1
    }

}