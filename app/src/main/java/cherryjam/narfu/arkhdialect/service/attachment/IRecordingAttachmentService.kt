package cherryjam.narfu.arkhdialect.service.attachment

import cherryjam.narfu.arkhdialect.data.RecordingAttachment

interface IRecordingAttachmentService {
    fun getData(): MutableList<RecordingAttachment>
    fun updateAttachments()
}