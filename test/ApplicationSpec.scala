import controllers.Application
import org.specs2.matcher.ShouldMatchers
import play.api.http.HeaderNames
import play.api.mvc.{Request, AnyContent}
import play.api.test.{PlaySpecification, FakeApplication, FakeRequest, WithApplication}
import securesocial.testkit.WithLoggedUser
import play.api.mvc.Cookie
import securesocial.core.{SocialUser, AuthenticationMethod, IdentityId, Identity}
import securesocial.testkit.SocialUserGenerator

class ApplicationSpec extends PlaySpecification with ShouldMatchers {
  import WithLoggedUser._
  def minimalApp = FakeApplication(withoutPlugins=excludedPlugins,additionalPlugins = includedPlugins)
  def getLoggedRequest(cookie: Cookie): Request[AnyContent] = FakeRequest().
    withHeaders((HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")).
    withCookies(cookie)
  def identity(username: String): Some[Identity] = {
    val identityId = IdentityId(username, "userpass")
    Some(SocialUserGenerator.socialUser(identityId))
  }
  
 
  "Access secured index " in new WithLoggedUser(minimalApp, identity("akvaariotest")) {
    val result = Application.index.apply(getLoggedRequest(cookie))
    
    val actual: Int= status(result)
    actual must be equalTo OK
  }

  "Redirect from secured index if user is not logged in" in new WithApplication {
    val result = Application.index(FakeRequest())
    status(result) must equalTo(303)

  }

  "Wildcard should function correctly" in new WithApplication{
    val result = Application.find("/notfoundordoesnotexist")(FakeRequest())
    status(result) must equalTo(303)
  }

  "Login function shall work correctly when user is logged in" in new WithLoggedUser(minimalApp, identity("akvaariotest")) {
    val result = Application.login(getLoggedRequest(cookie))
    status(result) must equalTo(OK)
  }
}
