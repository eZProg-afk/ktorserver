package spiral.bit.dev.data.table

import org.jetbrains.exposed.sql.Table

object UserTable : Table(name = "users") {

    val email = varchar("email", 512)
    val name = varchar("name", 512)
    val hashPassword = varchar("hashPassword", 512)

    override val primaryKey: PrimaryKey = PrimaryKey(email)
}