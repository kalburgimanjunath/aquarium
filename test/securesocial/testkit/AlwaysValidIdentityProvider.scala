package securesocial.testkit

import play.api.Logger
import securesocial.core._
import play.api.mvc.{Result, Request}
import securesocial.core.IdentityId
import play.api.mvc.SimpleResult

class AlwaysValidIdentityProvider(app:play.api.Application) extends IdentityProvider(app){
  val logger = Logger("securesocial.stubs.AlwaysValidIdentityProvider")
  def authMethod: AuthenticationMethod = AuthenticationMethod("naive")


  override def doAuth()(implicit request: Request[play.api.mvc.AnyContent]): Either[SimpleResult, SocialUser] ={
    val userId = request.body.asText.get
    val r =Right(SocialUserGenerator.socialUserGen(IdentityId(userId, id), authMethod).sample.get)
    r
  }


  def fillProfile(user: SocialUser): SocialUser = {
    user
  }

  def id: String = "naive"
}