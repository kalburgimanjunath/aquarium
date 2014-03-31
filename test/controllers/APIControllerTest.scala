package controllers

import play.api.test._
import play.api.libs.json._
import concurrent.duration.Duration
import securesocial.core._
import play.api.test.{FakeApplication, WithApplication}
import org.specs2.mock.Mockito
import securesocial.core.Identity
import securesocial.testkit.WithLoggedUser
import play.api.mvc.{Request, AnyContent}
import play.api.libs.iteratee.Enumerator
import securesocial.testkit.WithLoggedUser
import org.specs2.matcher.ShouldMatchers
import play.api.http.HeaderNames
import play.api.mvc.Cookie
import securesocial.core.{SocialUser, AuthenticationMethod, IdentityId}
import securesocial.testkit.SocialUserGenerator



class APIControllerSpec extends PlaySpecification with ShouldMatchers {
  //sequential //makes test non-paralell so that scoverage SCCT works.
  import WithLoggedUser._
  def minimalApp = FakeApplication(withoutPlugins = excludedPlugins, additionalPlugins = includedPlugins)
  def getLoggedRequest(cookie: Cookie): Request[AnyContent] = FakeRequest().
    withHeaders((HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")).
    withCookies(cookie)
  def identity(username: String, provider: String): Some[Identity] = {
    val identityId = IdentityId(username, provider)
    Some(SocialUserGenerator.socialUser(identityId))
  }

  "APIController" should {
      "load fixtures correctly" in new WithLoggedUser(minimalApp, identity("akvaariotest", "userpass")) {
            val resultP = controllers.APIController.project("PFuturiceDashboard")(getLoggedRequest(cookie))
            val resultE = controllers.APIController.getProjectEmails("PFuturiceDashboard")(getLoggedRequest(cookie))
            status(resultP) must equalTo(OK)
            status(resultE) must equalTo(OK)
            contentAsString(resultP) must contain("users")
            contentAsString(resultP) must contain("project")
      }

    "give 403 on project without login" in new WithApplication {

      val result = controllers.APIController.project("PFuturiceDashboard")(FakeRequest())

      status(result) must equalTo(403)
    }

    "access project api with login " in new WithLoggedUser(minimalApp, identity("akvaariotest", "userpass")) {

      val result = controllers.APIController.project("PFuturiceDashboard")(getLoggedRequest(cookie))

      val actual: Int = status(result)
      actual must be equalTo OK
      contentType(result) must beSome("application/json")
      contentAsString(result) must contain("Köhler")
      contentAsString(result) must contain("Dolenc")
      contentAsString(result) must contain("Ehnström")
      contentAsString(result) must contain("PFuturiceDashboard")
    }
    
    "not access project without authorization " in new WithLoggedUser(minimalApp, identity("scampitest", "userpass")) {

      val result = controllers.APIController.project("PFuturiceDashboard")(getLoggedRequest(cookie))

      val actual: Int = status(result)
      actual must be equalTo 403 // TODO
    }

    "send 403 on a non-existing project lookup" in new WithApplication {

      val result = controllers.APIController.project("ThisProjectDoesNotExist")(FakeRequest())

      status(result) must equalTo(403)
    }
    "give 404 when user has rights to non-existing project" in new WithLoggedUser(minimalApp, identity("projectlesstest", "userpass")){
      //test is based on user that does not exist in fixtures, only a permission exists in customerProjects
      val result = controllers.APIController.project("NoProject")(getLoggedRequest(cookie))

      status(result) must equalTo(404)
    }

    "send 403 on a non-existing project lookup with login" in new WithLoggedUser(minimalApp, identity("akvaariotest", "userpass")) {

      val result = controllers.APIController.project("ThisProjectDoesNotExist")(getLoggedRequest(cookie))

      status(result) must equalTo(403)
    }

    "give 403 on forbidden email" in new WithApplication {

      val result = controllers.APIController.getProjectEmails("PFuturiceDashboard")(FakeRequest())

      status(result) must equalTo(403)
    }

    "retrieve project emails logged in" in new WithLoggedUser(minimalApp, identity("akvaariotest", "userpass")) {

      val result = controllers.APIController.getProjectEmails("PFuturiceDashboard")(getLoggedRequest(cookie))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must contain("pfuturicedashboard")
    }

    "give user project upon request to customer" in new WithLoggedUser(minimalApp, identity("akvaariotest", "userpass")){

      val result = controllers.APIController.getUserProject()(getLoggedRequest(cookie))

      status(result) must equalTo(OK)
      contentAsString(result) must contain("PFuturiceDashboard")
    }

    "give user project upon request to employee" in new WithLoggedUser(minimalApp, identity("employee", "futurice")){

      val result = controllers.APIController.getUserProject()(getLoggedRequest(cookie))

      status(result) must equalTo(OK)
      contentAsString(result) must contain("PAaltoScampi")
    }

    "deny user project without login" in new WithApplication {

      val result = controllers.APIController.getUserProject()(FakeRequest())
      status(result) must equalTo(403)
    }

    "error correctly when user has no project" in new WithLoggedUser(minimalApp, identity("projectlessuser", "userpass")){
      //Test is outdated. getUserProject no longer errors on no projects but rather gives an empty list.
      val result = controllers.APIController.getUserProject()(getLoggedRequest(cookie))

      status(result) must equalTo(200) 

    }
    
    "give project status form upon request for akvaariotest" in new WithLoggedUser(minimalApp, identity("akvaariotest", "userpass")) {
      val result = controllers.APIController.getStatusForms("PFuturiceDashboard")(getLoggedRequest(cookie))
      status(result) must equalTo(200)
      contentType(result) must beSome("application/json")
      contentAsString(result) must contain ("Code monkeys with holidays...? You must be kidding me")
      contentAsString(result) must contain ("Nothing remarkable")
    }

    "give project status form upon request for scampitest" in new WithLoggedUser(minimalApp, identity("scampitest", "userpass")) {
      val result = controllers.APIController.getStatusForms("PAaltoScampi")(getLoggedRequest(cookie))
      status(result) must equalTo(200)
      contentType(result) must beSome("application/json")
      contentAsString(result) must contain ("0")
      contentAsString(result) must contain ("Project has not yet")
    }

    "give 403 on status form upon use not having permissions for project" in new WithLoggedUser(minimalApp, identity("projectlessuser", "userpass")) {
      val result = controllers.APIController.getStatusForms("PFuturiceDashboard")(getLoggedRequest(cookie))
      status(result) must equalTo(403)
    }
    "give 403 on status form without login" in new WithApplication {
      val result = controllers.APIController.getStatusForms("PFuturiceDashboard")(FakeRequest())
      status(result) must equalTo(403)
    }
    //links test
    "give project links form upon request" in new WithLoggedUser(minimalApp, identity("akvaariotest", "userpass")) {
      val result = controllers.APIController.getLinksForm("PFuturiceDashboard")(getLoggedRequest(cookie))
      status(result) must equalTo(200)
      contentType(result) must beSome("application/json")
    }

    "give 403 on project links upon use not having permissions for project" in new WithLoggedUser(minimalApp, identity("projectlessuser", "userpass")) {
      val result = controllers.APIController.getLinksForm("PFuturiceDashboard")(getLoggedRequest(cookie))
      status(result) must equalTo(403)
    }
    "give 403 on project links without login" in new WithApplication {
      val result = controllers.APIController.getLinksForm("PFuturiceDashboard")(FakeRequest())
      status(result) must equalTo(403)
    }

    // Customer contact information tests
    "give project customer contact information upon request" in new WithLoggedUser(minimalApp, identity("akvaariotest", "userpass")) {
      val result = controllers.APIController.getCustomerContactInfoForm("PFuturiceDashboard")(getLoggedRequest(cookie))
      status(result) must equalTo(200)
      contentType(result) must beSome("application/json")
    }

    "give 403 on project customer contact information upon use not having permissions for project" in new WithLoggedUser(minimalApp, identity("projectlessuser", "userpass")) {
      val result = controllers.APIController.getCustomerContactInfoForm("PFuturiceDashboard")(getLoggedRequest(cookie))
      status(result) must equalTo(403)
    }
    "give 403 on project customer contact information without login" in new WithApplication {
      val result = controllers.APIController.getCustomerContactInfoForm("PFuturiceDashboard")(FakeRequest())
      status(result) must equalTo(403)
    }


    "restrict user role without permission " in new WithApplication {
      val result = controllers.APIController.getUserRole()(FakeRequest())
    }
    "show correctly user role" in new WithLoggedUser(minimalApp, identity("akvaariotest", "userpass")) {
      val result = controllers.APIController.getUserRole()(getLoggedRequest(cookie))
      status(result) must equalTo(200)
      contentAsString(result) must contain ("CUSTOMER")
    }
    "show correctly user role" in new WithLoggedUser(minimalApp, identity("devtest", "futurice")) {
      val result = controllers.APIController.getUserRole()(getLoggedRequest(cookie))
      status(result) must equalTo(200)
      contentAsString(result) must contain ("EMPLOYEE")
    }


  }
  
   "APIController (developer) " should {
      
     "access all projects when logged in" in new WithLoggedUser(minimalApp, identity("devtest", "futurice")) {
          val result1 = controllers.APIController.project("PFuturiceDashboard")(getLoggedRequest(cookie))
          status(result1) must equalTo(OK)
          contentAsString(result1) must contain("PFuturiceDashboard")
          contentAsString(result1) must contain("devtest")
          
          val result2 = controllers.APIController.project("PAaltoScampi")(getLoggedRequest(cookie))
          status(result2) must equalTo(OK)
          contentAsString(result2) must contain("PAaltoScampi")
          contentAsString(result2) must not contain("devtest")
      }
     
     "access no projects when not logged in" in new WithApplication {
          val result1 = controllers.APIController.project("PFuturiceDashboard")(FakeRequest())
          status(result1) must equalTo(403)
          
          val result2 = controllers.APIController.project("PAaltoScampi")(FakeRequest())
          status(result2) must equalTo(403)
      }
      "be able to add customer projects" in new WithLoggedUser(minimalApp, identity("devtest", "futurice")) {
          val request1 = FakeRequest().withHeaders((HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")).withCookies(cookie).
            withFormUrlEncodedBody(
            "email" -> "testing@testcase.com",
            "project" -> "NoProject"
          )
          val result1 = controllers.APIController.addCustomerProject()(request1)
          status(result1) must equalTo(200)
      }
      "be denied customer projects when not logged int" in new WithApplication {
          val result1 = controllers.APIController.addCustomerProject()(FakeRequest())
          status(result1) must equalTo(403)
      }
      "give badrequest when trying to get addCustomerProject" in new WithLoggedUser(minimalApp, identity("devtest", "futurice")) {
          val result1 = controllers.APIController.addCustomerProject()(getLoggedRequest(cookie))
          status(result1) must equalTo(400)
      }
   }
}

 
