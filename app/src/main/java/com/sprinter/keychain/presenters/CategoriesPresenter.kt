package com.sprinter.keychain.presenters

import android.content.DialogInterface
import android.os.Bundle
import android.support.annotation.IdRes
import android.text.TextUtils
import com.arellomobile.mvp.InjectViewState
import com.crashlytics.android.Crashlytics
import com.sprinter.keychain.R
import com.sprinter.keychain.adapters.ItemClickListener
import com.sprinter.keychain.adapters.NestedItemClickListener
import com.sprinter.keychain.context.Router
import com.sprinter.keychain.dialogs.InputTextDialogFragment
import com.sprinter.keychain.dialogs.TupleDialogResult
import com.sprinter.keychain.repositories.keys.KeysRepository
import com.sprinter.keychain.repositories.source.models.Category
import com.sprinter.keychain.utils.RxUtils
import com.sprinter.keychain.views.CategoriesView
import io.reactivex.CompletableTransformer
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber

@InjectViewState
class CategoriesPresenter(private val router: Router, private val keysRepository: KeysRepository,
        private val toolBarClickSubject: Observable<Int>?) : AbstractPresenter<CategoriesView>(),
        NestedItemClickListener, ItemClickListener {

    private val categories: MutableList<Category> = ArrayList()

    private var disposable: Disposable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.visibilityEmptyListMessage(false)
        viewState.visibilityLoading(true)

        keysRepository.loadKeys()
                .compose(RxUtils::async)
                .compose(bindUntilDestroy())
                .doFinally { viewState.visibilityLoading(false) }
                .subscribe(this::updateCategoryList, this::onError)

        keysRepository.syncCategory()
                .compose(RxUtils::async)
                .compose(bindUntilDestroy())
                .subscribe(this::updateCategoryList)
    }

    override fun detachView(view: CategoriesView) {
        super.detachView(view)
        disposable?.dispose()
    }

    override fun attachView(view: CategoriesView) {
        super.attachView(view)
        disposable = toolBarClickSubject?.subscribe{
            when (it) {
                R.id.wBtnAdd -> onAddCategoryClick()
            }
        }
    }

    override fun onItemClick(rootPosition: Int, subPosition: Int, id: Int, tag: String?) {
        when (id) {
            R.id.liCategoryItemEdit -> router.openEditKeysScreen(
                    categories[rootPosition].id,
                    categories[rootPosition].items[subPosition].id
            )
            R.id.liCategoryItemDelete -> onDeleteCategoryItem(rootPosition, subPosition)
            R.id.liCategoryItemContainer -> router.openViewKeysScreen(
                    categories[rootPosition].id,
                    categories[rootPosition].items[subPosition].id
            )
        }
    }

    private fun onDeleteCategoryItem(rootPosition: Int, subPosition: Int) {
        val bundle = Bundle()
        bundle.putInt(BUNDLE_DIALOG_CATEGORY_POSITION, rootPosition)
        bundle.putInt(BUNDLE_DIALOG_CATEGORY_ITEM_POSITION, subPosition)
        viewState.showDialogMessage(
                R.string.category_dialog_item_delete,
                R.string.dialog_button_cancel,
                R.string.dialog_button_delete,
                0,
                REQUEST_DIALOG_DELETE_ITEM_CATEGORY,
                bundle
        )
    }

    override fun onItemClick(position: Int, @IdRes id: Int, tag: String?) {
        when (id) {
            R.id.liCategoryMenu -> viewState.showMenu(position, id)
        }
    }

    fun onMenuClick(position: Int, menuId: Int): Boolean {
        val bundle = Bundle()
        bundle.putInt(BUNDLE_DIALOG_CATEGORY_POSITION, position)

        if (categories.size > position && position > -1) {
            when (menuId) {
                R.id.menuAdd -> onAddCategoryItemClick(position)
                R.id.menuEdit -> onEditCategoryClick(bundle, position)
                R.id.menuDelete -> onCategoryDelete(bundle)
            }
        }
        return true
    }

    private fun onAddCategoryItemClick(position: Int) {
        router.openCreateKeysScreen(categories[position].id)
    }

    private fun onEditCategoryClick(bundle: Bundle, position: Int) {
        viewState.showDialogInput(
                R.string.category_dialog_create,
                categories[position].title,
                CATEGORY_NAME_MAX_LENGTH,
                REQUEST_DIALOG_EDIT_CATEGORY,
                bundle
        )
    }

    private fun onCategoryDelete(bundle: Bundle) {
        viewState.showDialogMessage(
                R.string.category_dialog_delete,
                R.string.dialog_button_cancel,
                R.string.dialog_button_delete,
                0,
                REQUEST_DIALOG_DELETE_CATEGORY,
                bundle
        )
    }

    private fun onAddCategoryClick() {
        viewState.showDialogInput(
                R.string.category_dialog_create,
                "",
                CATEGORY_NAME_MAX_LENGTH,
                REQUEST_DIALOG_CREATE_CATEGORY
        )
    }

    private fun updateCategoryList(categories: List<Category>) {
        this.categories.clear()
        this.categories.addAll(categories)
        viewState.setCategoriesList(this.categories)
        viewState.visibilityEmptyListMessage(categories.isEmpty())
    }

    fun onDialogMessageResult(result: TupleDialogResult) {
        if (result.requestCode == REQUEST_DIALOG_CREATE_CATEGORY && result.buttonId == DialogInterface.BUTTON_POSITIVE) {
            val text = result.data?.getString(InputTextDialogFragment.ARGS_VALUE)

            if (!TextUtils.isEmpty(text)) {
                keysRepository.createCategory(text!!)
                        .compose(RxUtils::async)
                        .compose(bindUntilDestroy())
                        .subscribe({}, this::onError)
            }

        } else if (result.requestCode == REQUEST_DIALOG_EDIT_CATEGORY && result.buttonId == DialogInterface.BUTTON_POSITIVE) {
            val text = result.data?.getString(InputTextDialogFragment.ARGS_VALUE)
            val position = result.data?.getInt(BUNDLE_DIALOG_CATEGORY_POSITION, -1) ?: -1

            if (!TextUtils.isEmpty(text) && position >= 0 && position < categories.size) {
                categories[position].title = text!!
                keysRepository.updateCategory(categories[position])
                        .compose(RxUtils::async)
                        .compose(bindUntilDestroy<CompletableTransformer>())
                        .subscribe({}, this::onError)
            }
        } else if (result.requestCode == REQUEST_DIALOG_DELETE_CATEGORY && result.buttonId == DialogInterface.BUTTON_NEGATIVE) {
            val position = result.data?.getInt(BUNDLE_DIALOG_CATEGORY_POSITION, -1) ?: -1

            if (position >= 0 && position < categories.size) {
                keysRepository.removeCategory(categories[position].id)
                        .compose(RxUtils::async)
                        .compose(bindUntilDestroy<CompletableTransformer>())
                        .subscribe({}, this::onError)
            }

        } else if (result.requestCode == REQUEST_DIALOG_DELETE_ITEM_CATEGORY && result.buttonId == DialogInterface.BUTTON_NEGATIVE) {
            val position = result.data?.getInt(BUNDLE_DIALOG_CATEGORY_POSITION, -1) ?: -1
            val subPosition = result.data?.getInt(BUNDLE_DIALOG_CATEGORY_ITEM_POSITION, -1) ?: -1

            if (position >= 0 &&
                position < categories.size &&
                subPosition >= 0 &&
                subPosition < categories[position].items.size
            ) {
                keysRepository.removeCategoryItem(
                        categories[position].id,
                        categories[position].items[subPosition].id
                )
                        .compose(RxUtils::async)
                        .compose(bindUntilDestroy<CompletableTransformer>())
                        .subscribe({}, this::onError)
            }
        }
    }

    private fun onError(throwable: Throwable) {
        Timber.e(throwable, "error")
        Crashlytics.logException(throwable)
        viewState.showDialogMessage(R.string.dialog_default_error)
    }

    companion object {
        const val REQUEST_DIALOG_CREATE_CATEGORY = 1
        const val REQUEST_DIALOG_EDIT_CATEGORY = 2
        const val REQUEST_DIALOG_DELETE_CATEGORY = 3
        const val REQUEST_DIALOG_DELETE_ITEM_CATEGORY = 4

        const val CATEGORY_NAME_MAX_LENGTH = 32

        private const val BUNDLE_DIALOG_CATEGORY_POSITION = "BUNDLE_DIALOG_CATEGORY_POSITION"
        private const val BUNDLE_DIALOG_CATEGORY_ITEM_POSITION = "BUNDLE_DIALOG_CATEGORY_ITEM_POSITION"
    }

}
