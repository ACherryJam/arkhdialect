package cherryjam.narfu.arkhdialect.adapter

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.util.Collections
import java.util.TreeSet

abstract class SelectableAdapter<T>
    : RecyclerView.Adapter<T>() where T : ViewHolder, T: SelectableAdapter.SelectableItem {
    interface SelectableItem {
        fun onSelect()
        fun onDeselect()
    }

    interface Listener {
        fun onSelectionStart()
        fun onSelectionEnd()
        fun onItemSelect(position: Int)
        fun onItemDeselect(position: Int)
    }

    private val listeners: MutableList<Listener> = mutableListOf()
    private var selectedItemPositions: TreeSet<Int> = sortedSetOf(Collections.reverseOrder())

    private var recyclerView: RecyclerView? = null

    var isSelecting: Boolean = false
        private set

    fun startSelection() {
        if (isSelecting) {
            Log.i(javaClass.simpleName, "Trying to start selection while already selecting")
            return
        }

        isSelecting = true
        for (listener in listeners)
            listener.onSelectionStart()
    }

    fun endSelection() {
        if (!isSelecting) {
            Log.i(javaClass.simpleName, "Trying to end selection while not selecting")
            return
        }

        isSelecting = false
        for (listener in listeners)
            listener.onSelectionEnd()
        selectedItemPositions.clear()
    }

    fun clearSelection() {
        if (!isSelecting) {
            Log.i(javaClass.simpleName, "Trying to clear selection while not selecting")
            return
        }

        for (position in selectedItemPositions)
            selectItem(position)
    }

    private fun addItemToSelection(position: Int) {
        selectedItemPositions.add(position)
        for (listener in listeners)
            listener.onItemSelect(position)
    }

    private fun removeItemFromSelection(position: Int) {
        selectedItemPositions.remove(position)
        for (listener in listeners)
            listener.onItemDeselect(position)
    }

    @Suppress("UNCHECKED_CAST")
    fun selectItem(position: Int) {
        if (recyclerView == null) {
            Log.e(javaClass.simpleName, "Trying to select while not attached to recycler view")
            return
        }

        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
        if (viewHolder == null) {
            Log.e(javaClass.simpleName, "ViewHolder not found")
            return
        }

        if (viewHolder !is SelectableItem) {
            Log.e(javaClass.simpleName, "ViewHolder does not implement SelectableItem")
            return
        }

        selectItem(position, viewHolder as T)
    }

    fun selectItem(viewHolder: T) {
        val position = viewHolder.bindingAdapterPosition
        if (position == RecyclerView.NO_POSITION) {
            Log.e(javaClass.simpleName, "Couldn't find position of ViewHolder $viewHolder")
            return
        }

        selectItem(position, viewHolder)
    }

    private fun selectItem(position: Int, viewHolder: T) {
        if (!isSelecting) {
            Log.e(javaClass.simpleName, "Trying to select item while not selecting")
            return
        }

        if (position >= itemCount || position < 0) {
            Log.e(javaClass.simpleName, "Trying to select item out of bounds ($position)")
            return
        }

        if (isItemSelected(position)) {
            removeItemFromSelection(position)
            viewHolder.onDeselect()
        }
        else {
            addItemToSelection(position)
            viewHolder.onSelect()
        }
    }

    fun addListener(listener: Listener) = listeners.add(listener)
    fun removeListener(listener: Listener) = listeners.remove(listener)

    fun isItemSelected(position: Int): Boolean {
        return selectedItemPositions.contains(position)
    }

    fun getSelectedItemPositions(): TreeSet<Int> {
        return selectedItemPositions
    }

    fun getSelectedItemCount(): Int {
        return selectedItemPositions.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
        super.onDetachedFromRecyclerView(recyclerView)
    }
}