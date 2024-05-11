package cherryjam.narfu.arkhdialect.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cherryjam.narfu.arkhdialect.data.TextAttachment
import cherryjam.narfu.arkhdialect.databinding.ItemTextAttachmentBinding
import cherryjam.narfu.arkhdialect.ui.TextAttachmentActivity
import cherryjam.narfu.arkhdialect.ui.TextAttachmentEditActivity
import kotlin.coroutines.coroutineContext

class TextAttachmentAdapter(val context: Context) :
    RecyclerView.Adapter<TextAttachmentAdapter.TextAttachmentViewHolder>()
{
    var data: List<TextAttachment> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class TextAttachmentViewHolder(val binding: ItemTextAttachmentBinding)
        : RecyclerView.ViewHolder(binding.root)

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

        with (holder.binding) {
            with (listItem) {
                headline.text = textAttachment.title
                supportText.text = textAttachment.data
            }

            listItemOptions.setOnClickListener {

            }

            listItem.setOnClickListener {
                val intent = Intent(context, TextAttachmentEditActivity::class.java)
                intent.putExtra("attachment", textAttachment)

                context.startActivity(intent)
            }
        }


    }
}