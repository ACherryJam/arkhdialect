package cherryjam.narfu.arkhdialect.adapter.interview

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class InterviewItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        view ?: return null

        val viewHolder = recyclerView.getChildViewHolder(view) as InterviewAdapter.InterviewViewHolder
        return viewHolder.getItemDetails()
    }
}