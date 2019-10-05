package me.duncanleo.mc_auth.model

import org.jetbrains.exposed.sql.*

object Users : Table() {
  val id = integer("id").autoIncrement().primaryKey()
  val name = varchar("name", length = 50).index()
  val passwordHash = varchar("password_hash", length = 100)
}
