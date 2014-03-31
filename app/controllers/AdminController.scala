package controllers

// Security
import securesocial.core.{ UserService, SocialUser, SecureSocial }
import securesocial.controllers.Registration._
import be.objectify.deadbolt.scala.DeadboltActions
import security.{ AkvaarioDeadboltHandler }

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{ Json, JsObject, JsArray, JsValue }
import play.api.data._
import play.api.data.Forms._
import play.api.libs.ws.WS
import scala.concurrent.Future
import scala.collection.mutable._
import akka.actor.Status.Success
import reactivemongo.api.Cursor
import java.util.Calendar

import models._
import models.DAO._

object AdminController extends Controller with securesocial.core.SecureSocial with DeadboltActions {

  case class ProjectStatus(spentBudget: Option[Int], budgetNotes: String, spentHours: Option[Int], hourNotes: String, workStatus: String, holidays: String, project: String)
  case class CustomerProject(emails: List[String], project: String)
  case class ProjectLinks(labels: List[String], urls: List[String], project: String)
  case class ContactInfo(name: String, title: String, email: String, phoneNumber: String, project: String)

/*------------------------------------------------------Forms---------------------------------------------------------------------*/

  val statusForm = Form(
    mapping(
      "spentBudget" -> optional(number),
      "budgetNotes" -> text,
      "spentHours" -> optional(number),
      "hourNotes" -> text,
      "workStatus" -> text,
      "holidays" -> text,
      "project" -> text
    )(ProjectStatus.apply)(ProjectStatus.unapply)
	)


  val customerProjectForm = Form(
    mapping(
      "emails" -> list(email),
      "project" -> text
    )(CustomerProject.apply)(CustomerProject.unapply)
  )

  val projectLinksForm = Form(
    mapping(
      "labels" -> list(text),
      "urls" -> list(text),
      "project" -> text
    )(ProjectLinks.apply)(ProjectLinks.unapply)
  )

  val contactInfoForm = Form(
    mapping(
      "name" -> text,
      "title" -> text,
      "email" -> text,
      "phoneNumber" -> text,
      "project" -> text
      )(ContactInfo.apply)(ContactInfo.unapply)
    )

/*------------------------------------------------------Form visualization--------------------------------------------------------*/
  
  def showProjectStatusForm(project: String) = Restrict (Array("EMPLOYEE"), new AkvaarioDeadboltHandler ) {
    Action.async { implicit request =>
        projectExists(project) match{
          case true =>
            val user: String = SecureSocial.currentUser(request).get.identityId.userId
            val projects = getDeveloperProjects(user) 
            projects.map { projects =>
              Ok(views.html.status(statusForm, project, projects))
            }
          case false => Future(NotFound)
        }
      
    }
  }
  
  def showAddCustomerForm(project: String) = Restrict (Array("EMPLOYEE"), new AkvaarioDeadboltHandler ) {
    Action.async { implicit request =>
      projectExists(project) match{
        case true =>
          val user: String = SecureSocial.currentUser(request).get.identityId.userId
          val projects = getDeveloperProjects(user)
          projects.map { projects =>
            Ok(views.html.addcustomer(customerProjectForm, project, projects))
          }
        case false => Future(NotFound)
      }
    }
  }
  
  def showProjectLinksForm(project: String) = Restrict (Array("EMPLOYEE"), new AkvaarioDeadboltHandler ) {
    // TODO: Function needs overall refactoring. Remove get, etc.
    Action.async { implicit request =>
      projectExists(project) match{
        case true =>
          val user: String = SecureSocial.currentUser(request).get.identityId.userId
          val futures = for {
            projects <- getDeveloperProjects(user)
            // Find existing links
            links <- getLinks(project)
          }
          yield {
            links match {
              case None => {
                val filledForm = projectLinksForm.fill(ProjectLinks(List(""),List(""),project))
                Ok(views.html.editlinks(filledForm, project, projects))
              }
              case l: Some[JsObject] => {
                var labelBuffer = new ArrayBuffer[String]
                var urlBuffer = new ArrayBuffer[String]
                val linksList = (l.get \ "links").as[List[JsObject]]
                .foreach { obj =>
                  labelBuffer.append((obj \ "label").as[String])
                  urlBuffer.append((obj \ "url").as[String])
                }
                // Fill fields with existing links
                val filledForm = projectLinksForm.fill(ProjectLinks(labelBuffer.toList, urlBuffer.toList, project))
                Ok(views.html.editlinks(filledForm, project, projects))
              }
            }
          }
          futures // case true ends here. futures variable is the return variable
        case false => Future(NotFound)
      }
    }
  }

