package cherryjam.narfu.arkhdialect.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Interview(
    val id: Long,
    val name: String = "",
    val location: String = ""
) : Parcelable