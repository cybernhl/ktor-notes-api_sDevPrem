package com.sdevprem.data.db.schema

import com.sdevprem.data.model.Note
import org.ktorm.schema.int
import org.ktorm.schema.text
import org.ktorm.schema.varchar
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
object NoteSchema : BaseTable<Note>("t_note") {
    val id = int("id").primaryKey()
    val title = varchar("title")
    val description = text("description")
    val uid = int("uid")

    override fun aliased(alias: String) = this

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean): Note {
        return Note(
            id = row[id]!!,
            title = row[title]!!,
            description = row[description]!!,
            userId = row[uid]!!
        )
    }
}