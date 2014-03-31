package security

// Security
import be.objectify.deadbolt.scala.{DynamicResourceHandler, DeadboltHandler}
import be.objectify.deadbolt.core.models.Subject
import securesocial.core.{SecureSocial, Identity, IdentityId}

import play.api.mvc.{Request, Result, Results, Controller, SimpleResult}
import play.api.libs.json.JsObject
import play.api.libs.json.Reads._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

import models.{AkvaarioUser,UserRole, Roles}
import models.DAO._

class AkvaarioDeadboltHandler(dynamicResourceHandler: Option[DynamicResourceHandler] = None) extends DeadboltHandler {

  def beforeAuthCheck[A](request: Request[A]) = None

  override def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = {
    if (dynamicResourceHandler.isDefined) dynamicResourceHandler
    else Some(new AkvaarioDynamicResourceHandler())
  }

  override def getSubject[A](request: Request[A]): Option[Subject] = {
    // Add if else statement to differ between single-sigon and user login
    val id: Option[Identity] = SecureSocial.currentUser(request)
    id match {
      case Some(someID) =>
        val provider = someID.identityId.providerId
        val userid = someID.identityId.userId
        Logger.debug("[AkvaarioDeadboltHandler] - Provider: " + provider)
        provider match {

          case "futurice" =>
            getGroup("Futurice").map{ group =>// Checks group from DB
              if (group.contains(userid)) Some(new AkvaarioUser(userid,List(new UserRole(Roles.EMPLOYEE))))
              else Some(new AkvaarioUser(userid)) // user had no rights to be logged in with openID
            } 

            Some(new AkvaarioUser(userid,List(new UserRole(Roles.EMPLOYEE))))

          case _ => //Equals userpass
            val customer_project = Await.result(getCustomerProjects(userid), 5 seconds)
            customer_project match {

              case List(project: String) => Some(new AkvaarioUser(userid, List(new UserRole(Roles.CUSTOMER)), List(project)))

              case List() => Some(new AkvaarioUser(userid)) // User had no projects registered
            }
          
        }
      case None => None
    }
    
  }

  def onAuthFailure[A](request: Request[A]): Future[SimpleResult] = {
    val f: Future[SimpleResult] = future {Results.Forbidden(views.html.errorPage("403"))}
    f
  }
}