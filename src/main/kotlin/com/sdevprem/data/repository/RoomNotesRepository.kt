package com.sdevprem.data.repository

import com.sdevprem.data.db.NoteDao
import com.sdevprem.data.model.Note

class RoomNotesRepository(private val noteDao: NoteDao) : INotesRepository {
    override suspend fun getNotes(uid: Int): List<Note> {
        return noteDao.getNotes(uid)
    }

    override suspend fun getNoteById(uid: Int, id: Int): Note? {
        return noteDao.getNoteById(uid, id)
    }

    override suspend fun insertNote(uid: Int, note: Note): Int {
        val noteToInsert = if (note.userId != uid) note.copy(userId = uid) else note
        return noteDao.insertNote(noteToInsert).toInt()
    }

    override suspend fun updateNote(uid: Int, id: Int, note: Note): Int {
        val noteToUpdate = note.copy(id = id, userId = uid)
        return noteDao.updateNote(noteToUpdate)
    }

    override suspend fun deleteNote(uid: Int, id: Int): Int {
        return noteDao.deleteNote(uid, id)
    }
}