  def showContactInfoForm(project: String) = Restrict (Array("EMPLOYEE"), new AkvaarioDeadboltHandler ) {
    Action.async { implicit request =>
      projectExists(project) match{
        case true =>
          val user = SecureSocial.currentUser(request).get.identityId.userId
          val projects = getDeveloperProjects(user)
          projects.map { projects =>
            Ok(views.html.contactinfo(contactInfoForm, project, projects))
          }
        case false => Future(NotFound)
      }
    } 
  }

/*--------------------------------------------------------Formdata handling--------------------------------------------------------*/
  
  // Function for inserting the data from a project status form into the database
  def updateProjectStatus(project: String) = Restrict (Array("EMPLOYEE"), new AkvaarioDeadboltHandler ) {
    Action.async { implicit request =>
      projectExists(project) match{
        case true =>
          statusForm.bindFromRequest.fold(
          	errors => Future.successful(BadRequest),
          	data => {
              // If budget and/or hours fields left empty, set value to 0
              val budget: Int = data.spentBudget.getOrElse(0)
              val hours: Int = data.spentHours.getOrElse(0)
      	      val today = Calendar.getInstance().getTime()
      			  val obj = Json.obj(
                  "time" -> today,
                  "spentBudget" -> budget,
                  "budgetNotes" -> data.budgetNotes,
                  "spentHours" -> hours,
                  "hourNotes" -> data.hourNotes,
                  "workStatus" -> data.workStatus,
                  "holidays" -> data.holidays,
                  "project" -> data.project
      	      )
      	      insertProjectStatus(obj) map { lastError =>
      	        if (lastError.ok) {
      	          Redirect(routes.AdminController.showProjectStatusForm(project)).flashing("success" -> "Status updated successfully!")
      	        }
      	        else {
      	          InternalServerError(views.html.errorPage("500"))
      	        }
      	      }
            }
          )
        case false => Future(NotFound)
      }
    }
  }
  
  // Function for updating the links associated with the project
  def editProjectLinks(project: String) = Restrict (Array("EMPLOYEE"), new AkvaarioDeadboltHandler ) {
    // TODO: Function needs overall refactoring. Remove get, etc.
    Action.async { implicit request =>
      projectExists(project) match{
        case true =>
          projectLinksForm.bindFromRequest.fold(
            formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
            data => {
              var urlArray = data.urls.toBuffer
              var labelArray = data.labels.toBuffer
              // Create array of label & url JsObjects
              var linkArray = new ArrayBuffer[JsObject]
              for (i <- 0 until urlArray.length) {
                // If URL field is empty then don't save (labels can be empty)
                if (urlArray(i) != "") {
                  linkArray.append(Json.obj("label" -> labelArray(i), "url" -> urlArray(i)))
                }
              }
              updateLinks(project, linkArray.toList) map { lastError =>
                if (lastError.ok) {
                  // Redirect and flash success message
      	          Redirect(routes.AdminController.showProjectLinksForm(project)).flashing("success" -> "Links edited successfully!")
                }
                else {
                  InternalServerError(views.html.errorPage("500"))
                }
              }
            }
          )
        case false => Future(NotFound)
      }
    }
  }

  def editCustomerContactInfo(project: String)  = Restrict (Array("EMPLOYEE"), new AkvaarioDeadboltHandler ) {
    Action.async { implicit request =>
      projectExists(project) match{
        case true =>
          contactInfoForm.bindFromRequest.fold(
            formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
            data => {
                  val obj = Json.obj(
                  "name" -> data.name,
                  "title" -> data.title,
                  "email" -> data.email,
                  "phoneNumber" -> data.phoneNumber,
                  "project" -> data.project
              )
              // Update links, removing empty fields if necessary
              insertCustomerContactInfo(obj) map { lastError =>
                if (lastError.ok) {
                  // Redirect and flash success message
                  Redirect(routes.AdminController.showContactInfoForm(project)).flashing("success" -> "Customer contact information saved! Add another?")
                }
                else {
                  InternalServerError(views.html.errorPage("500"))
                }
              }
            }
          )
        case false => Future(NotFound)
      }
    }
  }
}