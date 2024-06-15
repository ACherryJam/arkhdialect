package cherryjam.narfu.arkhdialect.data.entity

import android.net.wifi.aware.Characteristics
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "cards"
)
class Card(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    @ColumnInfo(name = "word") var word: String,
    @ColumnInfo(name = "location") var location: String,
    @ColumnInfo(name = "characteristics") var characteristics: String,
    @ColumnInfo(name = "meaning") var meaning: String,
    @ColumnInfo(name = "example") var example: String
) : Parcelable {
    constructor(
        word: String = "",
        location: String = "",
        characteristics: String = "",
        meaning: String = "",
        example: String = ""
    ) : this(null, word, location, characteristics, meaning, example)
}