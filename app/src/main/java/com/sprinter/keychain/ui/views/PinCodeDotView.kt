package com.sprinter.keychain.ui.views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout

import com.sprinter.keychain.R

class PinCodeDotView : FrameLayout {

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
        val view = View(context)
        view.setBackgroundResource(R.drawable.bg_pin_dot)
        val size = resources.getDimensionPixelSize(R.dimen.pin_dots_size)

        val layoutParams = FrameLayout.LayoutParams(size, size, Gravity.CENTER)

        this.addView(view, layoutParams)
    }

    override fun setActivated(activated: Boolean) {
        super.setActivated(activated)
        getChildAt(0).isActivated = activated
    }

}
