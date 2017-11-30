package com.sprinter.keychain.ui.views

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.v7.widget.GridLayout
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.sprinter.keychain.R
import java.util.*

class PinGridView : GridLayout, View.OnClickListener {

    private var mClickListener: OnPinKeyboardClickListener? = null
    private var animationClick: Animation? = null

    interface OnPinKeyboardClickListener {

        fun onPinKeyboardClick(tag: Any?)

    }

    private interface GridItem {

        fun createGridItem(context: Context): PincodeGridItem

    }

    private class TextItem internal constructor(private val title: String,
            private val subTitle: String?,
            private val enabled: Boolean = true) : GridItem {

        override fun createGridItem(context: Context): PincodeGridItem {
            val gridItem = PincodeGridItem(context)
            gridItem.isEnabled = enabled

            gridItem.setTitle(title)
            gridItem.setSubTitle(subTitle)
            gridItem.tag = title

            return gridItem
        }
    }

    private class ImageItem internal constructor(@param:DrawableRes
    @field:DrawableRes private val imageResId: Int, private val enabled: Boolean = true) :
            GridItem {

        override fun createGridItem(context: Context): PincodeGridItem {
            val gridItem = PincodeGridItem(context)
            gridItem.isEnabled = enabled

            if (imageResId != 0) {
                gridItem.setDrawable(imageResId)
            }
            gridItem.tag = imageResId

            return gridItem
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs,
            defStyle) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        if (!isInEditMode) {
            animationClick = AnimationUtils.loadAnimation(context, R.anim.anim_pin_button);
        }

        this.columnCount = 3
        this.rowCount = 4
        this.useDefaultMargins = true

        var gridItem: PincodeGridItem
        var column = 0
        var row = -1

        for (item in GRID_ITEMS) {
            gridItem = item.createGridItem(context)

            if (column % 3 == 0) {
                column = 0
                row++
            }

            val layoutParams = GridLayout.LayoutParams(GridLayout.spec(row, 1f),
                    GridLayout.spec(column, 1f))
            layoutParams.width = 0
            layoutParams.height = 0

            addView(gridItem, layoutParams)
            gridItem.setOnClickListener(this)
            column++
        }
    }

    fun setOnPincodeKeyboardListener(listener: OnPinKeyboardClickListener?) {
        mClickListener = listener
    }

    override fun onClick(view: View) {
        if (mClickListener != null && isEnabled) {
            view.startAnimation(animationClick)
            mClickListener!!.onPinKeyboardClick(view.tag)
        }
    }

    companion object {

        private val GRID_ITEMS: MutableList<GridItem>

        init {
            GRID_ITEMS = ArrayList()

            GRID_ITEMS.add(TextItem("1", ""))
            GRID_ITEMS.add(TextItem("2", "abc"))
            GRID_ITEMS.add(TextItem("3", "def"))
            GRID_ITEMS.add(TextItem("4", "ghi"))
            GRID_ITEMS.add(TextItem("5", "jkl"))
            GRID_ITEMS.add(TextItem("6", "mno"))
            GRID_ITEMS.add(TextItem("7", "pqrs"))
            GRID_ITEMS.add(TextItem("8", "tuv"))
            GRID_ITEMS.add(TextItem("9", "wxyz"))
            GRID_ITEMS.add(ImageItem(R.drawable.ic_fingerprint, false))
            GRID_ITEMS.add(TextItem("0", null))
            GRID_ITEMS.add(ImageItem(R.drawable.ic_backspace))
        }
    }

    fun hideFingerprint() {
        val size: Int = childCount;
        var view: View?
        for (i: Int in 0..size) {
            view = getChildAt(i)
            if (view is PincodeGridItem) {
                if (view.getDrawableResId() == R.drawable.ic_fingerprint) {
                    view.visibility = View.INVISIBLE
                }
            }
        }
    }

}
