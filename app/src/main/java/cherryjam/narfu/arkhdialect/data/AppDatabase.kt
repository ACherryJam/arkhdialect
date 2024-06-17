package cherryjam.narfu.arkhdialect.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cherryjam.narfu.arkhdialect.data.dao.CardDao
import cherryjam.narfu.arkhdialect.data.dao.InterviewDao
import cherryjam.narfu.arkhdialect.data.dao.PhotoAttachmentDao
import cherryjam.narfu.arkhdialect.data.dao.RecordingAttachmentDao
import cherryjam.narfu.arkhdialect.data.dao.TextAttachmentDao
import cherryjam.narfu.arkhdialect.data.entity.Card
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.data.entity.PhotoAttachment
import cherryjam.narfu.arkhdialect.data.entity.RecordingAttachment
import cherryjam.narfu.arkhdialect.data.entity.TextAttachment

@Database(
    entities = [
        Interview::class,
        Card::class,
        TextAttachment::class,
        PhotoAttachment::class,
        RecordingAttachment::class
    ],
    version = AppDatabase.VERSION,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun interviewDao() : InterviewDao
    abstract fun textAttachmentDao() : TextAttachmentDao
    abstract fun photoAttachmentDao() : PhotoAttachmentDao
    abstract fun recordingAttachmentDao() : RecordingAttachmentDao
    abstract fun cardDao() : CardDao

    companion object {
        const val VERSION = 9 // Increment on schema change
        private var instance: AppDatabase? = null

        fun createInstance(context: Context) {
            instance = Room
                .databaseBuilder(context.applicationContext, AppDatabase::class.java, "arch")
                .fallbackToDestructiveMigration()
                .build()
        }

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            instance ?: createInstance(context)
            return instance!!
        }
    }
}