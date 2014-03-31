package security

//Security
import be.objectify.deadbolt.scala.{ DynamicResourceHandler, DeadboltHandler }
import securesocial.core.{ SecureSocial, Identity }

import play.api.mvc.{ Controller, SimpleResult, Request }
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.Logger
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.collection.JavaConversions._
import java.lang.System
import collection.immutable.Map

import models.DAO._
import models.{AkvaarioUser,Roles, UserRole}



class AkvaarioDynamicResourceHandler extends DynamicResourceHandler with Controller {

  def isAllowed[A](name: String, meta: String, handler: DeadboltHandler, request: Request[A]) = {
    AkvaarioDynamicResourceHandler.handlers(name).isAllowed(name,
      meta,
      handler,
      request)
    //true
  }

  def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]) = {
    // todo implement this when demonstrating permissions
    false
  }
}

object AkvaarioDynamicResourceHandler extends Controller with MongoController {
  private val logger = play.api.Logger("security.AkvaarioDynamicResourceHandler")

  val handlers: Map[String, DynamicResourceHandler] =
    Map(
      "customerProjectPermission" -> new DynamicResourceHandler() {
        //Name is id of the user trying to access and meta is the project to be accessed
        def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]) = {
          deadboltHandler.getSubject(request) match {
            case Some(subject: AkvaarioUser) => 
              subject.getRoles.toList.exists(_.getName() == Roles.EMPLOYEE) || (subject.getRoles.toList.exists(_.getName() == Roles.CUSTOMER) && subject.getProjects.contains(meta))

            case _ => false
          }
          
        }
        def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]) = false
      })
}