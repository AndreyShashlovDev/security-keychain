package com.sprinter.keychain.ui.views

import android.animation.ObjectAnimator
import android.content.Context
import android.support.annotation.IntDef
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.drawable.DrawerArrowDrawable
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.sprinter.keychain.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.w_toolbar.view.*

class ToolbarView : AppBarLayout {

    private val mViewClickedSubject = PublishSubject.create<Int>()
    private val mViewClickedSubjectHide = mViewClickedSubject.hide()
    private lateinit var mHomeButtonDrawable: DrawerArrowDrawable
    @ButtonHomeSate private var mLastStateButtonHome: Int = STATE_HOME_BUTTON_HOME
    private lateinit var view: ViewGroup

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    @IntDef(STATE_HOME_BUTTON_HOME.toLong(),
            STATE_HOME_BUTTON_BACK.toLong()) annotation class ButtonHomeSate

    private fun init(context: Context) {
        view = View.inflate(context, R.layout.w_toolbar, this) as ViewGroup

        mHomeButtonDrawable = DrawerArrowDrawable(context)

        mHomeButtonDrawable.color = ContextCompat.getColor(context, android.R.color.white)
        view.toolbarHomeImage.setImageDrawable(mHomeButtonDrawable)

        setStateHomeButton(STATE_HOME_BUTTON_HOME)

        setItemsOnClickListener(OnClickListener { view -> mViewClickedSubject.onNext(view.id) })
    }

    private fun setItemsOnClickListener(listener: View.OnClickListener?) {
        val container: ViewGroup = view.findViewById(R.id.toolbarContainer)
        val childCount = container.childCount
        var child: View
        for (i in 0 until childCount) {
            child = container.getChildAt(i)
            child.setOnClickListener(listener)
        }
    }

    fun visibilityHomeButton(visible: Boolean) {
        view.toolbarHomeImage.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun setStateHomeButton(@ButtonHomeSate state: Int) {
        if (state != mLastStateButtonHome) {
            ObjectAnimator.ofFloat(mHomeButtonDrawable, VIEW_PROPERTY_PROGRESS,
                    if (mLastStateButtonHome == STATE_HOME_BUTTON_HOME) 1f else 0f).start()
        }
        mLastStateButtonHome = state
    }

    fun setTitle(@StringRes resId: Int) {
        view.toolbarTitle.setText(resId)
    }

    fun setTitle(title: String) {
        view.toolbarTitle.text = title
    }

    fun setView(@LayoutRes resId: Int) {
        val insertView: View = View.inflate(context, resId, null)
        insertView.tag = resId
        val container: ViewGroup = view.findViewById(R.id.toolbarContainer)
        container.addView(insertView)
        setItemsOnClickListener(OnClickListener { view -> mViewClickedSubject.onNext(view.id) })
    }

    fun removeView(@LayoutRes resId: Int) {
        val container: ViewGroup = view.findViewById(R.id.toolbarContainer)
        val removeView: View? = container.findViewWithTag(resId)
        if (removeView != null) {
            removeView.setOnClickListener(null)
            container.removeView(removeView)
        }
    }

    fun viewClickedSubject(): Observable<Int> {
        return mViewClickedSubjectHide
    }

    override fun onDetachedFromWindow() {
        mViewClickedSubject.onComplete()
        setItemsOnClickListener(null)
        super.onDetachedFromWindow()
    }

    companion object {

        const val STATE_HOME_BUTTON_HOME = 0
        const val STATE_HOME_BUTTON_BACK = 1

        private const val VIEW_PROPERTY_PROGRESS = "progress"

    }

}
