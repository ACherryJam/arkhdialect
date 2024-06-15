package cherryjam.narfu.arkhdialect.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class TextAttachment(
    val id: Long,
    val interview: Interview? = null,
    val title: String = "",
    val data: String = ""
) : Parcelable