package cherryjam.narfu.arkhdialect.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.adapter.SelectableAdapter
import cherryjam.narfu.arkhdialect.ui.MainActivity

class SelectableHelper<T>(
    private val fragment: Fragment? = null,
    private val activity: AppCompatActivity? = null,
    private val adapter: SelectableAdapter<T>,
    private val context: Context,
    val deleteSelectedItems: () -> Unit={},
    val checkShowItem: () -> Unit={}
) where T : ViewHolder, T : SelectableAdapter.SelectableItem {

    private var actionMode: ActionMode? = null
    private var type: String
    var flag: Boolean = true

    init {
        if (fragment != null){
            type = fragment::class.java.simpleName
            type = type.replace("Fragment", "").lowercase()
        }
        else {
            type = activity!!::class.java.simpleName
            type = type.replace("AttachmentActivity", "").lowercase()
        }
    }


    @SuppressLint("DiscouragedApi")
    fun getStringResourceId(resourceName: String): Int {
        val resources = context.resources
        return resources.getIdentifier(resourceName, "string", context.packageName)

    }


    fun getSelectableAdapterCallback(): SelectableAdapter.Listener {
        val selectableAdapterCallback = object : SelectableAdapter.Listener {
            override fun onSelectionStart() {
                actionMode = if (fragment != null)
                    (fragment.activity as MainActivity).startSupportActionMode(actionModeCallback)
                else
                    activity!!.startSupportActionMode(actionModeCallback)
            }

            override fun onSelectionEnd() {
                actionMode?.finish()
            }

            override fun onItemSelect(position: Int) {
                actionMode?.title =
                    context.getString(R.string.selected_items, adapter.getSelectedItemCount())
            }

            override fun onItemDeselect(position: Int) {
                actionMode?.title =
                    context.getString(R.string.selected_items, adapter.getSelectedItemCount())
            }
        }
        return selectableAdapterCallback
    }


    val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_multi_select, menu)

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu?): Boolean {
            flag = false
            checkShowItem()

            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_delete -> {
                    AlertDialogHelper.showAlertDialog(
                        context,
                        title = context.getString(getStringResourceId("delete_${type}_title")),
                        message = context.getString(getStringResourceId("delete_${type}_message"), adapter.getSelectedItemCount()),
                        positiveText = context.getString(R.string.delete),
                        positiveCallback = deleteSelectedItems,
                        negativeText = context.getString(R.string.cancel),
                    )
                    return true
                }

                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null

            flag = true
            checkShowItem()


            // Janky way to handle OnBackPressed in ActionMode
            // OnBackPressedCallback doesn't work
            if (adapter.isSelecting) {
                adapter.clearSelection()
                adapter.endSelection()
            }
        }

    }

}