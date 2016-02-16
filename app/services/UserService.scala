package services

import java.util.UUID

import anorm._
import models.{Role, User}
import play.api.Play.current
import play.api.db._

class UserService {

  def webAuthentication(uuid: UUID, token: String): Int = {
    val id: Int = DB.withTransaction { implicit c =>
      SQL("UPDATE USER SET TOKEN = {token}, FLAGCONNECTION=1 WHERE UUID = {uuid} AND FLAGCONNECTION=0").on('uuid -> uuid, 'token -> token).executeUpdate()
      SQL("UPDATE USER SET TOKEN = {token}, FLAGCONNECTION=3 WHERE UUID = {uuid} AND FLAGCONNECTION=2").on('uuid -> uuid, 'token -> token).executeUpdate()
      SQL("commit;").executeUpdate()
    }
    id
  }

  def webDisconnection(uuid: UUID): Int = {
    val id: Int = DB.withTransaction { implicit c =>
      SQL("UPDATE USER SET FLAGCONNECTION=2 WHERE UUID = {uuid} AND FLAGCONNECTION=3").on('uuid -> uuid).executeUpdate()
      SQL("UPDATE USER SET TOKEN = '', FLAGCONNECTION=0 WHERE UUID = {uuid} AND FLAGCONNECTION=1").on('uuid -> uuid).executeUpdate()
      SQL("commit;").executeUpdate()
    }
    id
  }

  def minecraftAuthentication(uuid: UUID, token: String): Int = {
    val id: Int = DB.withTransaction { implicit c =>
      SQL("UPDATE USER SET TOKEN = {token}, FLAGCONNECTION=2 WHERE UUID = {uuid} AND FLAGCONNECTION=0").on('uuid -> uuid, 'token -> token).executeUpdate()
      SQL("UPDATE USER SET TOKEN = {token}, FLAGCONNECTION=3 WHERE UUID = {uuid} AND FLAGCONNECTION=1").on('uuid -> uuid, 'token -> token).executeUpdate()
      SQL("commit;").executeUpdate()
    }
    id
  }

  def minecraftDisconnection(uuid: UUID): Int = {
    val id: Int = DB.withTransaction { implicit c =>
      SQL("UPDATE USER SET FLAGCONNECTION=2 WHERE UUID = {uuid} AND FLAGCONNECTION=3").on('uuid -> uuid).executeUpdate()
      SQL("UPDATE USER SET TOKEN = '', FLAGCONNECTION=0 WHERE UUID = {uuid} AND FLAGCONNECTION=2").on('uuid -> uuid).executeUpdate()
      SQL("commit;").executeUpdate()
    }
    id
  }

  def getRoles(): List[Role] = {
    val results: List[Role] = DB.withConnection { implicit c =>
      SQL( """SELECT * FROM ROLE""").as(Role.parser.*)
    }
    results
  }


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

  def create(user: User, account_balance:BigDecimal, description:String): Int  = {
    val id: Int = DB.withTransaction { implicit c =>
      val account_id: Option[Long] = DB.withConnection { implicit c =>
        SQL("INSERT INTO ACCOUNT(account_balance, description) VALUES({account_balance}, {description})")
          .on('account_balance -> account_balance, 'description -> description).executeInsert()
      }
      account_id.getOrElse(-1)
      SQL("INSERT INTO USER(UUID, ID_ACCOUNT, ID_ROLE, PSEUDO) VALUES({uuid}, {account_id}, {role_id}, {pseudo})")
        .on('uuid -> user.uuid, 'account_id -> account_id, 'role_id -> user.role_id, 'pseudo -> user.pseudo).executeInsert()
      SQL("commit;").executeUpdate()
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

  def delete(user_id: UUID): Int = {

    val id: Int = DB.withTransaction { implicit c =>
      SQL("SELECT @account_id:=ID_ACCOUNT FROM USER WHERE UUID = {user_id}").on('user_id -> user_id).execute()
      SQL("UPDATE USER SET ID_ACCOUNT = null WHERE UUID = {user_id}").on('user_id -> user_id).executeUpdate()
      SQL("DELETE FROM ACCOUNT WHERE ID = @account_id").on('user_id -> user_id).executeUpdate()
      SQL("DELETE FROM UNBLOCK WHERE UUID = {user_id}").on('user_id -> user_id).executeUpdate()
      SQL("DELETE FROM USER WHERE UUID = {user_id}").on('user_id -> user_id).executeUpdate()
      SQL("commit;").executeUpdate()
    }
    id
  }
}
