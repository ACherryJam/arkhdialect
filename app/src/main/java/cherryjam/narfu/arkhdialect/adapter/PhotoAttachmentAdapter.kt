package cherryjam.narfu.arkhdialect.adapter

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.util.Size
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cherryjam.narfu.arkhdialect.data.PhotoAttachment
import cherryjam.narfu.arkhdialect.databinding.ItemPhotoAttachmentBinding
import java.lang.Exception

class PhotoAttachmentAdapter(val context: Context) :
    RecyclerView.Adapter<PhotoAttachmentAdapter.PhotoAttachmentViewHolder>() {

    var data: List<PhotoAttachment> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class PhotoAttachmentViewHolder(val binding: ItemPhotoAttachmentBinding)
            : ViewHolder(binding.root)

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
        val contentResolver = context.contentResolver

        try {
            val image = contentResolver.loadThumbnail(
                photoAttachment.uri, Size(256, 256), null
            )

            with (holder.binding) {
                imageView.setImageBitmap(image)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}