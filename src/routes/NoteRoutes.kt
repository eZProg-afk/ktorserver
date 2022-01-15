package spiral.bit.dev.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.routing.post
import org.jetbrains.exposed.sql.not
import spiral.bit.dev.data.model.Note
import spiral.bit.dev.data.model.SimpleResponse
import spiral.bit.dev.data.model.User
import spiral.bit.dev.repository.NotesRepository
import spiral.bit.dev.repository.UsersRepository
import java.lang.Exception

const val NOTES = "$API_VERSION/notes"
const val CREATE_NOTE = "$NOTES/create"
const val UPDATE_NOTE = "$NOTES/update"
const val DELETE_NOTE = "$NOTES/delete"

@Location(NOTES)
class NoteGetRoute

@Location(CREATE_NOTE)
class NoteCreateRoute

@Location(UPDATE_NOTE)
class NoteUpdateRoute

@Location(DELETE_NOTE)
class NoteDeleteRoute

fun Route.NoteRoutes(notesRepository: NotesRepository, hashFunction: (String) -> String) {
    authenticate("jwt") {
        post<NoteCreateRoute> {
            val note = try {
                call.receive<Note>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Missing fields"))
                return@post
            }

            try {
                val email = call.principal<User>()!!.email
                notesRepository.add(note, email)
                call.respond(HttpStatusCode.OK, SimpleResponse(true, "Note Added Successfully"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occured"))
            }
        }

        get<NoteGetRoute> {
            try {
                val email = call.principal<User>()!!.email
                val notes = notesRepository.getAllNotes(email)
                call.respond(HttpStatusCode.OK, notes)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occured"))
            }
        }

        post<NoteUpdateRoute> {
            val note = try {
                call.receive<Note>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Missing fields"))
                return@post
            }

            try {
                val email = call.principal<User>()!!.email
                notesRepository.update(note, email)
                call.respond(HttpStatusCode.OK, SimpleResponse(true, "Note Updated Successfully"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occured"))
            }
        }

        delete<NoteDeleteRoute> {
            val noteId = try {
                call.request.queryParameters["id"]!!
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Query parameter id is not present!"))
                return@delete
            }

            try {
                val email = call.principal<User>()!!.email
                notesRepository.delete(noteId, email)
                call.respond(HttpStatusCode.OK, SimpleResponse(true, "Note deleted successfully!"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occured."))
            }
        }
    }
}