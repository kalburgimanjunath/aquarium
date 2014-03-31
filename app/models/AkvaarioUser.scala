package models

//Security
import be.objectify.deadbolt.core.models._
import securesocial.core. { Identity, SecureSocial }
import securesocial.core.providers._

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import play.api.mvc.RequestHeader

// Roles to be used with Deadbolt for authorization
object Roles {
	val CUSTOMER = "CUSTOMER"
	val EMPLOYEE = "EMPLOYEE"
	val SERVER = "SERVER"
}

// Wrapper for roles
case class UserRole(role:String) extends Role {
	def getName: String = role
}

// Wrapper for user permissions
class UserPermission(permission: String) extends Permission{
	def getValue: String = permission
}

case class AkvaarioUser(id: String, roles: List[UserRole] = List(new UserRole(Roles.CUSTOMER)), projects: List[String] = List(), permissions: List[UserPermission] = List()) extends Subject{

	def getIdentifier: String = id

	def getRoles: java.util.List[UserRole] = roles.asJava

	def getPermissions: java.util.List[UserPermission] = permissions.asJava

	def getProjects: List[String] = projects
	
}

object AkvaarioUser {
  
  def getCurrentUserRole(request: RequestHeader): Option[UserRole] = {
	      SecureSocial.currentUser(request).map { suser =>
	        suser.identityId.providerId match {
            	case "futurice" => Some(UserRole(Roles.EMPLOYEE))
            	case _ => Some(UserRole(Roles.CUSTOMER))
	        }
	      } getOrElse(None)
  }
}
