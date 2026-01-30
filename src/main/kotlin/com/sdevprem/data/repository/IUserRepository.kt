package com.sdevprem.data.repository

import com.sdevprem.data.model.User

/**
 * 通用的使用者儲存庫介面，屏蔽了底層資料庫實現的細節。
 */
interface IUserRepository {
    // 改為 suspend 函式以兼容 Room 的異步操作
    suspend fun isUserEmailExist(email: String): Boolean
    suspend fun getUserByEmail(email: String): User?
    suspend fun insertUser(user: User): Int
}
