package com.sprinter.keychain.adapters

import android.content.res.Resources
import android.support.annotation.IdRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sprinter.keychain.R
import com.sprinter.keychain.holders.KeyPairHolder

class KeyPairRecyclerAdapter constructor(resource: Resources) :
        AbstractRecyclerAdapter<Pair<String, String>, KeyPairHolder>() {

    interface KeyPairChangeListener {
        fun textChanged(position: Int, text: String, @IdRes viewId: Int)
    }

    private val keyAdapterItems: List<String> = resource.getStringArray(R.array.key_types).asList()
    private var isEditMode = true
    private var keyPairChangeListener: KeyPairChangeListener? = null

    override fun onInitViewHolder(parent: ViewGroup, inflater: LayoutInflater,
            viewType: Int): KeyPairHolder {
        val view: View = inflater.inflate(R.layout.li_key_pair_item, parent, false)

        return KeyPairHolder(view)
    }

    override fun onBindViewHolder(holder: KeyPairHolder, position: Int) {
        holder.setSpinnerItems(keyAdapterItems)
        holder.setKeyPairItemTextChangeListener(keyPairChangeListener)
        holder.setIsEditMode(isEditMode)
        super.onBindViewHolder(holder, position)
    }

    override fun getClickableItems(): IntArray {
        return intArrayOf(R.id.liKeyPairDelete, R.id.liKeyPairCopy)
    }

    fun setIsEditMode(isEditMode: Boolean) {
        this.isEditMode = isEditMode
        notifyDataSetChanged()
    }

    fun setKeyPairItemTextChangeListener(listener: KeyPairRecyclerAdapter.KeyPairChangeListener?) {
        keyPairChangeListener = listener
    }

}
