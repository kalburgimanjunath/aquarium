package controllers

// Security
import securesocial.core.{UserService, SocialUser}

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._



import models._

object Application extends Controller with securesocial.core.SecureSocial{


	def index = SecuredAction { implicit request =>
	    Ok(views.html.index(AkvaarioUser.getCurrentUserRole(request)))
	}
	

	def find(wildcard: String) = Action {
		Redirect(routes.Application.index.url + "#" + wildcard, SEE_OTHER)
	}

	def login = Action { implicit request =>
	    Ok(views.html.index(AkvaarioUser.getCurrentUserRole(request)))
	}
  
}

