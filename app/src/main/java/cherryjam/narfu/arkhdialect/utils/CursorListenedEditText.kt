package cherryjam.narfu.arkhdialect.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.R

class CursorListenedEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : androidx.appcompat.widget.AppCompatEditText(context, attrs, defStyleAttr) {
    interface CursorListener {
        fun onCursorChange(cursorPosition: Int)
    }

    private val cursorListeners: MutableList<CursorListener> = mutableListOf()

    private var cursorPosition: Int = 0

    fun addCursorListener(listener: CursorListener) {
        cursorListeners.add(listener)
    }

    fun removeCursorListener(listener: CursorListener) {
        cursorListeners.remove(listener)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        cursorPosition = selStart

        if (cursorListeners == null)
            return

        for (listener in cursorListeners) {
            listener.onCursorChange(cursorPosition)
        }
    }
}