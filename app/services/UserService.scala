package services

import java.util.UUID

import anorm._
import com.google.inject.Inject
import models.User
import play.api.Play.current
import play.api.db._
import play.api.libs.json.Json
import play.api.libs.ws._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService @Inject()(ws: WSClient) {

  def getAll(): List[User] = {
    val results: List[User] = DB.withConnection { implicit c =>
      SQL( """SELECT * FROM USER ORDER BY FLAGCONNECTION DESC""").as(User.parser.*)
    }
    results
  }

  def get(user_id: UUID): Option[User] = {
    val result: Option[User] = DB.withConnection { implicit c =>
      SQL( """SELECT * FROM USER WHERE UUID = {user_id}""").on('user_id -> user_id).as(User.parser.singleOpt)
    }
    result
  }

  def create(user: User): Future[Int] = {
    val data = Json.obj(
      "account_balance" -> 200,
      "description" -> " "
    )
    val url = "http://localhost:9000/accounts"
    val futureAccountResponse: Future[WSResponse] = ws.url(url).post(data)

    val result: Future[Int] = futureAccountResponse map { response: WSResponse =>
      insert(user, response.body.toLong)
    }
    result
  }

  def insert(user: User, account_id: Long): Int = {
    val id: Int = DB.withConnection { implicit c =>
      SQL("INSERT INTO USER(UUID, ID_ACCOUNT, ID_ROLE, PSEUDO) VALUES({uuid}, {account_id}, {role_id}, {pseudo})")
        .on('uuid -> user.uuid, 'account_id -> account_id, 'role_id -> user.role_id, 'pseudo -> user.pseudo).executeUpdate()
    }
    id
  }

  def update(user: User): Int = {
    val id: Int = DB.withConnection { implicit c =>
      SQL("UPDATE USER SET " +
        "UUID = {uuid}, " +
        "ID_ACCOUNT = {account_id}, " +
        "ID_TOWN = {town_id}, " +
        "ID_ROLE = {role_id}, " +
        "PSEUDO = {pseudo}, " +
        "TOKEN = {token}, " +
        "FLAGCONNECTION = {flag_connection} WHERE UUID = {uuid}")
        .on('uuid -> user.uuid, 'account_id -> user.account_id, 'town_id -> user.town_id, 'role_id -> user.role_id, 'pseudo -> user.pseudo, 'token -> user.token, 'flag_connection -> user.flag_connection).executeUpdate()
    }
    id
  }

  // TODO: delete account/badges associés (transaction maybe)
  def delete(user_id: UUID): Int = {
    val id: Int = DB.withConnection { implicit c =>
      SQL("DELETE FROM USER WHERE UUID = {user_id}").on('user_id -> user_id).executeUpdate()
    }
    id
  }
}
