package controllers

import java.util.UUID
import javax.inject.Inject

import actions.Authorized
import io.swagger.annotations._
import models.{Role, User}
import play.api.libs.json.Json
import play.api.mvc._
import services.UserService

@Api(value = "/users", description = "Operations about users", consumes="application/json, application/xml")
class Users @Inject()(userService: UserService) extends Controller {

  @ApiOperation(
    nickname = "getUsers",
    value = "Get all users",
    notes = "Return a list of users",
    response = classOf[models.User], httpMethod = "GET")
  @ApiResponses(Array(new ApiResponse(code = 404, message = "Users not found"), new ApiResponse(code = 200, message = "Users found")))
  def getUsers() = Authorized { request =>
    request.accepts("application/json") || request.accepts("text/json") match {
      case true => {
        val users: List[User] = userService.getAll()
        if (users.isEmpty) NotFound("Users not found") else Ok(Json.toJson(users))
      }
      case false => {
        val users: List[User] = userService.getAll()
        if (users.isEmpty) NotFound("Users not found") else Ok(<users>{users.map(a => a.toXml)}</users>)
      }
    }
  }

  @ApiOperation(
    nickname = "getUser",
    value = "Get a specific user",
    notes = "Return a user",
    response = classOf[models.User], httpMethod = "GET")
  @ApiResponses(Array(new ApiResponse(code = 404, message = "User not found"), new ApiResponse(code = 200, message = "User found")))
  def getUser(user_id: String) = Authorized { request =>
    request.accepts("application/json") || request.accepts("text/json") match {
      case true => {
        userService.get(UUID.fromString(user_id)) match {
          case Some(user) => Ok(Json.toJson(user))
          case None => NotFound("User not found")
        }
      }
      case false => {
        userService.get(UUID.fromString(user_id)) match {
          case Some(user) => Ok(user.toXml)
          case None => NotFound("User not found")
        }
      }
    }
  }

  @ApiOperation(
    nickname = "createUser",
    value = "Create an user",
    response = classOf[models.User], httpMethod = "POST")
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "The user could not be created"),
    new ApiResponse(code = 200, message = "The user has been successfully created")))
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

  @ApiOperation(
    nickname = "updateUser",
    value = "Update an user",
    response = classOf[models.User], httpMethod = "PUT")
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "The user could not be updated"),
    new ApiResponse(code = 200, message = "The user has been successfully updated")))
  def updateUser(user_id: String) = Authorized(parse.json) { request =>
    val account_id: Long = (request.body \ "account_id").as[Long]
    val role_id: Long = (request.body \ "role_id").as[Long]
    val pseudo: String = (request.body \ "pseudo").as[String]
    val flag_connection: Int = (request.body \ "flag_connection").as[Int]
    val token: Option[String] = (request.body \ "token").asOpt[String]
    val town_id: Option[Long] = (request.body \ "town_id").asOpt[Long]

    val user = new User(UUID.fromString(user_id), account_id, role_id, pseudo, flag_connection, token, town_id)
    userService.update(user) match {
      case 0 => BadRequest("The user could not be updated")
      case _ => Ok("The user has been successfully updated")
    }
  }

  @ApiOperation(
    nickname = "getRoles",
    value = "Get all roles",
    response = classOf[models.User], httpMethod = "GET")
  @ApiResponses(Array(new ApiResponse(code = 404, message = "Roles not found"), new ApiResponse(code = 200, message = "Roles found")))
  def getRoles() = Action { request =>
    request.accepts("application/json") || request.accepts("text/json") match {
      case true => {
        val roles: List[Role] = userService.getRoles()
        if (roles.isEmpty) NotFound("Roles not found") else Ok(Json.toJson(roles))
      }
      case false => {
        val roles: List[Role] = userService.getRoles()
        if (roles.isEmpty) NotFound("Roles not found") else Ok(<roles>{roles.map(a => a.toXml)}</roles>)
      }
    }
  }

  @ApiOperation(
    nickname = "deleteUser",
    value = "Delete a user",
    response = classOf[models.User], httpMethod = "DELETE")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "The user has been successfully deleted")))
  def deleteUser(user_id: String) = Action {
    Ok(Json.toJson(userService.delete(UUID.fromString(user_id))))
  }
}
