package cherryjam.narfu.arkhdialect.adapter

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.RecordingAttachment
import cherryjam.narfu.arkhdialect.databinding.ItemRecordingAttachmentBinding
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper
import com.simplemobiletools.commons.extensions.formatDate
import com.simplemobiletools.commons.extensions.getFormattedDuration


class RecordingAttachmentAdapter(val context: Context)
    : SelectableAdapter<RecordingAttachmentAdapter.RecordingAttachmentViewHolder>() {
    var data: List<RecordingAttachment> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    inner class RecordingAttachmentViewHolder(val binding: ItemRecordingAttachmentBinding)
        : RecyclerView.ViewHolder(binding.root), SelectableItem
    {
        private val context = binding.root.context
        private lateinit var attachment: RecordingAttachment

        init {
            binding.listItem.setOnClickListener {
                if (isSelecting)
                    selectItem(this)
                else
                    openAttachment()
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
                            openAttachment()
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
                                title = context.getString(R.string.delete_recording_title),
                                message = context.getString(R.string.delete_recording_message),
                                positiveText = context.getString(R.string.delete),
                                negativeText = context.getString(R.string.cancel),
                                positiveCallback = {
                                    Thread {
                                        AppDatabase.getInstance(context).recordingAttachmentDao().delete(attachment)
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

        fun onBind(attachment: RecordingAttachment) {
            this.attachment = attachment

            with (binding) {
                recordingTitle.text = attachment.name
                recordingDate.text = attachment.timestamp.formatDate(root.context)

                val toSec = attachment.duration / 1000
                recordingDuration.text = toSec.getFormattedDuration()
            }

            if (isItemSelected(bindingAdapterPosition)) onSelect() else onDeselect()
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

        fun openAttachment() {
            val intent = Intent().apply {
                setAction(Intent.ACTION_VIEW)
                setDataAndType(attachment.uri, context.contentResolver.getType(attachment.uri))
            }
            try { context.startActivity(intent) }
            catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                Toast.makeText(context, context.getString(R.string.no_activity_found), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingAttachmentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRecordingAttachmentBinding.inflate(layoutInflater, parent, false)

        return RecordingAttachmentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecordingAttachmentViewHolder, position: Int) {
        val recordingAttachment = data[position]
        holder.onBind(recordingAttachment)
    }
}