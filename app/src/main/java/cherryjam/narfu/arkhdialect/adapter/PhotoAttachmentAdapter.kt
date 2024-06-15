package cherryjam.narfu.arkhdialect.adapter

import android.content.Context
import android.util.Size
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.PhotoAttachment
import cherryjam.narfu.arkhdialect.databinding.ItemPhotoAttachmentBinding

class PhotoAttachmentAdapter :
    RecyclerView.Adapter<PhotoAttachmentAdapter.PhotoAttachmentViewHolder>() {

    var data: List<PhotoAttachment> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    inner class PhotoAttachmentViewHolder(val binding: ItemPhotoAttachmentBinding) : ViewHolder(binding.root) {
        private val context = binding.root.context
        private lateinit var attachment: PhotoAttachment

        init {
            binding.imageView.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                popup.inflate(R.menu.options_menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.select -> {
//                            if (!isSelecting)
//                                startSelection()
//                            selectItem(this)
                            true
                        }
                        R.id.delete -> {
                            Thread {
                                AppDatabase.getInstance().photoAttachmentDao().delete(attachment)
                            }.start()
                            true//
                        }
                        else -> false
                    }
                }
                popup.show()
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