package security

// Security
import securesocial.core._

import play.api.libs.ws.WS
import play.api.mvc.{ AnyContent, SimpleResult, AsyncResult, Controller, Action, Request, Results }
import play.api.libs.openid.OpenID
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Thrown
import play.api.{ Application, Logger, Play }
import play.api.data.Form
import play.api.data.Forms._
import play.api.Play.current
import FuturiceOpenID._
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * A Futurice OpenID Provider
 * 
 * This is a custom OpenID implementation for securesocial as they currently do not
 * support OpenID. Newer versions of securesocial should support it, in which case this
 * can probably be removed in favor of their implementation. (2014-03-28)
 */

case class FutuLogin(username: String)

object FuturiceOpenIDController extends Controller {
  private val logger = play.api.Logger("security.FuturiceOpenIDController")

  val futuLogin = Form(mapping("username" -> text)(FutuLogin.apply)(FutuLogin.unapply))

  def prepareOpenID = Action.async { implicit request =>
    logger.debug("Sending OpenID request")

    // Make sure to use SSL in production
    val useSSL = Play.isProd
    
    futuLogin.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest),
      data => {
        
        OpenID.redirectURL("https://login.futurice.com/openid/"+data.username,
            securesocial.controllers.routes.ProviderController.authenticateByPost("futurice", None).absoluteURL(useSSL),
          Seq("email" -> "http://axschema.org/contact/email",
            "username" -> "http://axschema.org/namePerson/friendly",
            "firstname" -> "http://axschema.org/namePerson/first",
            "lastname" -> "http://axschema.org/namePerson/last"))
          .map(url => {
            logger.debug("OpenID redirect URL: " + url)
            Redirect(url)
          }).recover {
            case thrown =>
              logger.error(thrown.getMessage())
              logger.error(thrown.getStackTraceString)
              Redirect(securesocial.controllers.routes.LoginPage.login.absoluteURL())
          }
      })
  }
}

class FuturiceOpenID(application: Application) extends IdentityProvider(application) {
  private val logger = play.api.Logger("securesocial.core.FuturiceOpenIDProvider")
  override def id = FuturiceOpenID.Futurice

  override def authMethod = AuthenticationMethod.OpenId

  def doAuth()(implicit request: Request[AnyContent]): Either[SimpleResult, SocialUser] = {
    
    println(request.queryString)
    val openFuture = OpenID.verifiedId.map { info =>

      // These values should always be available, but better safe than sorry.
      val username = info.attributes.getOrElse("username.1", {
          logger.error("Username missing from openID response")
          throw new AuthenticationException()})
      
      val email = info.attributes.getOrElse("email.1", {
          logger.error("Email missing from openID response")
          throw new AuthenticationException()})
      
      val firstname = info.attributes.getOrElse("firstname.1", {
          logger.error("Firstname missing from openID response")
          throw new AuthenticationException()})
      
      val lastname = info.attributes.getOrElse("lastname.1", {
          logger.error("Lastname missing from openID response")
          throw new AuthenticationException()})

      logger.debug("OpenID user found: " + username)
      Right(SocialUser(IdentityId(username, FuturiceOpenID.Futurice), firstname, lastname, firstname + " " + lastname, Some(email), None, authMethod))

    } recover {
      // Something went wrong. This should be improved to handle different errors. (or wait for securesocial to implement openid)
      case thrown =>
        logger.error(thrown.getStackTraceString)
        throw new AuthenticationException()
    }
    Await.result(openFuture, 15 seconds)
  }

  override def fillProfile(user: SocialUser): SocialUser = {
    //TODO: This?
    user
  }
}

object FuturiceOpenID {
  val Api = "https://login.futurice.com/openid"
  val Futurice = "futurice"
}