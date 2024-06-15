package cherryjam.narfu.arkhdialect.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "textAttachments",
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
data class TextAttachment(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    @ColumnInfo(name = "interviewId", index = true) var interviewId: Long,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "content") var content: String
) : Parcelable {
    constructor(interviewId: Long,
                title: String="",
                content: String="") : this(null, interviewId, title, content)
}
