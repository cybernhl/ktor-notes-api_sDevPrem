package com.sdevprem.data.repository

import com.sdevprem.data.model.Note

/**
 * 通用的筆記儲存庫介面，屏蔽了底層資料庫實現的細節。
 */
interface INotesRepository {
    // 全部改為 suspend函式
    suspend fun getNotes(uid: Int): List<Note>
    suspend fun getNoteById(uid: Int, id: Int): Note?
    suspend fun insertNote(uid: Int, note: Note): Int
    suspend fun updateNote(uid: Int, id: Int, note: Note): Int
    suspend fun deleteNote(uid: Int, id: Int): Int
}
