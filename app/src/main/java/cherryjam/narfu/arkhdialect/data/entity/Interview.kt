package cherryjam.narfu.arkhdialect.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "interviews")
class Interview(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "interviewer") var interviewer: String,
    @ColumnInfo(name = "location") var location: String
) : Parcelable {
    constructor(
        name: String = "",
        interviewer: String = "",
        location: String = ""
    ) : this(null, name, interviewer, location)
}
