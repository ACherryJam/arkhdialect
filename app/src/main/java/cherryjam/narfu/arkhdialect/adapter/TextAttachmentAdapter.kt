package cherryjam.narfu.arkhdialect.adapter

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.TextAttachment
import cherryjam.narfu.arkhdialect.databinding.ItemTextAttachmentBinding
import cherryjam.narfu.arkhdialect.ui.TextAttachmentEditActivity

class TextAttachmentAdapter(val context: Context) :
    SelectableAdapter<TextAttachmentAdapter.TextAttachmentViewHolder>()
{
    var data: List<TextAttachment> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    inner class TextAttachmentViewHolder(val binding: ItemTextAttachmentBinding)
        : RecyclerView.ViewHolder(binding.root), SelectableItem {
        private val context = binding.root.context
        private lateinit var textAttachment: TextAttachment

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
                                AppDatabase.getInstance().textAttachmentDao().delete(textAttachment)
                            }.start()
                            true//
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }

        fun onBind(textAttachment: TextAttachment) {
            this.textAttachment = textAttachment

            with(binding.listItem) {
                headline.text = textAttachment.title
                supportText.text = textAttachment.content

                if (isItemSelected(bindingAdapterPosition)) onSelect() else onDeselect()
            }
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
            val intent = Intent(context, TextAttachmentEditActivity::class.java)
            intent.putExtra("attachment", textAttachment)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextAttachmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTextAttachmentBinding.inflate(inflater, parent, false)

        return TextAttachmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TextAttachmentViewHolder, position: Int) {
        val textAttachment = data[position]
        holder.onBind(textAttachment)
    }
}