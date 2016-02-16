package models

import java.util.UUID

import anorm._
import anorm.SqlParser._
import play.api.libs.json.{Json, Writes}

/**
 * Contains data related to a user
 *
 * @param uuid Account Balance
 */
case class User(uuid: UUID, account_id: Long, role_id: Long, pseudo: String, flag_connection: Int = 0, token: Option[String], var town_id: Option[Long]) {
  def this(uuid: UUID, account_id: Long, role_id: Long, pseudo: String) {
    this(uuid, account_id, role_id, pseudo, 0, null, null)
  }

  def setTown(v: Option[Long]) {
    town_id = v
  }

  def toXml = {
    <user>
      <uuid>{uuid}</uuid>
      <account_id>{account_id}</account_id>
      <role_id>{role_id}</role_id>
      <pseudo>{pseudo}</pseudo>
      <flag_connection>{flag_connection}</flag_connection>
      {if (token.isDefined) <token>{token.get}</token> else <token />}
      {if (town_id.isDefined) <town_id>{town_id.get}</town_id> else <town_id />}
    </user>
  }
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
      case uuid ~ account_id ~ role_id ~ pseudo ~ flag_connection ~ token ~ town_id =>
        User(uuid, account_id, role_id, pseudo, flag_connection, token, town_id)
    }
  }
}
