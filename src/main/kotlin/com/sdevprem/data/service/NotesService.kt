package com.sdevprem.data.service

import com.sdevprem.data.model.Note
import com.sdevprem.data.repository.INotesRepository // <-- 依賴介面

class NotesService(
    private val notesRepository: INotesRepository // <-- 使用介面類型
) {
    // 全部加上 suspend
    suspend fun getNotes(uid: Int) =
        notesRepository.getNotes(uid)

    suspend fun deleteNote(uid: Int, noteId: Int) =
        notesRepository.deleteNote(uid, noteId)

    suspend fun updateNote(uid: Int, noteId: Int, note: Note) =
        notesRepository.updateNote(uid, noteId, note)

    suspend fun createNote(uid: Int, note: Note) =
        notesRepository.insertNote(uid, note)

    suspend fun getNoteById(uid: Int, noteId: Int) =
        notesRepository.getNoteById(uid, noteId)
}
