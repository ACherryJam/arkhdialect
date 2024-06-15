package cherryjam.narfu.arkhdialect.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cherryjam.narfu.arkhdialect.data.RecordingAttachment
import cherryjam.narfu.arkhdialect.databinding.ItemRecordingAttachmentBinding
import com.simplemobiletools.commons.extensions.formatDate
import com.simplemobiletools.commons.extensions.getFormattedDuration


class RecordingAttachmentAdapter(val context: Context) :  RecyclerView.Adapter<RecordingAttachmentAdapter.RecordingAttachmentViewHolder>(){
    var data: List<RecordingAttachment> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class RecordingAttachmentViewHolder(val binding: ItemRecordingAttachmentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingAttachmentAdapter.RecordingAttachmentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRecordingAttachmentBinding.inflate(layoutInflater, parent, false)

        return RecordingAttachmentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecordingAttachmentAdapter.RecordingAttachmentViewHolder, position: Int) {
        val recordingAttachment = data[position]

        with (holder.binding) {
            with (itemHolder) {
                recordingTitle.text = recordingAttachment.name
                recordingDate.text = recordingAttachment.timestamp.formatDate(root.context)
                val toSec = recordingAttachment.duration / 1000

                recordingDuration.text = toSec.getFormattedDuration()
            }
        }

    }

}