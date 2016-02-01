package services

import models.User
import play.api.Play._
import play.api.db._
import anorm._


class UserService {

  def getAll() = {
    val results: List[User] = DB.withConnection { implicit c =>
      SQL( """SELECT * FROM USER """).as(User.parser.*)
    }
    results
  }
}
