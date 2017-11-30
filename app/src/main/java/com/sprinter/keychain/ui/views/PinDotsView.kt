package com.sprinter.keychain.ui.views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.annotation.AttrRes
import android.support.annotation.IntRange
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout

import com.sprinter.keychain.R

class PinDotsView : LinearLayout {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context,
            attrs, defStyleAttr) {
        init(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) constructor(context: Context, attrs: AttributeSet?,
            @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(context, attrs,
            defStyleAttr, defStyleRes) {
        init(context)
    }

    private fun init(context: Context) {
        weightSum = COUNT_DOTS_WEIGHT.toFloat()
        gravity = Gravity.CENTER_HORIZONTAL
        isBaselineAligned = false
        orientation = LinearLayout.HORIZONTAL

        val size = COUNT_DOTS + 1
        val dotSize = resources.getDimensionPixelSize(R.dimen.pin_dots_size)
        var layoutParams: LinearLayout.LayoutParams
        for (i in 0 until size) {
            layoutParams = LinearLayout.LayoutParams(dotSize, ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f)
            addView(PinCodeDotView(context), layoutParams)
        }
    }

    fun reset() {
        val size = childCount
        for (i in 0 until size) {
            getChildAt(i).isActivated = false
        }
    }

    fun setActivated(@IntRange(from = 0, to = COUNT_DOTS.toLong())
    position: Int, activated: Boolean) {
        getChildAt(position).isActivated = activated
    }

    companion object {

        const val COUNT_DOTS = 5 // from 0 to number of count;
        private val COUNT_DOTS_WEIGHT = COUNT_DOTS + 1 // started from index 1 (+1);
    }

}
