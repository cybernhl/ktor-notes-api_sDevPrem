package com.sdevprem.data.repository

import com.sdevprem.data.db.schema.NoteSchema
import com.sdevprem.data.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*

class NotesRepository(private val db: Database) : INotesRepository {

    private val ioDispatcher = Dispatchers.IO

    override suspend fun getNotes(uid: Int): List<Note> = withContext(ioDispatcher) {
        db.from(NoteSchema)
            .select()
            .where { NoteSchema.uid eq uid }
            .map { NoteSchema.createEntity(it) }
    }

    override suspend fun getNoteById(uid: Int, id: Int): Note? = withContext(ioDispatcher) {
        db.from(NoteSchema)
            .select()
            .where { (NoteSchema.uid eq uid) and (NoteSchema.id eq id) }
            .map { NoteSchema.createEntity(it) }
            .firstOrNull()
    }

    override suspend fun insertNote(uid: Int, note: Note): Int = withContext(ioDispatcher) {
        db.insertAndGenerateKey(NoteSchema) {
            set(it.uid, uid)
            set(it.description, note.description)
            set(it.title, note.title)
        } as Int
    }

    override suspend fun updateNote(uid: Int, id: Int, note: Note): Int = withContext(ioDispatcher) {
        db.update(NoteSchema) {
            set(it.description, note.description)
            set(it.title, note.title)
            where {
                (it.uid eq uid) and (it.id eq id)
            }
        }
    }

    override suspend fun deleteNote(uid: Int, id: Int): Int = withContext(ioDispatcher) {
        db.delete(NoteSchema) {
            (it.uid eq uid) and (it.id eq id)
        }
    }
}