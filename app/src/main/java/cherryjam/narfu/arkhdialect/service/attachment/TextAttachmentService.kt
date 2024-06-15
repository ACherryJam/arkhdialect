package cherryjam.narfu.arkhdialect.service.attachment

import cherryjam.narfu.arkhdialect.data.TextAttachment

interface TextAttachmentService {
    fun getData(): MutableList<TextAttachment>
}