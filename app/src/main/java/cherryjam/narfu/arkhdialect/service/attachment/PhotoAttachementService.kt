package cherryjam.narfu.arkhdialect.service.attachment

import cherryjam.narfu.arkhdialect.data.PhotoAttachment

interface PhotoAttachementService {
    fun getData(): MutableList<PhotoAttachment>
    fun updateAttachments()
}