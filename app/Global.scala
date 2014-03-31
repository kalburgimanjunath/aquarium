
import play.api._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.Play.current
import play.libs.Akka
import akka.actor._
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import models.{ FumDAO, fumClient, MailDAO }
import models.DAO._

//Error page
import play.api.mvc._
//import play.api.mvc.Results._
import scala.concurrent.Future

object Global extends GlobalSettings {
  
  /* Error Page */
  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(
      views.html.errorPage("404")
      //views.html.notFoundPage(request.path)
    ))
  }
  /* END Error page */


  val fumDAO = new FumDAO(fumClient)
  val mailDAO = new MailDAO()

  override def onStart(app: Application) {
    	scheduleDaemon(app)
  }
    
  def scheduleDaemon(app: Application) = {
   
   	val Running = "running"

    val logActor = Akka.system.actorOf(Props(new Actor {
    	def receive = {
    		case Running => Logger.info("Application still running")
    	}
    }))

    val PullUsers = "pullusers"
    val PullProjects = "pullprojects"
    val PullMail = "pullmail"
    val PullFuturiceUsers = "pullfuturiceusers"

    val fumActor = Akka.system.actorOf(Props(new Actor {
      def receive = {
        case PullUsers => {
          Logger.info("Trying to update FUM users.")
          fumDAO.getAllUsers() match {
            case Some(json) => {
              saveFUMItems(userDataCollection, json)
              Logger.info("Updated FUM users.")
            }
            case None => {
               Logger.info("No FUM users found.")
            }
          }
        }
        case PullProjects => {
          Logger.info("Trying to update FUM projects.")
          fumDAO.getAllProjects() match {
            case Some(json) => {
              saveFUMItems(projectDataCollection, json)
              Logger.info("Updated FUM projects.")
            }
            case None => {
               Logger.info("No FUM projects found.")
            }
          }
        }
        case PullFuturiceUsers => {
          Logger.info("Trying to update futurice-group users.")
          fumDAO.getAllFuturiceUsers() match {
            case Some(json) => {
            groupsDataCollection.update( Json.obj("name" -> json \ "name")  , Json.obj("name" -> json \ "name", "users" -> json \ "users" ), upsert = true)
              Logger.info("Updated futurice group.")
            }
            case None => {
               Logger.info("No futurice group found.")
            }
          }
        }
      }
    }))
    
    
    val mailActor = Akka.system.actorOf(Props(new Actor {
      def receive = {
        case PullMail => {
          Logger.info("Trying to connect to email server.")
          mailDAO.getAllUnreadMail match {
            case Array() => {
              Logger.info("No new emails found.")
            }
            case (result: Array[JsObject]) => {
              result.foreach { mail =>
                updateEmail(mail)
              }
              // TODO: Error handling if mail is not JsObject? Is it necessary?
              Logger.info("Unread emails saved to database and marked as SEEN.")
            }
          }
        }
      }
    }))
    
     Akka.system.scheduler.schedule(0 seconds, 10 hours, logActor, Running)
     
     // Only run updates when actually in production.
      Play.current.configuration.getString("application.env").getOrElse("") match {
   	  case "prod" =>
   	    Logger.info("Running in production, scheduling updates.")
	    Akka.system.scheduler.schedule(0 seconds, 6 hours, fumActor, PullUsers)
	    Akka.system.scheduler.schedule(15 seconds, 12 hours, fumActor, PullFuturiceUsers)
	    Akka.system.scheduler.schedule(30 seconds, 6 hours, fumActor, PullProjects)
	    Akka.system.scheduler.schedule(0 seconds, 10 minutes, mailActor, PullMail)
   	  case _ =>
   	    Logger.info("Not running in production, so not scheduling any updated.")
   	}
    
  
  }
}