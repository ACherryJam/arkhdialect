package cherryjam.narfu.arkhdialect.adapter

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.Interview
import cherryjam.narfu.arkhdialect.databinding.ItemInterviewBinding

class InterviewAdapter() :
    Adapter<InterviewAdapter.InterviewViewHolder>() {
    var data: List<Interview> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    var selected_data: MutableList<Interview> = arrayListOf()


    class InterviewViewHolder(val binding: ItemInterviewBinding) : ViewHolder(binding.root) {
        init {
            binding.listItemOptions.setOnClickListener {
                //creating a popup menu
                val popup: PopupMenu = PopupMenu(it.context, binding.listItemOptions)

                //inflating menu from xml resource
                popup.inflate(R.menu.options_menu)

                //adding click listener
                popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        return when (item.itemId) {
                            R.id.select ->
                                true

                            R.id.delete ->
                                true

                            else -> false
                        }
                    }
                })

                //displaying the popup
                popup.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemInterviewBinding.inflate(inflater, parent, false)

        return InterviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InterviewViewHolder, position: Int) {
        val interview = data[position]

        with (holder.binding.listItem) {
            tag = interview

            headline.text = interview.name
            supportText.text = interview.location
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    //https://habr.com/ru/articles/705064/
}