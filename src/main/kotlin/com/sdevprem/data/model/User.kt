package com.sdevprem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userName: String,
    val email: String,
    @get:JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val password: String
)