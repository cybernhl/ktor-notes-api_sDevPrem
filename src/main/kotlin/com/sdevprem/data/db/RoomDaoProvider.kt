package com.sdevprem.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.io.File

object RoomDaoProvider {
    private val db: SqliteDatabase by lazy {
        val dbFile = File("build/db", "notes_room.db")
        dbFile.parentFile.mkdirs()
        Room.databaseBuilder<SqliteDatabase>(
            name = dbFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(connection: SQLiteConnection) {
                    super.onCreate(connection)
                    println("Show me sqlite db onCreate connection is $connection")
                }

                override fun onOpen(connection: SQLiteConnection) {
                    super.onOpen(connection)
                    println("Show me sqlite db onOpen connection is $connection")
                }
            })
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations( object : Migration(1, 2) {
                override fun migrate(connection: SQLiteConnection) {
                    // We can perform actions like
                    /*database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER, `name` TEXT, " +
                            "PRIMARY KEY(`id`))")*/
                }
            })
            .build()
    }

    val userDao: UserDao by lazy { db.userDao() }
    val noteDao: NoteDao by lazy { db.noteDao() }
}
