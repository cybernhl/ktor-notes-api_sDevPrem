package com.sdevprem.data

import com.sdevprem.data.db.DBHelper
import com.sdevprem.data.db.RoomDaoProvider
import com.sdevprem.data.repository.*

object RepositoryFactory {
    private const val useRoom = true
    private val db by lazy { DBHelper.database }

    fun createUserRepository(): IUserRepository {
        return if (useRoom) {
            RoomUserRepository(RoomDaoProvider.userDao)
        } else {
            UserRepository(db) // Ktorm
        }
    }

    fun createNotesRepository(): INotesRepository {
        return if (useRoom) {
            RoomNotesRepository(RoomDaoProvider.noteDao)
        } else {
            NotesRepository(db) // Ktorm
        }
    }
}
