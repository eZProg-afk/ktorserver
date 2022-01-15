package spiral.bit.dev.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import spiral.bit.dev.data.model.Note
import spiral.bit.dev.data.model.User
import spiral.bit.dev.data.table.NoteTable
import spiral.bit.dev.data.table.UserTable
import spiral.bit.dev.repository.DatabaseFactory.dbQuery

class NotesRepository {

    suspend fun add(note: Note, email: String) = dbQuery {
        NoteTable.insert { noteTable ->
            noteTable[id] = note.id
            noteTable[userEmail] = email
            noteTable[noteTitle] = note.noteTitle
            noteTable[description] = note.description
            noteTable[date] = note.date
        }
    }

    suspend fun update(note: Note, email: String) = dbQuery {
        NoteTable.update(where = { NoteTable.userEmail.eq(email) and NoteTable.id.eq(note.id) }) { noteTable ->
            noteTable[noteTitle] = note.noteTitle
            noteTable[description] = note.description
            noteTable[date] = note.date
        }
    }

    suspend fun delete(noteId: String, email: String) = dbQuery {
        NoteTable.deleteWhere { NoteTable.userEmail.eq(email) and NoteTable.id.eq(noteId) }
    }

    suspend fun getAllNotes(email: String): List<Note> = dbQuery {
        NoteTable.select {
            NoteTable.userEmail.eq(email)
            }.mapNotNull { it.mapToNote() }
    }

    private fun ResultRow?.mapToNote(): Note? = this?.let { Note(
        id = this[NoteTable.id],
        noteTitle = this[NoteTable.noteTitle],
        description = this[NoteTable.description],
        date = this[NoteTable.date]
    ) } ?: run { null }
}