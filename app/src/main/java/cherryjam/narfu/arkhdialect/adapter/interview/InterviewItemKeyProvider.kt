package cherryjam.narfu.arkhdialect.adapter.interview

import androidx.recyclerview.selection.ItemKeyProvider

class InterviewItemKeyProvider(private val adapter: InterviewAdapter)
    : ItemKeyProvider<Long>(SCOPE_CACHED)
{
    override fun getKey(position: Int): Long? = adapter.currentList[position].id
    override fun getPosition(key: Long): Int = adapter.currentList.indexOfFirst { it.id == key }
}