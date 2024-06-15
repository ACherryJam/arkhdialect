package cherryjam.narfu.arkhdialect.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cherryjam.narfu.arkhdialect.data.entity.PhotoAttachment
import java.io.File

@Dao
interface PhotoAttachmentDao {
    @Query("SELECT * FROM photoAttachments")
    fun getAll(): LiveData<List<PhotoAttachment>>

    @Query("SELECT * FROM photoAttachments WHERE id = :id")
    fun getById(id: Int): PhotoAttachment

    @Query("SELECT * FROM photoAttachments WHERE interviewId = :interviewId")
    fun getByInterviewId(interviewId: Long): LiveData<List<PhotoAttachment>>

    @Insert
    fun _insert(photoAttachment: PhotoAttachment): Long

    fun insert(photoAttachment: PhotoAttachment): PhotoAttachment {
        photoAttachment.id = _insert(photoAttachment)
        return photoAttachment
    }

    @Delete
    fun _delete(photoAttachment: PhotoAttachment)

    fun delete(photoAttachment: PhotoAttachment) {
        _delete(photoAttachment)

        photoAttachment.uri.path?.let {
            val file = File(it)
            if (file.exists())
                file.delete()
        }
    }

    @Query("DELETE FROM photoAttachments WHERE id = :id")
    fun deleteById(id: Long)

    @Update
    fun update(vararg attachments: PhotoAttachment)
}