package cherryjam.narfu.arkhdialect.data.entity

import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "photoAttachments",
    foreignKeys = [
        ForeignKey(
            entity = Interview::class,
            parentColumns = ["id"],
            childColumns = ["interviewId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class PhotoAttachment(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    @ColumnInfo(name = "interviewId", index = true) var interviewId: Long,
    @ColumnInfo(name = "uri") var uri: Uri
) : Parcelable {
    constructor(interviewId: Long, uri: Uri) : this(null, interviewId, uri)
}