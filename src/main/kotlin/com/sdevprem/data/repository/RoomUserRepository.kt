package com.sdevprem.data.repository

import com.sdevprem.data.db.UserDao
import com.sdevprem.data.model.User

class RoomUserRepository(private val userDao: UserDao) : IUserRepository {
    override suspend fun isUserEmailExist(email: String): Boolean {
        return userDao.getUserByEmail(email) != null
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    override suspend fun insertUser(user: User): Int {
        return userDao.insertUser(user).toInt()
    }
}
