package controllers

import java.util.UUID
import javax.inject.Inject

import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Authentification @Inject()(ws: WSClient, userService: UserService) extends Controller {

  def connect(context: String) = Action.async(parse.json) { request =>
    val username: String = (request.body \ "username").as[String]
    val password: String = (request.body \ "password").as[String]

    // Creation du tableau pour le POST vers l'API
    val json: JsValue = Json.obj(
      "agent" -> Json.obj("name" -> "Minecraft", "version" -> 1.8),
      "username" -> username,
      "password" -> password
    )

    val url = "https://authserver.mojang.com/authenticate"
    val futureResponse: Future[WSResponse] = ws.url(url).withHeaders("Content-Type" -> "application/json").post(json)

    futureResponse map {
      case (response) => {
        if (response.status == 200) {
          val id: String     = (response.json \ "selectedProfile" \ "id").as[String]
          val uuid: String   = id.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")
          val pseudo: String = (response.json \ "selectedProfile" \ "name").as[String]
          var token: String  = UUID.randomUUID().toString.replaceAll("-", "")
          if (context == "web"){
            token = userService.webAuthentication(UUID.fromString(uuid), token)
          }
          else
            token = userService.minecraftAuthentication(UUID.fromString(uuid), token)

          val result: JsValue = Json.obj(
            "uuid" -> uuid,
            "token" -> token,
            "pseudo" -> pseudo
          )

          Ok(Json.toJson(result))
        } else {
          BadRequest("The request failed, please contact your admin :" + response.body)
        }
      }
    }
  }

  def disconnect(context: String) = Action(parse.json) { request =>
    val uuid: String = (request.body \ "uuid").as[String]

    if (context == "web") userService.webDisconnection(UUID.fromString(uuid))
    else
      userService.minecraftDisconnection(UUID.fromString(uuid))

    Ok("Disconnection Done")
  }
}