package models

import anorm._
import anorm.SqlParser._
import play.api.libs.json.{Json, Writes}


case class Role(role_id: Long, label: String)


object Role {
  implicit val jsonWrites: Writes[Role] = Json.writes[Role]

  val parser: RowParser[Role] = {
      get[Long]("ID") ~
      get[String]("LABEL") map {
      case role_id ~ label =>
        Role(role_id, label)
    }
  }
}
