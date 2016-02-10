package controllers

import java.util.UUID
import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import models.User
import play.api.libs.json.Json
import play.api.mvc._
import services.UserService

import scala.concurrent.Future

class Users @Inject()(userService: UserService) extends Controller {
  def getUsers() = Action {
    Ok(Json.toJson(userService.getAll()))
  }

  def getUser(user_id: String) = Action {

    userService.get(UUID.fromString(user_id)) match {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound
    }
  }

  def createUser() = Action.async(parse.json) { request =>
    val uuid: String = (request.body \ "uuid").as[String]
    val role_id: Long = (request.body \ "role_id").as[Long]
    val pseudo: String = (request.body \ "pseudo").as[String]
    val user = new User(UUID.fromString(uuid), 0, role_id, pseudo)
    val id:Future[Int] = userService.create(user)

    id map {
      case -1 => BadRequest("The user could not be created")
      case _ => Created("The user has been successfully created")
    }
  }

  def updateUser(user_id: String) = Action(parse.json) { implicit request =>
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

  def getBadgesByUser(user_id: String) = play.mvc.Results.TODO
}