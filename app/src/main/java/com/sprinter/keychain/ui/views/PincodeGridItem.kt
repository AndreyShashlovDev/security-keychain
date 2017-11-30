package com.sprinter.keychain.ui.views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.sprinter.keychain.R
import kotlinx.android.synthetic.main.w_pin_grid_item.view.*

class PincodeGridItem : FrameLayout {

    private lateinit var pincodeView: View
    private var drawableResId: Int = 0;

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
            defStyleAttr) {
        init(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) constructor(context: Context,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private fun init(context: Context) {
        pincodeView = View.inflate(context, R.layout.w_pin_grid_item, this)
    }

    fun setTitle(title: String) {
        pincodeView.liCategoryTitle.text = title
    }

    fun setSubTitle(subTitle: String?) {
        pincodeView.subTitle.text = subTitle
        pincodeView.subTitle.visibility = if (subTitle == null) View.GONE else View.VISIBLE
    }

    fun setDrawable(@DrawableRes drawable: Int) {
        val visibility = if (drawable != 0) View.GONE else View.VISIBLE
        drawableResId = drawable;

        if (drawable != 0) {
            pincodeView.image.setImageResource(drawable)
        }

        pincodeView.liCategoryTitle.setVisibility(visibility)
        pincodeView.subTitle.setVisibility(visibility)
    }

    fun getDrawableResId(): Int = drawableResId

}
