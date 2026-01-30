package com.sdevprem.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sdevprem.data.model.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE userId = :uid")
    suspend fun getNotes(uid: Int): List<Note>

    @Query("SELECT * FROM notes WHERE userId = :uid AND id = :id LIMIT 1")
    suspend fun getNoteById(uid: Int, id: Int): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note): Int

    @Query("DELETE FROM notes WHERE userId = :uid AND id = :id")
    suspend fun deleteNote(uid: Int, id: Int): Int
}
