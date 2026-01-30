package com.sdevprem.data.service

import at.favre.lib.crypto.bcrypt.BCrypt
import com.sdevprem.data.model.User
import com.sdevprem.data.repository.IUserRepository // <-- 依賴介面

class UserService(
    private val userRepository: IUserRepository // <-- 使用介面類型
) {
    // 加上 suspend
    suspend fun isUserEmailExist(email: String) =
        userRepository.isUserEmailExist(email)

    // 加上 suspend
    suspend fun getUserByEmail(email: String) =
        userRepository.getUserByEmail(email)

    // 加上 suspend
    suspend fun insertUser(user: User): User {
        val hashedPassword = BCrypt.withDefaults().hashToString(12, user.password.toCharArray())
        // 注意：這裡我們假設 insertUser 返回的是新 ID
        val newId = userRepository.insertUser(user.copy(password = hashedPassword))
        return user.copy(id = newId)
    }

    fun isUserPasswordValid(userPassword: String, dbPassword: String) =
        BCrypt.verifyer().verify(userPassword.toCharArray(), dbPassword)
            .verified
}
