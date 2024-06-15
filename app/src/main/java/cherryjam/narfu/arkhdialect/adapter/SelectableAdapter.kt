package cherryjam.narfu.arkhdialect.adapter

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.util.Collections
import java.util.TreeSet

abstract class SelectableAdapter<T : ViewHolder> : RecyclerView.Adapter<T>() {
    interface Listener {
        fun onSelectionStart()
        fun onSelectionEnd()
        fun onItemSelect(position: Int)
        fun onItemDeselect(position: Int)
    }

    private val listeners: MutableList<Listener> = mutableListOf()
    private var selectedItemPositions: TreeSet<Int> = sortedSetOf(Collections.reverseOrder())

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
            removeItemFromSelection(position)
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

    fun selectItem(position: Int) {
        if (!isSelecting) {
            Log.e(javaClass.simpleName, "Trying to select item while not selecting")
            return
        }

        if (position >= itemCount || position < 0) {
            Log.e(javaClass.simpleName, "Trying to select item out of bounds ($position)")
            return
        }

        if (isItemSelected(position))
            removeItemFromSelection(position)
        else
            addItemToSelection(position)
    }

    fun selectItem(viewHolder: T) {
        if (!isSelecting) {
            Log.e(javaClass.simpleName, "Trying to select item while not selecting")
            return
        }

        val position = viewHolder.adapterPosition
        if (position == RecyclerView.NO_POSITION) {
            Log.e(javaClass.simpleName, "Couldn't find position of ViewHolder $viewHolder")
            return
        }

        selectItem(position)
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
}