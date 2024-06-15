package cherryjam.narfu.arkhdialect.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.databinding.ItemInterviewBinding
import cherryjam.narfu.arkhdialect.ui.InterviewEditActivity

class InterviewAdapter : SelectableAdapter<InterviewAdapter.InterviewViewHolder>() {
    var data: List<Interview> = listOf()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    inner class InterviewViewHolder(val binding: ItemInterviewBinding) : ViewHolder(binding.root) {
        private val context = binding.root.context
        private lateinit var interview: Interview

        init {
            binding.listItem.setOnClickListener {
                if (isSelecting)
                    selectItem(this)
                else {
                    val intent = Intent(context, InterviewEditActivity::class.java)
                    intent.putExtra("interview", interview)
                    context.startActivity(intent)
                }
            }
            binding.listItem.setOnLongClickListener {
                if (!isSelecting)
                    startSelection()
                selectItem(this)

                false
            }
            binding.listItemOptions.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                popup.inflate(R.menu.options_menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.select -> {
                            if (!isSelecting)
                                startSelection()
                            selectItem(this)
                            true
                        }
                        R.id.delete -> {
                            Thread {
                                AppDatabase.getInstance().interviewDao().delete(interview)
                            }.start()
                            true//
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }

        fun onBind(interview: Interview, position: Int) {
            this.interview = interview

            with(binding) {
                listItem.headline.text = interview.name
                listItem.supportText.text = interview.location

                val color = if (isItemSelected(position))
                    R.color.selected_item
                else
                    R.color.white
                listItem.setBackgroundResource(color)
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
        holder.onBind(interview, position)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}