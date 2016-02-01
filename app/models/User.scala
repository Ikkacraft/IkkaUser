package models

import java.util.UUID

import anorm.SqlParser._
import anorm._
import play.api.libs.json.{Json, Writes}

/**
 * Contains data related to a user
 *
 * @param UUID Account Balance
 */
case class User(UUID: UUID, account_id: Long, role_id :Long, pseudo: String, flagConnection: Int, token: Option[String], var town_id: Option[Long]) {
  def this(UUID: UUID, account_id: Long, token: Option[String], flagConnection: Option[Int], town_id: Option[Long])
  = this(UUID, account_id)

  def setTown(v: Option[Long]) { town_id = v }

}

object User {
  implicit val jsonWrites: Writes[User] = Json.writes[User]

  val parser: RowParser[User] = {
    get[UUID]("UUID") ~
      get[Long]("ID_ACCOUNT") ~
      get[Long]("ID_ROLE") ~
      get[String]("PSEUDO") ~
      get[Int]("FLAGCONNECTION") ~
      get[Option[String]]("TOKEN") ~
      get[Option[Long]]("ID_TOWN") map {
      case UUID ~ account_id ~ role_id ~ pseudo ~ flagConnection ~ token ~ town_id =>
        User(UUID, account_id, role_id, pseudo, flagConnection, token, town_id)
    }
  }
}
