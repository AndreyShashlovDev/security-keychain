package com.sprinter.keychain.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.sprinter.keychain.R
import com.sprinter.keychain.adapters.ItemClickListener
import com.sprinter.keychain.adapters.KeyPairRecyclerAdapter
import com.sprinter.keychain.dialogs.TupleDialogResult
import com.sprinter.keychain.presenters.KeysPresenter
import com.sprinter.keychain.ui.views.ToolbarView
import com.sprinter.keychain.utils.AndroidUtils
import com.sprinter.keychain.utils.BindLayout
import com.sprinter.keychain.views.KeysView
import kotlinx.android.synthetic.main.fmt_keys.*
import kotlinx.android.synthetic.main.fmt_keys.view.*


@BindLayout(R.layout.fmt_keys)
class KeysFragment : AbstractFragment<KeysPresenter>(), KeysView, TextWatcher,
        KeyPairRecyclerAdapter.KeyPairChangeListener, ItemClickListener {

    @InjectPresenter internal lateinit var presenter: KeysPresenter
    private lateinit var keysAdapter: KeyPairRecyclerAdapter

    @ProvidePresenter override fun providePresenter(): KeysPresenter {
        super.providePresenter()

        val keysRepository = appContext.getRepositories().getKeysRepository()
        val toolbarClickSubject = getToolBar()?.viewClickedSubject()
        val router = appContext.router()

        return KeysPresenter(keysRepository, toolbarClickSubject, router)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keysAdapter = KeyPairRecyclerAdapter(resources)

        view.keyList.layoutManager = LinearLayoutManager(context)
        view.keyList.adapter = keysAdapter

        keysAdapter.setClickListener(this)
        keysAdapter.setKeyPairItemTextChangeListener(this)

        val categoryId = arguments?.getLong(ARGS_CATEGORY_ID) ?: 0
        val categoryItemId = arguments?.getLong(ARGS_CATEGORY_ITEM_ID) ?: 0
        val isEditMode = arguments?.getBoolean(ARGS_IS_EDIT_MODE, false) ?: false

        presenter.setCategoryId(categoryId)
        presenter.isEditMode(isEditMode)
        presenter.setCategoryItemId(categoryItemId)

        liCategoryTitle.addTextChangedListener(this)
    }

    override fun onResume() {
        super.onResume()
        getToolBar()?.visibility = View.VISIBLE
        getToolBar()?.visibilityHomeButton(true)
        getToolBar()?.setStateHomeButton(ToolbarView.STATE_HOME_BUTTON_BACK)
    }

    override fun textChanged(position: Int, text: String, viewId: Int) {
        presenter.pairItemTextChanged(position, text, viewId)
    }

    override fun setCategoryTitle(title: String) {
        view?.liCategoryTitle?.setText(title)
    }

    override fun setKeyList(items: List<Pair<String, String>>) {
        keysAdapter.data = items
    }

    override fun setTitle(title: String) {
        getToolBar()?.setTitle(title)
    }

    override fun enabledTitle(enabled: Boolean) {
        view?.liCategoryTitle?.isEnabled = enabled
        view?.liCategoryTitle?.inputType = if (enabled) (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) else InputType.TYPE_NULL
    }

    override fun enabledKeysItemsList(enabled: Boolean) {
        keysAdapter.setIsEditMode(enabled)
    }

    override fun visibilityAddKeysButton(visible: Boolean) {
        getToolBar()?.findViewById<View>(
                R.id.wBtnAdd)?.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            getToolBar()?.setView(R.layout.w_btn_done)
        } else {
            getToolBar()?.removeView(R.layout.w_btn_done)
        }
    }

    override fun copyToClipboard(label: String, value: String) {
        AndroidUtils.setClipboard(context!!, label, value)
    }

    override fun showToast(messageId: Int) {
        Toast.makeText(context!!, messageId, Toast.LENGTH_LONG).show()
    }

    override fun onDialogMessageResult(result: TupleDialogResult) {
        presenter.onDialogMessageResult(result)
    }

    override fun visibilityLoading(visible: Boolean) {
        loading.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun visibilityTitle(visible: Boolean) {
        liCategoryTitle.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun visibilityKeys(visible: Boolean) {
        keyList.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun afterTextChanged(s: Editable) {
        presenter.setTextTitle(s.toString())
    }

    override fun onItemClick(position: Int, id: Int, tag: String?) {
        presenter.onItemClick(position, id, tag)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun onDestroyView() {
        getToolBar()?.removeView(R.layout.w_btn_done)
        getToolBar()?.findViewById<View>(R.id.wBtnAdd)?.visibility = View.VISIBLE
        super.onDestroyView()
    }

    companion object {

        private const val ARGS_CATEGORY_ID = "ARGS_CATEGORY_ID"
        private const val ARGS_CATEGORY_ITEM_ID = "ARGS_CATEGORY_ITEM_ID"
        private const val ARGS_IS_EDIT_MODE = "ARGS_IS_EDIT_MODE"

        val FRAGMENT_TAG: String
            get() = KeysFragment::class.java.simpleName

        fun newInstance(editMode: Boolean, categoryId: Long, categoryItemId: Long): KeysFragment {
            val args = Bundle()

            args.putLong(ARGS_CATEGORY_ID, categoryId)
            args.putLong(ARGS_CATEGORY_ITEM_ID, categoryItemId)

            args.putBoolean(ARGS_IS_EDIT_MODE, editMode)

            val fragment = KeysFragment()
            fragment.arguments = args

            return fragment
        }

    }

}