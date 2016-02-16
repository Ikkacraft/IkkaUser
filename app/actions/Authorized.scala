package actions

import java.util.UUID

import play.api.libs.ws.{WS, WSClient}
import play.api.mvc._
import services.UserService
import play.api.Play.current
import scala.concurrent.Future

class Authorized extends ActionBuilder[Request] with ActionFilter[Request] {

  private val http: WSClient = WS.client
  private val userService: UserService = new UserService()


  private val unauthorized =
    Results.Unauthorized.withHeaders("WWW-Authenticate" -> "Basic realm=Unauthorized")

  def filter[A](request: Request[A]): Future[Option[Result]] = {
    val result = request.headers.get("Authorization") map { authHeader =>
      val (uuid, token: String) = decodeBasicAuth(authHeader)
      userService.get(UUID.fromString(uuid)) match {
        case Some(user) => {
          if (UUID.fromString(uuid) == user.uuid && token == user.token.getOrElse("")) None else Some(unauthorized)
        }
        case None => Some(unauthorized)
      }

    } getOrElse Some(unauthorized)

    Future.successful(result)
  }

  private[this] def decodeBasicAuth(authHeader: String): (String, String) = {
    val baStr = authHeader.replaceFirst("Basic ", "")
    val decoded = new sun.misc.BASE64Decoder().decodeBuffer(baStr)
    val Array(user, password) = new String(decoded).split(":")
    (user, password)
  }
}

object Authorized extends Authorized
