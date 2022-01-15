package spiral.bit.dev.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import spiral.bit.dev.authentication.JwtService
import spiral.bit.dev.data.model.LoginRequest
import spiral.bit.dev.data.model.RegisterRequest
import spiral.bit.dev.data.model.SimpleResponse
import spiral.bit.dev.data.model.User
import spiral.bit.dev.repository.UsersRepository

const val API_VERSION = "/v1"
const val USERS = "$API_VERSION/users"
const val REGISTER_REQUEST = "$USERS/register"
const val LOGIN_REQUEST = "$USERS/login"

@Location(REGISTER_REQUEST)
class UserRegisterRoute

@Location(LOGIN_REQUEST)
class UserLoginRoute

fun Route.UserRoutes(
    usersRepository: UsersRepository,
    jwtService: JwtService,
    hashFunction: (String) -> String
) {
    post<UserRegisterRoute> {
        val registerRequest = try {
            call.receive<RegisterRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Missing some fields"))
            return@post
        }

        try {
            val user = User(registerRequest.email, hashFunction(registerRequest.password), registerRequest.name)
            usersRepository.add(user)
            call.respond(HttpStatusCode.OK, SimpleResponse(true, jwtService.generateToken(user)))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some error occured."))
        }
    }

    post<UserLoginRoute> {
        val loginRequest = try {
            call.receive<LoginRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Missing some fields"))
            return@post
        }

        try {
            val user = usersRepository.getUserByEmail(loginRequest.email)
            user?.let {
                if (it.hashPassword == hashFunction(loginRequest.password)) {
                    call.respond(HttpStatusCode.OK, SimpleResponse(true, jwtService.generateToken(user)))
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        SimpleResponse(
                            false,
                            "Password incorrect"
                        )
                    )
                }
            } ?: run {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, "Wrong Email id."))
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                SimpleResponse(
                    false,
                    e.message ?: "Some problem"
                )
            )
        }
    }
}