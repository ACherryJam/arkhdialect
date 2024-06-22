package cherryjam.narfu.arkhdialect.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "interviews")
data class Interview(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "location") var location: String,
    @ColumnInfo(name = "interviewer") var interviewer: String
) : Parcelable {
    constructor(
        name: String="",
        location: String="",
        interviewer: String="") : this(null, name, location, interviewer)
}
