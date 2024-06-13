package cherryjam.narfu.arkhdialect.service.attachment

import cherryjam.narfu.arkhdialect.data.PhotoAttachment
import cherryjam.narfu.arkhdialect.data.RecorderAttachment

interface IRecorderAttachmentService {
    fun getData(): MutableList<RecorderAttachment>
    fun updateAttachments()
}