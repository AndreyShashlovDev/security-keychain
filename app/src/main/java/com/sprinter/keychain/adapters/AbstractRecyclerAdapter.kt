package com.sprinter.keychain.adapters


import android.support.annotation.IdRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.sprinter.keychain.holders.AbstractHolder
import java.util.*

abstract class AbstractRecyclerAdapter<D, T : AbstractHolder<D>> constructor(
        data: List<D> = emptyList<D>()) : RecyclerView.Adapter<T>(), ItemClickListener {

    private var mData: List<D>
    private var clickListener: ItemClickListener? = null

    var data: List<D>
        get() = mData
        set(mData) {
            this.mData = Collections.unmodifiableList(mData)
            notifyDataSetChanged()
        }

    init {
        this.mData = Collections.unmodifiableList(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): T {
        val inflater = LayoutInflater.from(parent.context)

        return onInitViewHolder(parent, inflater, viewType)
    }

    protected abstract fun onInitViewHolder(parent: ViewGroup, inflater: LayoutInflater,
            viewType: Int): T

    open override fun onBindViewHolder(holder: T, position: Int) {
        holder.setData(mData[position])
        holder.setOnItemClick(this, getClickableItems())
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    open fun getClickableItems(): IntArray {
        return CLICKABLE_LAYOUT_ITEMS;
    }

    fun clear() {
        this.mData = emptyList()
        notifyDataSetChanged()
    }

    fun addAll(data: MutableList<D>) {
        val dList = ArrayList<D>()
        data.addAll(mData)
        dList.addAll(data)
        this.mData = Collections.unmodifiableList(dList)
        notifyDataSetChanged()
    }

    open fun setClickListener(clickListener: ItemClickListener?) {
        this.clickListener = clickListener
    }

    override fun onViewRecycled(holder: T) {
        holder.unbind()
        holder.setOnItemClick(null, getClickableItems())

        super.onViewRecycled(holder)
    }

    open override fun onItemClick(position: Int, @IdRes id: Int, tag: String?) {
        clickListener?.onItemClick(position, id, tag)
    }

    companion object {

        @IdRes private val CLICKABLE_LAYOUT_ITEMS = intArrayOf(AbstractHolder.NO_RES_ID)

    }

}
