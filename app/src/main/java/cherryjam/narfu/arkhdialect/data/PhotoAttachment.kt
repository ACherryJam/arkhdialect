package cherryjam.narfu.arkhdialect.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class PhotoAttachment(
    val id: Long,
    val uri: Uri,
    val interview: Interview? = null,
) : Parcelable {
}