package com.sprinter.keychain.presenters

import android.content.DialogInterface
import android.os.Bundle
import android.support.annotation.IdRes
import android.text.TextUtils
import com.arellomobile.mvp.InjectViewState
import com.crashlytics.android.Crashlytics
import com.sprinter.keychain.R
import com.sprinter.keychain.adapters.ItemClickListener
import com.sprinter.keychain.context.Router
import com.sprinter.keychain.dialogs.TupleDialogResult
import com.sprinter.keychain.repositories.keys.KeysRepository
import com.sprinter.keychain.repositories.source.models.Category
import com.sprinter.keychain.repositories.source.models.CategoryItem
import com.sprinter.keychain.utils.RxUtils
import com.sprinter.keychain.views.KeysView
import io.reactivex.CompletableTransformer
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber

@InjectViewState
class KeysPresenter(private val keysRepository: KeysRepository,
        private val toolbarClickSubject: Observable<Int>?, private val router: Router) :
        AbstractPresenter<KeysView>(), ItemClickListener {

    private val items: MutableList<Pair<String, String>> = ArrayList()
    private var categoryItem: CategoryItem? = null
    private var category: Category? = null
    private var categoryId: Long = 0
    private var categoryItemId: Long = 0
    private var categoryItemTitle: String = ""

    private var disposable: Disposable? = null
    private var isChanged = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        visibilityLoading(true)

        keysRepository.getCategoryById(categoryId).compose(RxUtils::async).compose(
                bindUntilDestroy()).subscribe({ result ->
            fetchCategoryItem()
            category = result
            viewState.setTitle(result.title)
        }, this::onError)
    }

    override fun detachView(view: KeysView) {
        super.detachView(view)
        disposable?.dispose()
    }

    override fun attachView(view: KeysView) {
        super.attachView(view)
        disposable = toolbarClickSubject?.subscribe{
            when (it) {
                R.id.toolbarHomeImage -> onBackPressed()
                R.id.wBtnAdd -> onAddPairClick()
                R.id.wBtnDone -> saveCategoryItem()
            }
        }
    }

    fun onBackPressed() {
        if (isChanged ||
            (categoryItem != null && categoryItem?.title != categoryItemTitle) ||
            categoryItem == null && !TextUtils.isEmpty(categoryItemTitle)
        ) {
            viewState.showDialogMessage(
                    R.string.key_dialog_exit_without_saving,
                    R.string.dialog_button_save,
                    R.string.dialog_button_donot_save,
                    0,
                    REQUEST_DIALOG_WITHOUT_SAVE
            )

        } else {
            router.activeBackPress(true)
            viewState.onBackPressed()
        }
    }

    private fun fetchCategoryItem() {
        if (categoryItemId > 0) {
            keysRepository.getCategoryItemById(categoryId, categoryItemId)
                    .compose(RxUtils::async)
                    .compose(bindUntilDestroy())
                    .subscribe({ result ->
                        visibilityLoading(false)
                        categoryItem = result
                        items.clear()
                        items.addAll(result.items)
                        val title = category?.title +
                                    (if (TextUtils.isEmpty(result.title)) "" else "-") + result.title
                        viewState.setTitle(title)
                        viewState.setCategoryTitle(result.title)
                        viewState.setKeyList(result.items)
                    }, this::onError)
        } else {
            visibilityLoading(false)
        }
    }

    private fun visibilityLoading(visible: Boolean) {
        viewState.visibilityLoading(visible)
        viewState.visibilityTitle(!visible)
        viewState.visibilityKeys(!visible)
    }

    private fun saveCategoryItem() {
        visibilityLoading(true)
        isEditMode(false)

        if (categoryItem == null) {
            keysRepository.createCategoryItem(categoryId, categoryItemTitle)
                    .compose(RxUtils::async)
                    .compose(bindUntilDestroy())
                    .flatMapCompletable { categoryItem ->
                        (categoryItem.items as MutableList).addAll(items)
                        keysRepository.updateCategoryItem(categoryItem)
                    }.subscribe({
                        router.activeBackPress(true)
                        viewState.onBackPressed()
                    }, this::onError)
        } else {
            (categoryItem!!.items as MutableList).clear()
            (categoryItem!!.items as MutableList).addAll(items)
            categoryItem?.title = categoryItemTitle
            keysRepository.updateCategoryItem(categoryItem!!)
                    .compose(RxUtils::async)
                    .compose(bindUntilDestroy<CompletableTransformer>())
                    .subscribe({
                        router.activeBackPress(true)
                        viewState.onBackPressed()
                    }, this::onError)
        }
    }

    fun isEditMode(editMode: Boolean) {
        viewState.enabledTitle(editMode)
        viewState.enabledKeysItemsList(editMode)
        viewState.visibilityAddKeysButton(editMode)
        router.activeBackPress(!editMode)
    }

    fun setCategoryId(categoryId: Long) {
        this.categoryId = categoryId
    }

    fun setCategoryItemId(categoryItemId: Long) {
        this.categoryItemId = categoryItemId
    }

    fun setTextTitle(value: String) {
        categoryItemTitle = value
    }

    private fun onAddPairClick() {
        isChanged = true
        items.add(Pair("", ""))
        viewState.setKeyList(items)
    }

    override fun onItemClick(position: Int, @IdRes id: Int, tag: String?) {
        when (id) {
            R.id.liKeyPairDelete -> onDeleteKeyPair(position)
            R.id.liKeyPairCopy -> onCopyKeyValue(position)
        }
    }

    fun pairItemTextChanged(position: Int, text: String, viewId: Int) {
        when (viewId) {
            R.id.liKeyPairValue -> changeKeyPairValue(position, text)
            R.id.liKeyPairKeyType -> changeKeyPairKey(position, text)
        }
    }

    private fun changeKeyPairValue(position: Int, text: String) {
        if (items[position].second != text) {
            items[position] = Pair(items[position].first, text)
            isChanged = true
        }
    }

    private fun changeKeyPairKey(position: Int, text: String) {
        if (items[position].first != text) {
            items[position] = Pair(text, items[position].second)
            isChanged = true
        }
    }

    private fun onDeleteKeyPair(position: Int) {
        val bundle = Bundle()
        bundle.putInt(BUNDLE_DIALOG_KEY_PAIR_POSITION, position)

        viewState.showDialogMessage(
                R.string.key_dialog_delete_keypair,
                R.string.dialog_button_cancel,
                R.string.dialog_button_delete,
                0,
                REQUEST_DIALOG_DELETE_KEY_PAIR,
                bundle
        )
    }

    private fun onCopyKeyValue(position: Int) {
        viewState.copyToClipboard(items[position].first, items[position].second)
        viewState.showToast(R.string.key_item_copied)
    }

    fun onDialogMessageResult(result: TupleDialogResult) {
        if (result.buttonId == DialogInterface.BUTTON_NEGATIVE && result.requestCode == REQUEST_DIALOG_DELETE_KEY_PAIR) {
            val position = result.data?.getInt(BUNDLE_DIALOG_KEY_PAIR_POSITION, -1) ?: -1
            if (position >= 0 && position < items.size) {
                items.removeAt(position)
                viewState.setKeyList(items)
                isChanged = true
            }

        } else if (result.requestCode == REQUEST_DIALOG_WITHOUT_SAVE) {
            if (result.buttonId == DialogInterface.BUTTON_POSITIVE) {
                saveCategoryItem()

            } else {
                router.activeBackPress(true)
                viewState.onBackPressed()
            }
        }
    }

    private fun onError(throwable: Throwable) {
        Timber.e(throwable, "error")
        Crashlytics.logException(throwable)
        viewState.showDialogMessage(R.string.dialog_default_error)
    }

    companion object {

        const val REQUEST_DIALOG_DELETE_KEY_PAIR = 1
        const val REQUEST_DIALOG_WITHOUT_SAVE = 2
        const val BUNDLE_DIALOG_KEY_PAIR_POSITION = "BUNDLE_DIALOG_KEY_PAIR_POSITION"

    }

}
