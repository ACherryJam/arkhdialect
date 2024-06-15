package cherryjam.narfu.arkhdialect.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cherryjam.narfu.arkhdialect.data.entity.RecordingAttachment
import java.io.File

@Dao
interface RecordingAttachmentDao {
    @Query("SELECT * FROM recordingAttachments")
    fun getAll(): LiveData<List<RecordingAttachment>>

    @Query("SELECT * FROM recordingAttachments WHERE id = :id")
    fun getById(id: Int): RecordingAttachment

    @Query("SELECT * FROM recordingAttachments WHERE interviewId = :interviewId")
    fun getByInterviewId(interviewId: Long): LiveData<List<RecordingAttachment>>

    @Insert
    fun _insert(photoAttachment: RecordingAttachment): Long

    fun insert(photoAttachment: RecordingAttachment): RecordingAttachment {
        photoAttachment.id = _insert(photoAttachment)
        return photoAttachment
    }

    @Delete
    fun _delete(photoAttachment: RecordingAttachment)

    fun delete(photoAttachment: RecordingAttachment) {
        _delete(photoAttachment)

        photoAttachment.uri.path?.let {
            val file = File(it)
            if (file.exists())
                file.delete()
        }
    }

    @Query("DELETE FROM recordingAttachments WHERE id = :id")
    fun deleteById(id: Long)

    @Update
    fun update(vararg attachments: RecordingAttachment)
}