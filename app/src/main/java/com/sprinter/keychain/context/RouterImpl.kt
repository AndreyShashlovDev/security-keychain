package com.sprinter.keychain.context

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.support.annotation.AnimRes
import android.support.annotation.AnimatorRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.sprinter.keychain.R
import com.sprinter.keychain.ui.fragments.AuthorizationFragment
import com.sprinter.keychain.ui.fragments.CategoriesFragment
import com.sprinter.keychain.ui.fragments.KeysFragment
import com.sprinter.keychain.utils.AndroidUtils
import com.sprinter.keychain.views.ActivityBaseView
import timber.log.Timber

internal class RouterImpl(context: Context) : Router, Application.ActivityLifecycleCallbacks,
        ActivityBaseView.OnBackPressedListener {

    private var activityOnTop: AppCompatActivity? = null;
    private var isBackPressActive: Boolean = true

    init {
        val appDelegate = context.applicationContext as AppDelegate
        appDelegate.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityPaused(activity: Activity?) {}
    override fun onActivityStarted(activity: Activity?) {}
    override fun onActivityDestroyed(activity: Activity?) {}
    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        setUpTopActivity(activity as AppCompatActivity)
    }

    override fun onActivityResumed(activity: Activity?) {
        setUpTopActivity(activity as AppCompatActivity)
    }

    override fun onActivityStopped(activity: Activity?) {
        if (activity is ActivityBaseView) {
            (activity as ActivityBaseView).setOnBackPressedListener(null)
        }
    }

    private fun setUpTopActivity(activity: AppCompatActivity?) {
        if (activity is ActivityBaseView) {
            (activity as ActivityBaseView).setOnBackPressedListener(this)
        }
        activityOnTop = activity
    }

    override fun onBackPressed(): Boolean {
        if (!isBackPressActive) {
            return false
        }

        val manager = activityOnTop?.supportFragmentManager
        val backStackEntryCount = manager?.backStackEntryCount ?: 0

        val view = activityOnTop?.currentFocus
        view?.clearFocus()

        if (backStackEntryCount < 2) {
            activityOnTop?.supportFinishAfterTransition()
            return false

        }

        return true
    }

    override fun activeBackPress(active: Boolean) {
        isBackPressActive = active
    }

    private fun changeFragment(fragment: Fragment, tag: String, clearBackStack: Boolean,
            replace: Boolean = true, @AnimatorRes @AnimRes enter: Int = 0, @AnimatorRes @AnimRes
            exit: Int = 0, @AnimatorRes @AnimRes popEnter: Int = 0, @AnimatorRes @AnimRes
            popExit: Int = 0) {
        try {
            val manager = activityOnTop?.supportFragmentManager ?: return

            if (clearBackStack) {
                try {
                    manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

                } catch (e: IllegalStateException) {
                    Timber.e(e.message)
                }
            }

            val transaction = manager.beginTransaction().addToBackStack(tag)

            transaction.setCustomAnimations(enter, exit, popEnter, popExit)

            if (replace) {
                transaction.replace(R.id.liCategoryItemContainer, fragment, tag)
            } else {
                transaction.add(R.id.liCategoryItemContainer, fragment, tag)
            }
            transaction.commitAllowingStateLoss()
            AndroidUtils.hideSoftKeyboard(activityOnTop!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun openCategoriesScreen() {
        changeFragment(CategoriesFragment.newInstance(), CategoriesFragment.FRAGMENT_TAG, true,
                false)
    }

    override fun openAuthorizationScreen() {
        changeFragment(AuthorizationFragment.newInstance(), AuthorizationFragment.FRAGMENT_TAG,
                true, false)
    }

    override fun openEditKeysScreen(categoryId: Long, categoryItemId: Long) {
        changeFragment(KeysFragment.newInstance(true, categoryId, categoryItemId),
                KeysFragment.FRAGMENT_TAG, false, true, R.anim.anim_out_bottom_to_top,
                R.anim.anim_top_to_top_out, R.anim.anim_top_out_to_top,
                R.anim.anim_top_to_bottom_out)
    }

    override fun openViewKeysScreen(categoryId: Long, categoryItemId: Long) {
        changeFragment(KeysFragment.newInstance(false, categoryId, categoryItemId),
                KeysFragment.FRAGMENT_TAG, false, true, R.anim.anim_right_to_left,
                R.anim.anim_right_right_out, R.anim.anim_out_left_to_left,
                R.anim.anim_left_to_right_out)
    }

    override fun openCreateKeysScreen(categoryId: Long) {
        changeFragment(KeysFragment.newInstance(true, categoryId, 0), KeysFragment.FRAGMENT_TAG,
                false, true, R.anim.anim_right_to_left, R.anim.anim_right_right_out,
                R.anim.anim_out_left_to_left, R.anim.anim_left_to_right_out)
    }

}
