//Code from http://shrikar.com/blog/2013/10/26/playframework-securesocial-and-mongodb/

package services

// Secure social
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId

// Time imports
import _root_.java.util.Date
import org.joda.time.{DateTime, LocalTime}
import org.joda.time.format.{ DateTimeFormatter, DateTimeFormat }

import play.api.{ Logger, Application }
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent. { Future, Await }
import scala.concurrent.duration._
import reactivemongo.core.commands.GetLastError
import scala.util.parsing.json.JSONObject
import scala.language.postfixOps

import models.DAO._

case class MongoUserService(application: Application) extends UserServicePlugin(application) {
  val outPutUser = (__ \ "id").json.prune

  def retIdentity(json: JsObject, provider: String): Identity = {
    
    var userid: String = ""
    var firstname: String = ""
    var lastname: String = ""
    var email: String = ""
    var avatar: String = ""
    var hash: String = ""
    var password: String = ""
    var authmethod: String = ""

    provider match {

      case "futurice" =>
        userid = (json \ "username").as[String]
        firstname = (json \ "first_name").as[String]
        lastname = (json \ "last_name").as[String]
        email = (json \ "email").as[String]
        avatar = (json \ "portrait_thumb_url").as[String]
        hash = ""
        password = ""
        authmethod = "openId"

      case _ =>
        userid = (json \ "userid").as[String]
        firstname = (json \ "firstname").as[String]
        lastname = (json \ "lastname").as[String]
        email = (json \ "email").as[String]
        avatar = (json \ "avatar").as[String]
        hash = (json \ "password" \ "hasher").as[String]
        password = (json \ "password" \ "password").as[String]
        authmethod = (json \ "authmethod").as[String]
    }

    val identity: IdentityId = new IdentityId(userid, provider)
    val authMethod: AuthenticationMethod = new AuthenticationMethod(authmethod)
    val pwdInfo: PasswordInfo = new PasswordInfo(hash, password)
    val user: SocialUser = new SocialUser(identity, firstname, lastname, firstname, Some(email), Some(avatar), authMethod, None, None, Some(pwdInfo))
    user
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    
    var futureUser = Future(Option(Json.obj()))
    providerId match {
      case "futurice" => 
        futureUser = getUser(email).map {
          case Some(user) => Some(user)
          case None => None
        }

      case _ =>
        futureUser = getCustomer(email, providerId).map {
          case Some(user) => Some(user)
          case None => None
        }
    }

    val option = Await.result(futureUser, 5 seconds)
    option.map { jobj =>
      Some(retIdentity(jobj.asInstanceOf[JsObject], providerId))
    }.getOrElse {
      None
    }
  }

  // Save user info on login
  def save(user: Identity): Identity = {

    if (!user.identityId.providerId.equals("userpass")) {
     return user
    }

    val email = user.email match {
      case Some(email) => email
      case _ => "N/A"
    }

    val avatar = user.avatarUrl match {
      case Some(url) => url
      case _ => "N/A"
    }
    
    val password = user.passwordInfo match {
      case Some(p) => Json.obj("hasher" -> user.passwordInfo.get.hasher, "password" -> user.passwordInfo.get.password, "salt" -> user.passwordInfo.get.salt)
      case _ => Json.obj("hasher" -> "", "password" -> "", "salt" -> "")
    }

    getCustomer(user.identityId.userId, user.identityId.providerId).map { option =>

      option.map { customer =>
        val customerJson = Json.obj(
          "userid" -> (customer \ "userid"),
          "provider" -> (customer \ "provider"),
          "firstname" -> (customer \ "firstname"),
          "lastname" -> (customer \ "lastname"),
          "email" -> (customer \ "email"),
          "avatar" -> (customer \ "avatar"),
          "authmethod" -> (customer \ "authmethod"),
          "password" -> (customer \ "password"),
          "created_at" -> (customer \ "created_at"),
          "updated_at" -> Json.obj("$date" -> new Date()))
        updateCustomer(user.identityId.userId, customerJson)
      }.getOrElse {
        val customerJson = Json.obj(
          "userid" -> user.identityId.userId,
          "provider" -> user.identityId.providerId,
          "firstname" -> user.firstName,
          "lastname" -> user.lastName,
          "email" -> email,
          "avatar" -> avatar,
          "authmethod" -> user.authMethod.method,
          "password" -> password,
          "created_at" -> Json.obj("$date" -> new Date()),
          "updated_at" -> Json.obj("$date" -> new Date()))
        updateCustomer(user.identityId.userId, customerJson)
      }
    }
    user
  }

  def find(id: IdentityId): Option[Identity] = {
    findByEmailAndProvider(id.userId, id.providerId)
  }

  def save(token: Token) {
    val tokenJson = Json.obj(
      "uuid" -> token.uuid,
      "email" -> token.email,
      "creation_time" -> Json.obj("$date" -> token.creationTime),
      "expiration_time" -> Json.obj("$date" -> token.expirationTime),
      "isSignUp" -> token.isSignUp)
    saveToken(tokenJson)
  }

  def findToken(token: String): Option[Token] = {
    val futureToken = getToken(token).map {
      case Some(token) => token
      case None => false
    }
    val jobj = Await.result(futureToken, 5 seconds)
    jobj match {
      case x: Boolean => None
      case obj: JsObject => {
        println(obj)
        val uuid = (obj \ "uuid").as[String]
        val email = (obj \ "email").as[String]
        val created = (obj \ "creation_time" \ "$date").as[Long]
        val expire = (obj \ "expiration_time" \ "$date").as[Long]
        val signup = (obj \ "isSignUp").as[Boolean]
        val df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        Some(new Token(uuid, email, new DateTime(created), new DateTime(expire), signup))
      }
    }
  }

  def deleteToken(uuid: String) {
    removeToken(uuid)
  }

  def deleteExpiredTokens() {
    Logger.info("[MongoUserService] - Checking for expired tokens")
    getAllTokens().map { tokens =>
      for (token <- tokens) {
        val uuid = (token \ "uuid").as[String]
        val expire = new DateTime((token \ "expiration_time" \ "$date").as[Long])
        val df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        if (df.print(DateTime.now) > df.print(expire))
        {
          deleteToken(uuid)
        }
      }
    }
  }

  def link(current: Identity, to: Identity) = {
    
  }
}

