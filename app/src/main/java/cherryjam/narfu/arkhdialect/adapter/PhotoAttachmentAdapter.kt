package cherryjam.narfu.arkhdialect.adapter

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Size
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.PhotoAttachment
import cherryjam.narfu.arkhdialect.databinding.ItemPhotoAttachmentBinding
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper

class PhotoAttachmentAdapter :
    SelectableAdapter<PhotoAttachmentAdapter.PhotoAttachmentViewHolder>() {
    var data: List<PhotoAttachment> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    inner class PhotoAttachmentViewHolder(val binding: ItemPhotoAttachmentBinding)
        : ViewHolder(binding.root), SelectableItem {
        private val context = binding.root.context
        private lateinit var attachment: PhotoAttachment

        init {
            binding.imageView.setOnClickListener {
                if (isSelecting)
                    selectItem(this)
                else {
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
                                    title = context.getString(R.string.delete_photo_title),
                                    message = context.getString(R.string.delete_photo_message),
                                    positiveText = context.getString(R.string.delete),
                                    negativeText = context.getString(R.string.cancel),
                                    positiveCallback = {
                                        Thread {
                                            AppDatabase.getInstance(context).photoAttachmentDao().delete(attachment)
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
            binding.imageView.setOnLongClickListener {
                if (!isSelecting)
                    startSelection()
                selectItem(this)
                true
            }
        }

        fun onBind(attachment: PhotoAttachment) {
            this.attachment = attachment
            val contentResolver = context.contentResolver

            try {
                val image = contentResolver.loadThumbnail(
                    attachment.uri, Size(256, 256), null
                )
                binding.imageView.setImageBitmap(image)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (isItemSelected(bindingAdapterPosition)) onSelect() else onDeselect()
        }

        override fun onSelect() {
            binding.imageView.scaleX = 0.75f
            binding.imageView.scaleY = 0.75f
        }

        override fun onDeselect() {
            binding.imageView.scaleX = 1f
            binding.imageView.scaleY = 1f
        }

        fun openAttachment() {
            val intent = Intent().apply {
                setAction(Intent.ACTION_VIEW)
                setDataAndType(attachment.uri, context.contentResolver.getType(attachment.uri))
                setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try { context.startActivity(intent) }
            catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                Toast.makeText(context, context.getString(R.string.no_activity_found), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoAttachmentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPhotoAttachmentBinding.inflate(layoutInflater, parent, false)

        return PhotoAttachmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoAttachmentViewHolder, position: Int) {
        val photoAttachment = data[position]
        holder.onBind(photoAttachment)
    }
}