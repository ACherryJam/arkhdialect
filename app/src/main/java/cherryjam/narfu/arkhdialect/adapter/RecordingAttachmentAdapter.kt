package cherryjam.narfu.arkhdialect.adapter

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.provider.MediaStore.Audio.Media
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.RecordingAttachment
import cherryjam.narfu.arkhdialect.databinding.ItemRecordingAttachmentBinding
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
                            Thread {
                                AppDatabase.getInstance(context).recordingAttachmentDao().delete(attachment)
                            }.start()
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

            context.contentResolver.query(
                attachment.uri,
                arrayOf(Media.DISPLAY_NAME,
                        Media.DATE_ADDED,
                        Media.DURATION),
                null, null, null
            )?.use { cursor ->
                val nameColumn = cursor.getColumnIndexOrThrow(Media.DISPLAY_NAME)
                val timestampColumn = cursor.getColumnIndexOrThrow(Media.DATE_ADDED)
                val durationColumn = cursor.getColumnIndexOrThrow(Media.DURATION)

                if (cursor.moveToFirst()) {
                    val name = cursor.getString(nameColumn)
                    val timestamp = cursor.getInt(timestampColumn)
                    val duration = cursor.getInt(durationColumn)

                    with (binding) {
                        recordingTitle.text = name
                        recordingDate.text = timestamp.formatDate(root.context)

                        val toSec = duration / 1000
                        recordingDuration.text = toSec.getFormattedDuration()
                    }
                }
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
            context.startActivity(intent)
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