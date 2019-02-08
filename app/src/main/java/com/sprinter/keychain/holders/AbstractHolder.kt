package com.sprinter.keychain.holders

import android.support.annotation.IdRes
import android.support.v7.widget.RecyclerView
import android.view.View
import com.sprinter.keychain.adapters.ItemClickListener

abstract class AbstractHolder<D>(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

    private var model: D? = null
    protected var clickListener: ItemClickListener? = null

    open fun bind(model: D) {}

    /**
     * Prepares view holder to be recycled.
     */
    open fun unbind() {}

    fun setData(model: D) {
        this.model = model
        bind(model)
    }

    fun setClickableItems(@IdRes items: IntArray) {
        val listener: View.OnClickListener? = if (clickListener == null) null else this;
        var view: View?

        for (id in items) {
            view = if (id == NO_RES_ID) itemView else itemView.findViewById(id);
            view?.setOnClickListener(listener);
            view?.isClickable = true;
            view?.isEnabled = true;
        }
    }

    open override fun onClick(view: View) {
        clickListener?.onItemClick(adapterPosition, view.id, null)
    }

    open fun setOnItemClick(clickListener: ItemClickListener?, @IdRes items: IntArray) {
        this.clickListener = clickListener
        setClickableItems(items);
    }

    companion object {

        const val NO_RES_ID = -1

    }

}
