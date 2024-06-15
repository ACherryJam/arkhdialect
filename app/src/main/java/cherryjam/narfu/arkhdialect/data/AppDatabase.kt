package cherryjam.narfu.arkhdialect.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cherryjam.narfu.arkhdialect.data.dao.*
import cherryjam.narfu.arkhdialect.data.entity.*

@Database(
    entities = [
        Interview::class,
        Card::class,
        TextAttachment::class,
        PhotoAttachment::class,
    ],
    version = AppDatabase.VERSION,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun interviewDao() : InterviewDao
    abstract fun textAttachmentDao() : TextAttachmentDao
    abstract fun photoAttachmentDao() : PhotoAttachmentDao
    abstract fun cardDao() : CardDao

    companion object {
        const val VERSION = 8 // Increment on schema change
        private var instance: AppDatabase? = null

        fun createInstance(context: Context) {
            instance = Room
                .databaseBuilder(context.applicationContext, AppDatabase::class.java, "arch")
                .fallbackToDestructiveMigration()
                .build()
        }

        @Synchronized
        fun getInstance(): AppDatabase {
            instance ?: throw NullPointerException("Create database instance with createInstance()")
            return instance!!
        }
    }
}