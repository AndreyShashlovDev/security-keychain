package com.sprinter.keychain.holders

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.sprinter.keychain.R
import com.sprinter.keychain.adapters.ItemClickListener
import com.sprinter.keychain.adapters.KeyPairRecyclerAdapter
import kotlinx.android.synthetic.main.li_key_pair_item.view.*

class KeyPairHolder(itemView: View) : AbstractHolder<Pair<String, String>>(itemView), TextWatcher,
        AdapterView.OnItemSelectedListener {

    private var listener: ItemClickListener? = null
    private var keyPairChangeListener: KeyPairRecyclerAdapter.KeyPairChangeListener? = null
    private val keyTypeAdapter: ArrayAdapter<String> = ArrayAdapter(
            itemView.context,
            R.layout.li_spinner_item
    )

    init {
        keyTypeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
    }

    override fun bind(model: Pair<String, String>) {
        super.bind(model)

        itemView.liKeyPairValue.removeTextChangedListener(this)
        itemView.liKeyPairValue.addTextChangedListener(this)

        itemView.liKeyPairKeyType.onItemSelectedListener = this
        itemView.liKeyPairDelete.setOnClickListener(this)

        itemView.liKeyPairCopy.setOnClickListener({ view ->
            itemView.swipeKeyItem.close()
            listener?.onItemClick(adapterPosition, view.id, null)
        })

        itemView.liKeyPairKeyType.adapter = keyTypeAdapter
        val pos = keyTypeAdapter.getPosition(model.first)

        if (pos >= 0) {
            itemView.liKeyPairKeyType.setSelection(pos)
        }
        itemView.liKeyPairKeyTypeLabel.text = model.first
        itemView.liKeyPairValue.setText(model.second)
    }

    override fun onClick(view: View) {
        itemView.swipeKeyItem.close()
        super.onClick(view)
    }

    override fun unbind() {
        itemView.liKeyPairValue.removeTextChangedListener(this)
        itemView.liKeyPairKeyType.onItemSelectedListener = null

        super.unbind()
    }

    fun setSpinnerItems(item: List<String>) {
        keyTypeAdapter.clear()
        keyTypeAdapter.addAll(item)
        keyTypeAdapter.notifyDataSetChanged()
    }

    fun setIsEditMode(isEditMode: Boolean) {
        itemView.liKeyPairKeyType.visibility = if (isEditMode) View.VISIBLE else View.GONE
        itemView.liKeyPairKeyTypeLabel.visibility = if (isEditMode) View.GONE else View.VISIBLE
        itemView.liKeyPairValue?.isEnabled = isEditMode
        itemView.liKeyPairValue?.inputType = if (isEditMode) InputType.TYPE_CLASS_TEXT else InputType.TYPE_NULL
        itemView.liKeyPairDelete.visibility = if (isEditMode) View.VISIBLE else View.GONE
    }

    fun setKeyPairItemTextChangeListener(listener: KeyPairRecyclerAdapter.KeyPairChangeListener?) {
        keyPairChangeListener = listener
    }

    override fun afterTextChanged(s: Editable?) {
        keyPairChangeListener?.textChanged(adapterPosition, s.toString(),
                itemView.liKeyPairValue.id)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        keyPairChangeListener?.textChanged(
                adapterPosition,
                keyTypeAdapter.getItem(position),
                itemView.liKeyPairKeyType.id
        )
    }

}
