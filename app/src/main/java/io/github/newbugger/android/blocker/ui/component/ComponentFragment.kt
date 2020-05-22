package io.github.newbugger.android.blocker.ui.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elvishew.xlog.XLog
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.blocker.baseview.ContextMenuRecyclerView
import io.github.newbugger.android.blocker.core.root.EControllerMethod
import io.github.newbugger.android.blocker.ui.Constants
import io.github.newbugger.android.blocker.util.PreferenceUtil
import io.github.newbugger.android.blocker.util.ToastUtil
import io.github.newbugger.android.libkit.utils.ApplicationUtil
import kotlinx.android.synthetic.main.component_item.view.*
import kotlinx.android.synthetic.main.fragment_component.*
import kotlinx.android.synthetic.main.fragment_component.view.*


class ComponentFragment : Fragment(), ComponentContract.View, ComponentContract.ComponentItemListener {

    override lateinit var presenter: ComponentContract.Presenter
    private lateinit var componentAdapter: ComponentsRecyclerViewAdapter
    private lateinit var packageName: String
    private lateinit var type: EComponentType
    private val logger = XLog.tag("io.github.newbugger.android.blocker.ui.component.ComponentFragment").build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getSerializable(Constants.CATEGORY) as EComponentType
        packageName = arguments?.getString(Constants.PACKAGE_NAME) ?: ""
        presenter = ComponentPresenter(requireContext(), this, packageName)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_component, container, false)
        with(root) {
            componentListSwipeLayout.apply {
                setColorSchemeColors(
                        ContextCompat.getColor(context, R.color.colorPrimary),
                        ContextCompat.getColor(context, R.color.colorAccent),
                        ContextCompat.getColor(context, R.color.colorPrimaryDark)
                )
                setOnRefreshListener {
                    presenter.loadComponents(packageName, type)
                }
            }

            componentListFragmentRecyclerView.apply {
                val layoutManager = LinearLayoutManager(context)
                this.layoutManager = layoutManager
                componentAdapter = ComponentsRecyclerViewAdapter()
                componentAdapter.setOnClickListener(this@ComponentFragment)
                this.adapter = componentAdapter
                this.itemAnimator = DefaultItemAnimator()
                addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
                registerForContextMenu(this)
            }
        }
        setHasOptionsMenu(true)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.loadComponents(packageName, type)
    }

    override fun onDestroy() {
        presenter.destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                searchForComponent(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                searchForComponent(query)
                return true
            }
        })
        searchView.setOnSearchClickListener {
            setItemsVisibility(menu, searchItem, false)
        }
        searchView.setOnCloseListener {
            setItemsVisibility(menu, searchItem, true)
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_filter -> showFilteringPopUpMenu()
            R.id.menu_refresh -> presenter.loadComponents(packageName, type)
            R.id.menu_block_all -> showDisableAllAlert()
            R.id.menu_enable_all -> {
                Toast.makeText(context, R.string.enabling_hint, Toast.LENGTH_SHORT).show()
                presenter.enableAllComponents(packageName, type)
            }
            R.id.menu_export_rule -> presenter.exportRule(packageName)
            R.id.menu_import_rule -> {
                presenter.importRule(packageName)
            }
        }
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.component_list_long_click_menu, menu)
        context?.let {
            if (PreferenceUtil.getControllerType(it) == EControllerMethod.IFW) {
                menu.removeItem(R.id.block_by_ifw)
                menu.removeItem(R.id.enable_by_ifw)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (!isVisible) {
            return false
        }
        val position = (item.menuInfo as ContextMenuRecyclerView.RecyclerContextMenuInfo).position
        val component = componentAdapter.getDataAt(position)
        when (item.itemId) {
            R.id.block_by_ifw -> presenter.addToIFW(component.packageName, component.name, type)
            R.id.enable_by_ifw -> presenter.removeFromIFW(component.packageName, component.name, type)
            R.id.copy_component_name -> copyToClipboard(component.simpleName)
            R.id.copy_full_name -> copyToClipboard(component.name)
        }
        return true
    }

    override fun setLoadingIndicator(active: Boolean) {
        componentListSwipeLayout?.run {
            post { isRefreshing = active }
        }
    }

    override fun showNoComponent() {
        componentListFragmentRecyclerView?.visibility = View.GONE
        noComponentContainer?.visibility = View.VISIBLE
    }

    override fun searchForComponent(name: String) {
        componentAdapter.filter(name)
    }

    override fun showFilteringPopUpMenu() {
        PopupMenu(activity, activity?.findViewById(R.id.menu_filter)).apply {
            menuInflater.inflate(R.menu.filter_component, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.name_asc -> presenter.currentComparator = EComponentComparatorType.SIMPLE_NAME_ASCENDING
                    R.id.name_des -> presenter.currentComparator = EComponentComparatorType.SIMPLE_NAME_DESCENDING
                    R.id.package_name_asc -> presenter.currentComparator = EComponentComparatorType.NAME_ASCENDING
                    R.id.package_name_des -> presenter.currentComparator = EComponentComparatorType.NAME_DESCENDING
                }
                presenter.loadComponents(packageName, type)
                true
            }
            show()
        }
    }

    override fun refreshComponentState(componentName: String) {
        val viewModel = presenter.getComponentViewModel(packageName, componentName)
        componentAdapter.updateViewModel(viewModel)
    }

    override fun showAlertDialog(message: String?) {
        context?.apply {
            AlertDialog.Builder(this)
                    .setTitle(resources.getString(R.string.oops))
                    .setMessage(getString(R.string.no_root_error_message, message))
                    .setPositiveButton(R.string.close) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                    .show()
        }
    }

    override fun showComponentList(components: MutableList<ComponentItemViewModel>) {
        noComponentContainer?.visibility = View.GONE
        componentListFragmentRecyclerView?.visibility = View.VISIBLE
        componentAdapter.updateData(components)
    }

    override fun onComponentClick(name: String) {
    }

    override fun onComponentLongClick(name: String) {

    }

    // here to click button and switch component
    // the behavior is only defined by button is on or off
    // only use this when not on shizuku mode
    override fun onSwitchClick(name: String, isChecked: Boolean) {
        if (isChecked && !PreferenceUtil.checkShizukuType(requireContext()))
            presenter.enable(packageName, name)
        else
            presenter.disable(packageName, name)
    }

    override fun showDisableAllAlert() {
        context?.let {
            AlertDialog.Builder(it)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.warning_disable_all_component)
                    .setCancelable(true)
                    .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(R.string.ok) { _, _ ->
                        Toast.makeText(it, R.string.disabling_hint, Toast.LENGTH_SHORT).show()
                        presenter.disableAllComponents(packageName, type)
                    }
                    .create()
                    .show()
        }
    }

    override fun showActionDone() {
        Toast.makeText(context, R.string.done, Toast.LENGTH_SHORT).show()
    }

    override fun showActionFail() {
        Toast.makeText(context, R.string.fail, Toast.LENGTH_SHORT).show()
    }

    override fun showImportFail() {
        Toast.makeText(context, R.string.import_fail_message, Toast.LENGTH_SHORT).show()
    }

    override fun showToastMessage(message: String?, length: Int) {
        ToastUtil.showToast(message ?: "null", length)
    }

    private fun setItemsVisibility(menu: Menu, exception: MenuItem, visible: Boolean) {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item !== exception)
                item.isVisible = visible
        }
    }

    private fun copyToClipboard(content: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                ?: return
        val clip = ClipData.newPlainText(getString(R.string.component_name), content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), R.string.copied, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(packageName: String, type: EComponentType): Fragment {
            val fragment = ComponentFragment()
            val bundle = Bundle()
            bundle.putSerializable(Constants.CATEGORY, type)
            bundle.putString(Constants.PACKAGE_NAME, packageName)
            fragment.arguments = bundle
            return fragment
        }
    }

    inner class ComponentsRecyclerViewAdapter(
            private var components: MutableList<ComponentItemViewModel> = mutableListOf())
        : RecyclerView.Adapter<ComponentsRecyclerViewAdapter.ViewHolder>() {

        private lateinit var pm: PackageManager
        private var listCopy = ArrayList<ComponentItemViewModel>()
        private lateinit var listener: ComponentContract.ComponentItemListener

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.component_item, parent, false)
            pm = parent.context.packageManager
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindComponent(this.components[position])
            holder.itemView.isLongClickable = true
        }

        override fun getItemCount(): Int {
            return this.components.size
        }

        fun updateData(components: MutableList<ComponentItemViewModel>) {
            this.components = components
            this.listCopy = ArrayList(components)
            notifyDataSetChanged()
        }

        fun getDataAt(index: Int): ComponentItemViewModel {
            return components[index]
        }

        fun updateViewModel(viewModel: ComponentItemViewModel) {
            components.forEachIndexed { i, model ->
                if (model.name == viewModel.name) {
                    components[i] = viewModel
                    notifyItemChanged(i)
                }
            }
            listCopy.forEachIndexed { i, model ->
                if (model.name == viewModel.name) {
                    listCopy[i] = viewModel
                }
            }
        }

        fun filter(keyword: String) {
            components = if (keyword.isEmpty()) {
                listCopy
            } else {
                listCopy.asSequence().filter { it.name.contains(keyword, true) }.toMutableList()
            }
            notifyDataSetChanged()
        }

        fun setOnClickListener(listener: ComponentContract.ComponentItemListener) {
            this.listener = listener
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            fun bindComponent(component: ComponentItemViewModel) {
                with(itemView) {
                    component_name.text = component.simpleName
                    component_package_name.text = component.name
                    component_switch.isChecked = component.state && component.ifwState
                    setOnClickListener {
                        listener.onSwitchClick(component.name, !it.component_switch.isChecked)
                        it.component_switch.isChecked = !it.component_switch.isChecked
                    }
                    component_switch.setOnClickListener {
                        listener.onSwitchClick(component.name, it.component_switch.isChecked)
                    }
                    if (component.isRunning) {
                        itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.md_light_blue_50))
                    } else {
                        itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.md_white_1000))
                    }
                }
            }
        }
    }

}
