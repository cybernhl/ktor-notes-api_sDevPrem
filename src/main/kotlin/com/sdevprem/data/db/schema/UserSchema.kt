package com.sdevprem.data.db.schema

import com.sdevprem.data.model.User
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.int
import org.ktorm.schema.varchar
object UserSchema : BaseTable<User>("t_user") {
    val id = int("id").primaryKey()
    val userName = varchar("userName")
    val email = varchar("email")
    val password = varchar("password")

    override fun aliased(alias: String) = this

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean): User {
        return User(
            id = row[id]!!,
            userName = row[userName]!!,
            email = row[email]!!,
            password = row[password]!!
        )
    }
}