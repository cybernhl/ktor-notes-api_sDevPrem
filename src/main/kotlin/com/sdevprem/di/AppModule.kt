package com.sdevprem.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sdevprem.data.db.NoteDao
import com.sdevprem.data.db.SqliteDatabase
import com.sdevprem.data.db.UserDao
import com.sdevprem.data.repository.INotesRepository
import com.sdevprem.data.repository.IUserRepository
import com.sdevprem.data.service.NotesService
import com.sdevprem.data.service.UserService
import io.ktor.server.application.Application
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import com.sdevprem.data.repository.*
import kotlinx.coroutines.Dispatchers
import org.ktorm.database.Database
import java.io.File

val appModule = module {
    single<String>(named("jwtSecret")) {
//        val application = get<Application>()
//        application.environment.config.property("jwt.secret").getString()
        generateSecureSecret(32)
//        generateSecureSecret(64)
    }

    single<SqliteDatabase> {
        val dbFile = File("build/db", "notes_room.db").apply { parentFile.mkdirs() }
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

    single<Database> {
        val config = get<Application>().environment.config // ✅ 在 single 內部安全地 get()
        Database.connect(
            url = config.property("db.config.db_url").getString(),
            user = config.property("db.config.db_user").getString(),
            password = config.property("db.config.db_pwd").getString(),
            driver = "com.mysql.cj.jdbc.Driver",
        )
    }

    single<UserDao> { get<SqliteDatabase>().userDao() }
    single<NoteDao> { get<SqliteDatabase>().noteDao() }

    single<IUserRepository> {
        val dbType = get<Application>().environment.config.property("app.database_type").getString()
        if (dbType == "room") {
            RoomUserRepository(get())
        } else {
            UserRepository(get())
        }
    }

    single<INotesRepository> {
        val dbType = get<Application>().environment.config.property("app.database_type").getString()
        if (dbType == "room") {
            RoomNotesRepository(get())
        } else {
            NotesRepository(get())
        }
    }

    single { UserService(get()) }
    single { NotesService(get()) }
}

@OptIn(ExperimentalEncodingApi::class)
private fun generateSecureSecret(lengthBytes: Int = 32): String {
    val key = ByteArray(lengthBytes)
    Random.Default.nextBytes(key)
    return Base64.encode(key)
}