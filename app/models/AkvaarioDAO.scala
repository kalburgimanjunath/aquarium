package models

import play.api._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin.db
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.language.postfixOps
import reactivemongo.core.commands.LastError


object DAO extends Controller with MongoController {

/*-----------------------------------Collections-----------------------------------------------------*/

	def userDataCollection: JSONCollection = db.collection[JSONCollection]("userData")
	def projectDataCollection: JSONCollection = db.collection[JSONCollection]("projectData")
	def emailDataCollection: JSONCollection = db.collection[JSONCollection]("emailData")
	def groupsDataCollection: JSONCollection = db.collection[JSONCollection]("groupsData")
	def customerProjects: JSONCollection = db.collection[JSONCollection]("customerProjects")
	def customers: JSONCollection = db.collection[JSONCollection]("customers")
	def tokens: JSONCollection = db.collection[JSONCollection]("tokens")
	def projectStatusCollection: JSONCollection = db.collection[JSONCollection]("projectStatus")
	def projectLinksCollection: JSONCollection = db.collection[JSONCollection]("projectLinks")
 	def customerContactInformationCollection : JSONCollection = db.collection[JSONCollection]("customerContactInformation")

/*-----------------------------------Query methods-----------------------------------------------------*/

	def getProject(project: String) : Future[Option[JsObject]] = {
		val query = Json.obj("name" -> project)
      	projectDataCollection.find(query).one[JsObject]
	}

	def getUser(user: String) : Future[Option[JsObject]] = {
		val query = Json.obj("username" -> user)
      	userDataCollection.find(query).one[JsObject]
    }
	
	def getFutuUserProjects(username: String) : Future[List[String]] = {
		val query = Json.obj("users" -> username)
      	projectDataCollection.find(query).cursor[JsObject].collect[List]().map{ list =>
      		list.map(name => (name \ "name").asOpt[String].getOrElse("Error: project name not found") )
		}
	}

    def getCustomerProjects(user: String) : Future[List[String]] = {
    	val query = Json.obj("email" -> user)
        customerProjects.find(query).cursor[JsObject].collect[List]().map{ list =>
      		list.map(name => (name \ "project").asOpt[String].getOrElse("Error: project name not found") )
		}
    }
    
    def getEmails(project: String) : Future[List[JsObject]] = {
    	val query = Json.obj("project" -> project.toLowerCase)
    	emailDataCollection.find(query).cursor[JsObject].collect[List]()
    }

    def getDeveloperProjects(username: String) : Future[List[JsObject]] = {
    	val query = Json.obj("users" -> username)
    	projectDataCollection.find(query).cursor[JsObject].collect[List]()
    }

    def getLinks(project: String) : Future[Option[JsObject]] = {
    	val query = Json.obj("project" -> project)
    	projectLinksCollection.find(query).one[JsObject]
    }

    def getStatusForm(project: String) : Future[List[JsObject]] = {
    	val query = Json.obj("project" -> project)
    	projectStatusCollection.find(query).cursor[JsObject].collect[List]()
    }

    def getCustomerContactInformation(project: String) : Future[List[JsObject]] = {
    	val query = Json.obj("project" -> project)
    	customerContactInformationCollection.find(query).cursor[JsObject].collect[List]()
    }
    
    def getGroup(name: String) : Future[List[String]] = {
    	val query = Json.obj("name" -> name)
    	groupsDataCollection.find(query).one[JsObject] map{ group =>
    		(group.get \ "users").as[List[String]]
    	}
    }
    def projectExists(project: String) : Boolean = {
        val query = Json.obj("name" -> project)
        Await.result(projectDataCollection.find(query).one[JsObject], 5 seconds) match {
            case Some(_) => true
            case None => false
        }
    }

  	def getCustomer(userId : String, providerId: String) : Future[Option[JsObject]] = {
		val query = Json.obj("userid" -> userId, "provider" -> providerId)
		customers.find(query).one[JsObject]
	}

	def getToken(token : String) : Future[Option[JsObject]] = {
		val query = Json.obj("uuid" -> token)
		tokens.find(Json.obj("uuid" -> token)).one[JsObject]
	}

	def getAllTokens() : Future[List[JsObject]] = {
		val query = Json.obj()
		tokens.find(query).cursor[JsObject].collect[List]()
	}


/*-----------------------------------Write methods-----------------------------------------------------*/

    def insertCustomerProject(email: String, project: String) : Future[LastError]= {
        var obj = Json.obj("email" -> email, "project" -> project)
        customerProjects.update(obj, obj, upsert = true)
        
        // Code below if we want to store single customer + array of projects, i.e. db entries would be {email: x, projects: [{project: y}, {project: z}]}
        // val query = Json.obj("email" -> email)
        // val obj = Json.obj("$addToSet" -> 
        //  Json.obj("projects" -> Json.obj("project" -> project))
        // )
        // customerProjects.update(query, obj, upsert = true)
    }

	def saveFUMItems(itemsCollection: JSONCollection, itemsJson: JsValue) {

		(__ \ "results")(itemsJson) match {
		  case results :: rest => {
		    results match {
		      case JsArray(elements) =>
		        elements.foreach(user => itemsCollection.update( Json.obj("id" -> user \ "id") ,  user , upsert = true))
		      case _ => Logger.info("No items recieved from FUM")
		    }
		  }
		  case other => Logger.info("Nothing recieved from FUM")
		}
	}

	def updateEmail(mail: JsObject) = {
		emailDataCollection.update(Json.obj("receivedDate" -> (mail \ "receivedDate") ), mail , upsert = true)
	}

	def insertProjectStatus(status: JsObject) : Future[LastError] = {
		projectStatusCollection.insert(status)
	}

    def updateLinks(project: String, links: List[JsObject]) : Future[LastError] = {
        val query = Json.obj("project" -> project)
        val obj = Json.obj("links" -> links, "project" -> project)
        projectLinksCollection.update(query, obj, upsert = true)
    }

    def insertCustomerContactInfo(info: JsObject) : Future[LastError] = {
        customerContactInformationCollection.insert(info)
    }

	def updateCustomer(userId : String, customerJson : JsObject) = {
		val query = Json.obj("userid" -> userId)
		customers.update(query, customerJson, upsert = true)
	}


	def saveToken(tokenJson : JsObject) = {
		tokens.save(tokenJson)
	}

/*-----------------------------------Remove methods-----------------------------------------------------*/

	def removeToken(uuid : String) = {
		def query = Json.obj("uuid" -> uuid)
		tokens.remove(query)
	}
}