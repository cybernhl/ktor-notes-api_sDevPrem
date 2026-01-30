package com.sdevprem.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sdevprem.data.model.Note
import com.sdevprem.data.model.User

@Database(
    entities = [User::class, Note::class],
    version = 1,
    exportSchema = true
)
abstract class SqliteDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
}
