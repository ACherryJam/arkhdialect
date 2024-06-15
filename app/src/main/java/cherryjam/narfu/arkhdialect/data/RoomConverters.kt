package cherryjam.narfu.arkhdialect.data

import android.net.Uri
import androidx.room.TypeConverter

class RoomConverters {
    // Taken from https://stackoverflow.com/a/49484603
    @TypeConverter
    fun fromString(value: String?): Uri? {
        return if (value == null) null else Uri.parse(value)
    }

    @TypeConverter
    fun toString(uri: Uri?): String? {
        return uri?.toString()
    }
}