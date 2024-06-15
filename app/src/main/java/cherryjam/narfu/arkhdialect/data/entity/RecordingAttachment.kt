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
    tableName = "recordingAttachments",
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
class RecordingAttachment(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    @ColumnInfo(name = "interviewId", index = true) var interviewId: Long,
    @ColumnInfo(name = "uri") var uri: Uri,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "timestamp") var timestamp: Int,
    @ColumnInfo(name = "duration") var duration: Int
) : Parcelable {
    constructor(interviewId: Long,
                uri: Uri,
                name: String = "",
                timestamp: Int = -1,
                duration: Int = -1)
            : this(null, interviewId, uri, name, timestamp, duration)
}