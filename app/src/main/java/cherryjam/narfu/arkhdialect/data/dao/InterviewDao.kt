package cherryjam.narfu.arkhdialect.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.data.entity.TextAttachment


data class InterviewWithAttachments(
    @Embedded val interview: Interview,
    @Relation(
        parentColumn = "id",
        entityColumn = "interviewId"
    )
    val textAttachments: List<TextAttachment>,
)

@Dao
interface InterviewDao {
    @Query("SELECT * FROM interviews")
    fun getAll(): LiveData<List<Interview>>

    @Query("SELECT * FROM interviews WHERE id = :id")
    fun getById(id: Int): Interview

    @Transaction
    @Query("SELECT * FROM interviews")
    fun getAllWithAttachments(): List<InterviewWithAttachments>

    @Transaction
    @Query("SELECT * FROM interviews WHERE id = :id")
    fun getByIdWithAttachments(id: Int): InterviewWithAttachments

    @Insert
    fun _insert(interview: Interview): Long

    @Insert
    fun _insert(interviews: List<Interview>): List<Long>

    fun insert(interview: Interview): Interview {
        interview.id = _insert(interview)
        return interview
    }

    fun insert(interviews: List<Interview>) : List<Interview> {
        val ids = _insert(interviews)
        interviews.forEachIndexed { i, it -> it.id = ids[i] }
        return interviews
    }

    @Delete
    fun delete(interview: Interview)

    @Query("DELETE FROM interviews WHERE id = :id")
    fun deleteById(id: Long)

    @Update
    fun update(vararg interview: Interview)

    @Query("SELECT * FROM interviews WHERE name LIKE :searchQuery OR interviewer LIKE :searchQuery")
    fun searchDatabase(searchQuery: String): LiveData<List<Interview>>
}