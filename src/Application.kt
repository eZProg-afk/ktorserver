package spiral.bit.dev

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import spiral.bit.dev.authentication.JwtService
import spiral.bit.dev.authentication.hash
import spiral.bit.dev.repository.DatabaseFactory
import spiral.bit.dev.repository.NotesRepository
import spiral.bit.dev.repository.UsersRepository
import spiral.bit.dev.routes.NoteRoutes
import spiral.bit.dev.routes.UserRoutes
import kotlin.collections.set

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {

    DatabaseFactory.init()
    val usersRepository = UsersRepository()
    val notesRepository = NotesRepository()
    val jwtService = JwtService()
    val hashFunction = { s: String -> hash(s) }

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Locations) {
    }

    install(Authentication) {
        jwt("jwt") {
            verifier(jwtService.verifier)
            realm = "Note Server"
            validate {
                val payload = it.payload
                val email = payload.getClaim("email").asString()
                val user = usersRepository.getUserByEmail(email)
                user
            }
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO SPIRAL BIT DEV!", contentType = ContentType.Text.Plain)
        }

        UserRoutes(usersRepository, jwtService, hashFunction)
        NoteRoutes(notesRepository, hashFunction)

        route("/notes") {
            post {
                val body = call.receive<Any>()
                call.respond(body)
            }

            delete {
                val body = call.receive<String>()
                call.respond(body)
            }
        }
    }
}

data class MySession(val count: Int = 0)

