package cherryjam.narfu.arkhdialect.data

import android.net.Uri
import android.os.Parcelable
import cherryjam.narfu.arkhdialect.data.entity.Interview
import kotlinx.parcelize.Parcelize

@Parcelize
class RecordingAttachment(
    val id: Long,
    val name: String,
    val uri: Uri,
    val timestamp: Int,
    val duration: Int,
    val interview: Interview? = null,
) : Parcelable

//https://github.com/SimpleMobileTools/Simple-Voice-Recorder