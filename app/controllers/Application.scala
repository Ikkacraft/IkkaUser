package controllers

import actions.Authorized
import play.api.mvc.Controller


class Application extends Controller {


  def index = Authorized {
    Ok("Your new application is ready.")
  }

  def authorized = Authorized {
    Ok("Your are authorized")
  }
}