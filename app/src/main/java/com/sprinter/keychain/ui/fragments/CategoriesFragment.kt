package com.sprinter.keychain.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.Gravity
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.sprinter.keychain.R
import com.sprinter.keychain.adapters.CategoriesRecyclerAdapter
import com.sprinter.keychain.context.Router
import com.sprinter.keychain.dialogs.TupleDialogResult
import com.sprinter.keychain.presenters.CategoriesPresenter
import com.sprinter.keychain.repositories.source.models.Category
import com.sprinter.keychain.ui.views.ToolbarView
import com.sprinter.keychain.utils.BindLayout
import com.sprinter.keychain.views.CategoriesView
import kotlinx.android.synthetic.main.fmt_caterories.*

@BindLayout(R.layout.fmt_caterories)
class CategoriesFragment : AbstractFragment<CategoriesPresenter>(), CategoriesView {

    @InjectPresenter internal lateinit var presenter: CategoriesPresenter
    private val categoryAdapter: CategoriesRecyclerAdapter = CategoriesRecyclerAdapter()

    @ProvidePresenter override fun providePresenter(): CategoriesPresenter {
        super.providePresenter()

        val router: Router = appContext.router()
        val userRepository = appContext.getRepositories().getKeysRepository()

        return CategoriesPresenter(router, userRepository, getToolBar()?.viewClickedSubject())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoriesRecycler.layoutManager = LinearLayoutManager(context)
        categoriesRecycler.adapter = categoryAdapter
        categoryAdapter.setClickListener(presenter)
        categoryAdapter.setNestedItemClickListener(presenter)
    }

    override fun onResume() {
        super.onResume()
        getToolBar()?.visibility = View.VISIBLE
        getToolBar()?.visibilityHomeButton(false)
        getToolBar()?.setStateHomeButton(ToolbarView.STATE_HOME_BUTTON_HOME)
        getToolBar()?.setTitle(R.string.app_name)
    }

    override fun visibilityLoading(visible: Boolean) {
        loading.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun setCategoriesList(categories: List<Category>) {
        categoryAdapter.data = categories
    }

    override fun visibilityEmptyListMessage(visible: Boolean) {
        liCategoryEmptyListMessage?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @SuppressLint("RestrictedApi") override fun showMenu(position: Int, @IdRes id: Int) {
        val holder = categoriesRecycler.findViewHolderForAdapterPosition(position) ?: return

        val view: View = holder.itemView.findViewById(id) ?: return

        val popup = PopupMenu(context!!, view)
        popup.menuInflater.inflate(R.menu.category_menu, popup.menu)

        popup.setOnMenuItemClickListener { item -> presenter.onMenuClick(position, item.itemId) }

        val menuHelper = MenuPopupHelper(context!!, popup.menu as MenuBuilder, view)
        menuHelper.setForceShowIcon(true)
        menuHelper.gravity = Gravity.END
        menuHelper.show()
    }

    override fun onDialogMessageResult(result: TupleDialogResult) {
        presenter.onDialogMessageResult(result)
    }

    companion object {

        val FRAGMENT_TAG: String
            get() = CategoriesFragment::class.java.simpleName

        fun newInstance(): CategoriesFragment = CategoriesFragment()

    }

}