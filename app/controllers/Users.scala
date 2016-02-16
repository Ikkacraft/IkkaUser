package controllers

import java.util.UUID
import javax.inject.Inject

import models.{Role, User}
import play.api.libs.json.Json
import play.api.mvc._
import services.UserService

class Users @Inject()(userService: UserService) extends Controller {
  def getUsers() = Action { request =>
    request.accepts("application/json") || request.accepts("text/json") match {
      case true => {
        val users: List[User] = userService.getAll()
        if (users.isEmpty) NoContent else Ok(Json.toJson(users))
      }
      case false => {
        val users: List[User] = userService.getAll()
        if (users.isEmpty) NoContent
        else Ok(<users>{users.map(a => a.toXml)}</users>)
      }
    }
  }

  def getUser(user_id: String) = Action { request =>
    request.accepts("application/json") || request.accepts("text/json") match {
      case true => {
        userService.get(UUID.fromString(user_id)) match {
          case Some(user) => Ok(Json.toJson(user))
          case None => NoContent
        }
      }
      case false => {
        userService.get(UUID.fromString(user_id)) match {
          case Some(user) => Ok(user.toXml)
          case None => NoContent
        }
      }
    }
  }

  def createUser() = Action(parse.json) { request =>
    val uuid: String = (request.body \ "uuid").as[String]
    val role_id: Long = (request.body \ "role_id").as[Long]
    val pseudo: String = (request.body \ "pseudo").as[String]
    val account_description: String = (request.body \ "account_description").as[String]
    val account_balance: BigDecimal = (request.body \ "account_balance").as[BigDecimal]
    val user = new User(UUID.fromString(uuid), 0, role_id, pseudo)

    userService.create(user, account_balance, account_description) match {
      case -1 => BadRequest("The user could not be created")
      case _ => Created("The user has been successfully created : ")
    }
  }

  def updateUser(user_id: String) = Action(parse.json) { request =>
    val account_id: Long = (request.body \ "account_id").as[Long]
    val role_id: Long = (request.body \ "role_id").as[Long]
    val pseudo: String = (request.body \ "pseudo").as[String]
    val flag_connection: Int = (request.body \ "flag_connection").as[Int]
    val token: Option[String] = (request.body \ "token").asOpt[String]
    val town_id: Option[Long] = (request.body \ "town_id").asOpt[Long]

    val user = new User(UUID.fromString(user_id), account_id, role_id, pseudo, flag_connection, token, town_id)
    userService.update(user) match {
      case 0 => BadRequest("The user could not be updated")
      case _ => {
        Ok("The user has been successfully updated")
      }
    }
  }

  def deleteUser(user_id: String) = Action {
    Ok(Json.toJson(userService.delete(UUID.fromString(user_id))))
  }

  def getRoles() = Action { request =>
    request.accepts("application/json") || request.accepts("text/json") match {
      case true => {
        val roles: List[Role] = userService.getRoles()
        if (roles.isEmpty) NoContent else Ok(Json.toJson(roles))
      }
      case false => {
        val roles: List[Role] = userService.getRoles()
        if (roles.isEmpty) NoContent
        else Ok(<roles>{roles.map(a => a.toXml)}</roles>)
      }
    }
  }
}
