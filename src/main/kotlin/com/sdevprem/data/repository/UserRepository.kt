package com.sdevprem.data.repository

import com.sdevprem.data.db.schema.UserSchema
import com.sdevprem.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*

class UserRepository(private val db: Database) : IUserRepository {
    private val ioDispatcher = Dispatchers.IO

    override suspend fun isUserEmailExist(email: String): Boolean = withContext(ioDispatcher) {
        db.from(UserSchema)
            .select(UserSchema.id)
            .where { UserSchema.email eq email }
            .limit(1)
            .map { it[UserSchema.id] }
            .firstOrNull() != null
    }

    override suspend fun getUserByEmail(email: String): User? = withContext(ioDispatcher) {
        db.from(UserSchema)
            .select()
            .where { UserSchema.email eq email }
            .map { UserSchema.createEntity(it) }
            .firstOrNull()
    }

    override suspend fun insertUser(user: User): Int = withContext(ioDispatcher) {
        db.insertAndGenerateKey(UserSchema) {
            set(it.email, user.email)
            set(it.userName, user.userName)
            set(it.password, user.password)
        } as Int
    }
}