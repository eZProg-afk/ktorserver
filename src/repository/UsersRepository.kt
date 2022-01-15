package spiral.bit.dev.repository

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import spiral.bit.dev.data.model.User
import spiral.bit.dev.data.table.UserTable
import spiral.bit.dev.repository.DatabaseFactory.dbQuery

class UsersRepository {

    suspend fun add(user: User) = dbQuery {
        UserTable.insert { userTable ->
            userTable[email] = user.email
            userTable[name] = user.userName
            userTable[hashPassword] = user.hashPassword
        }
    }

    suspend fun getUserByEmail(email: String) = dbQuery {
        UserTable.select { UserTable.email.eq(email) }
            .singleOrNull().mapToUser()
    }

    private fun ResultRow?.mapToUser(): User? = this?.let { User(
        email = this[UserTable.email],
        userName = this[UserTable.name],
        hashPassword = this[UserTable.hashPassword]
    ) } ?: run { null }
}