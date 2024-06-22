package cherryjam.narfu.arkhdialect.adapter

import android.content.Intent
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.databinding.ItemInterviewBinding
import cherryjam.narfu.arkhdialect.ui.InterviewEditActivity
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper


class InterviewAdapter : SelectableAdapter<InterviewAdapter.InterviewViewHolder>() {
    var data: List<Interview> = listOf()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    inner class InterviewViewHolder(val binding: ItemInterviewBinding)
        : ViewHolder(binding.root), SelectableItem {
        private val context = binding.root.context
        private lateinit var interview: Interview

        init {
            binding.listItem.setOnClickListener {
                if (isSelecting)
                    selectItem(this)
                else
                    openEditor()
            }
            binding.listItem.setOnLongClickListener {
                if (!isSelecting)
                    startSelection()
                selectItem(this)
                true
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
                            if (!isSelecting)
                                startSelection()
                            selectItem(this)
                            true
                        }
                        R.id.delete -> {
                            AlertDialogHelper.showAlertDialog(
                                context,
                                title = context.getString(R.string.delete_interview_title),
                                message = context.getString(R.string.delete_interview_message),
                                positiveText = context.getString(R.string.delete),
                                negativeText = context.getString(R.string.cancel),
                                positiveCallback = {
                                    Thread {
                                        AppDatabase.getInstance(context).interviewDao().delete(interview)
                                    }.start()
                                },
                            )
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

            if (isItemSelected(position)) onSelect() else onDeselect()
        }

        override fun onSelect() {
            val nightModeFlags: Int = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val color = when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> R.color.item_background_selected_night
                else -> R.color.item_background_selected_day
            }
            binding.listItem.setBackgroundResource(color)
        }

        override fun onDeselect() {
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
        val interview = data[position]
        holder.onBind(interview, position)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}