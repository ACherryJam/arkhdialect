package cherryjam.narfu.arkhdialect.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cherryjam.narfu.arkhdialect.data.entity.Card

@Dao
interface CardDao {
    @Query("SELECT * FROM cards")
    fun getAll(): LiveData<List<Card>>

    @Query("SELECT * FROM cards WHERE id = :id")
    fun getById(id: Int): Card

    @Insert
    fun _insert(card: Card): Long

    @Insert
    fun _insert(cards: List<Card>): List<Long>

    fun insert(card: Card): Card {
        card.id = _insert(card)
        return card
    }

    fun insert(cards: List<Card>) : List<Card> {
        val ids = _insert(cards)
        cards.forEachIndexed { i, it -> it.id = ids[i] }
        return cards
    }

    @Delete
    fun delete(card: Card)

    @Query("DELETE FROM cards WHERE id = :id")
    fun deleteById(id: Long)

    @Update
    fun update(vararg card: Card)
}