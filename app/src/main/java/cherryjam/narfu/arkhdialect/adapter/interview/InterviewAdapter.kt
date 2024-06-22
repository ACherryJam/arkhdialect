package cherryjam.narfu.arkhdialect.adapter.interview

import android.content.Intent
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.selection.ItemDetailsLookup.ItemDetails
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.databinding.ItemInterviewBinding
import cherryjam.narfu.arkhdialect.ui.InterviewEditActivity


class InterviewAdapter : ListAdapter<Interview, InterviewAdapter.InterviewViewHolder>(
    DIFF_CALLBACK
) {
    var tracker: SelectionTracker<Long>? = null

    class InterviewViewHolder(val binding: ItemInterviewBinding)
        : ViewHolder(binding.root) {
        private val context = binding.root.context
        private lateinit var interview: Interview

        init {
            binding.listItem.setOnClickListener {
                openEditor()
            }
            binding.listItemOptions.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                popup.inflate(R.menu.options_menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.open -> {
                            openEditor()
                            true
                        }
                        R.id.select -> {

                            true
                        }
                        R.id.delete -> {
                            Thread {
                                AppDatabase.getInstance(context).interviewDao().delete(interview)
                            }.start()
                            true//
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }

        fun onBind(interview: Interview, isSelected: Boolean) {
            this.interview = interview

            with(binding) {
                with(interview) {
                    listItem.headline.text = if (name.isEmpty())
                        context.getString(R.string.empty_interview_name)
                    else
                        name

                    listItem.supportText.text = if (location.isEmpty())
                        context.getString(R.string.empty_interview_region)
                    else
                        location
                }
            }

            if (isSelected) onSelect() else onDeselect()
        }

        fun getItemDetails(): ItemDetails<Long> = object : ItemDetails<Long>() {
            override fun getPosition(): Int = bindingAdapterPosition
            override fun getSelectionKey(): Long? = interview.id
        }

        fun onSelect() {
            val nightModeFlags: Int = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val color = when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> R.color.item_background_selected_night
                else -> R.color.item_background_selected_day
            }
            binding.listItem.setBackgroundResource(color)
        }

        fun onDeselect() {
            val nightModeFlags: Int = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val color = when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> R.color.item_background_night
                else -> R.color.item_background_day
            }
            binding.listItem.setBackgroundResource(color)
        }

        fun openEditor() {
            val intent = Intent(context, InterviewEditActivity::class.java)
            intent.putExtra("interview", interview)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemInterviewBinding.inflate(inflater, parent, false)

        return InterviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InterviewViewHolder, position: Int) {
        val interview = getItem(position)

        val isSelected = tracker?.isSelected(interview.id) ?: false
        holder.onBind(interview, isSelected)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Interview>() {
            override fun areItemsTheSame(oldItem: Interview, newItem: Interview): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Interview, newItem: Interview): Boolean {
                return oldItem == newItem
            }
        }
    }
}