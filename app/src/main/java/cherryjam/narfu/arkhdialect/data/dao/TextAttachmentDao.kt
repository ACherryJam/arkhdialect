package cherryjam.narfu.arkhdialect.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cherryjam.narfu.arkhdialect.data.entity.TextAttachment

@Dao
interface TextAttachmentDao {
    @Query("SELECT * FROM textAttachments")
    fun getAll(): LiveData<List<TextAttachment>>

    @Query("SELECT * FROM textAttachments WHERE id = :id")
    fun getById(id: Int): TextAttachment

    @Query("SELECT * FROM textAttachments WHERE interviewId = :interviewId")
    fun getByInterviewId(interviewId: Long?): LiveData<List<TextAttachment>>

    @Insert
    fun _insert(textAttachment: TextAttachment): Long

    fun insert(textAttachment: TextAttachment): TextAttachment {
        textAttachment.id = _insert(textAttachment)
        return textAttachment
    }

    @Delete
    fun delete(textAttachment: TextAttachment)

    @Query("DELETE FROM textAttachments WHERE id = :id")
    fun deleteById(id: Long)

    @Update
    fun update(vararg attachments: TextAttachment)
}