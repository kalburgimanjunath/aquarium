package controllers

// Security
import security.{ AkvaarioDeadboltHandler }
import be.objectify.deadbolt.scala.DeadboltActions
import securesocial.core.{ SecureSocial, Identity }
import play.api._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import scala.concurrent.Future
import scala.util.{ Failure, Success }
import models.DAO._
import play.api.libs.ws.WS
import java.io.OutputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
  import play.api.libs.iteratee._

object APIController extends Controller with DeadboltActions {

  // Method for retrieving a project and related users from DB and returns them via API endpoint.
  def project(project: String) = Dynamic("customerProjectPermission", project, new AkvaarioDeadboltHandler) {
    Action.async {

      Logger.info("[APIController] - Starting query for project " + project)

      getProject(project).flatMap { option =>
        option.map{ item =>
          Logger.info("[APIController] - Found project with name " + project + ", starting query for associated users")
          // TODO: Make get users a separate function
          val futureUsers: List[Future[JsObject]] = ((item \ "users").as[List[String]]).map {
            user: String =>
              getUser(user).map {
                name: Option[JsObject] => name.getOrElse(Json.obj("Error" -> user))
              }
          }
          Logger.info("[APIController] - Found " + futureUsers.length + " users associated with " + project)
          Future.sequence(futureUsers).map {
            users => Ok(Json.obj("project" -> item, "users" -> users))
          }
          
        }
        .getOrElse{
          Logger.info("[APIController] - No project with name " + project + " found")
          Future(NotFound)
        }
      }
    }
  }

  def getProjectEmails(project: String) = Dynamic("customerProjectPermission", project, new AkvaarioDeadboltHandler) {
    Action.async {

      getEmails(project).map {
        Logger.info("[APIController] - Starting query for mails associated with " + project)
        mails => Ok(Json.obj("mails" -> mails))

      }
    }
  }

  //Returns JsObject with user-> user and project->project (the projects of the user that was polled)
  def getUserProject() = SubjectPresent(new AkvaarioDeadboltHandler) {
    Action.async { implicit request =>
      SecureSocial.currentUser(request) match {
        case Some(user) =>

          Logger.info("[APIController] - Found a logged on user, starting query for associated projects")
          Logger.debug("[APIController] - Starting query for user with userId " + user.identityId.userId)
          val username = user.identityId.userId
          // TODO. split cases to functions, and change customer project to return list.
          val role = user.identityId.providerId 
          getUserProjectHelper(role,username)

        case None =>
          Logger.info("[APIController] - No currently logged in user found")
          Future(Forbidden)
      }
    }
  }
  def getUserProjectHelper(role: String, username: String) ={
    var result: Future[List[String]] = Future(List())
    role match{
      case "futurice" => result = getFutuUserProjects(username)
      case _          => result = getCustomerProjects(username)
    }
    result.map{ projects =>
      Ok(Json.obj("user" -> username, "projects" -> projects))
    }


  }
    //returns the role of the user
  def getUserRole() = SubjectPresent(new AkvaarioDeadboltHandler) {
    Action.async{ implicit request =>
      SecureSocial.currentUser(request) match{
        case Some(someID) =>
          // TODO: move provideri->"role" to separate function for future use. Take string from AkvaarioUser
          someID.identityId.providerId match {
            case "futurice" => Future(Ok(Json.obj("role"->"EMPLOYEE")))
            case _ => Future(Ok(Json.obj("role"->"CUSTOMER")))

          }
        case None => Future(NotFound)
      }
    }
  }

  // Returns a JsObject with a list of JsObjects
  def getStatusForms(project: String) = Dynamic("customerProjectPermission", project, new AkvaarioDeadboltHandler) {
    Action.async{
      getStatusForm(project).map{
        forms => Ok(Json.obj("forms" -> forms))
      }
    }
  }

  def getLinksForm(project: String) = Dynamic("customerProjectPermission", project, new AkvaarioDeadboltHandler) {
    Action.async{
      getLinks(project) map{option =>
        option.map { links: JsObject =>
            Ok(Json.obj("links" -> links \ "links"))
        }.getOrElse{
          NotFound //Ok(Json.obj("form" -> "")) might be better
        }
      }
    }
  }

  def getCustomerContactInfoForm(project: String) = Dynamic("customerProjectPermission", project, new AkvaarioDeadboltHandler) {
    Action.async{
      getCustomerContactInformation(project) map{
        forms => Ok(Json.obj("forms" -> forms))
      }
    }
  }
  
  def userImage(username: String) = SubjectPresent(new AkvaarioDeadboltHandler) {
    Action.async{ implicit request =>
      getUser(username).flatMap{ userOption =>
        userOption.map { user =>
          WS.url((user \ "portrait_thumb_url").as[String]).get().map { r =>
          	Ok(r.getAHCResponse.getResponseBodyAsBytes).as("image/png")
          }
        }.getOrElse(Future(NotFound))
      }
    }
  }

  
  case class CustomerProject(email: String, project: String)

  val customerProjectForm = Form(
    mapping(
      "email" -> email,
      "project" -> text)(CustomerProject.apply)(CustomerProject.unapply))

  //Adds a customer to a specific project.
  def addCustomerProject() = Restrict (Array("EMPLOYEE"), new AkvaarioDeadboltHandler ) {
    Action.async { implicit request =>
      customerProjectForm.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest),
        data => {
          insertCustomerProject(data.email, data.project) map { lastError =>
            // If lastError.updatExisting is false, the customer didn't exist and was inserted
            if (lastError.ok && lastError.updatedExisting == false) {
              Ok
            }
            // If lastError.updatExisting is true, the customer already exists for that project -> no change
            // TODO: Make prettier?
            else if (lastError.ok && lastError.updatedExisting == true) {
              Ok("customer-exists")
            }
            else {
              InternalServerError(Json.obj("error" -> lastError.errMsg))
            }
          }
        })
    }
  }
}