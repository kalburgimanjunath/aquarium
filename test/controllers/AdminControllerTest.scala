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



class AdminControllerSpec extends PlaySpecification with ShouldMatchers {
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

  "AdminController" should {

    "deny access to admin controllers for non-employees" in new WithLoggedUser(minimalApp, identity("akvaariotest", "userpass")) {
        status(controllers.AdminController.showProjectStatusForm("PFuturiceDashboard")(getLoggedRequest(cookie))) must equalTo(403)
        status(controllers.AdminController.showAddCustomerForm("PFuturiceDashboard")(getLoggedRequest(cookie))) must equalTo(403)
        status(controllers.AdminController.showProjectLinksForm("PFuturiceDashboard")(getLoggedRequest(cookie))) must equalTo(403)
        status(controllers.AdminController.showContactInfoForm("PFuturiceDashboard")(getLoggedRequest(cookie))) must equalTo(403)
/* ------------------------------------------------------------▲Get/Set▼-----------------------------------------------------------*/
        status(controllers.AdminController.updateProjectStatus("PFuturiceDashboard")(getLoggedRequest(cookie))) must equalTo(403)
        status(controllers.AdminController.editProjectLinks("PFuturiceDashboard")(getLoggedRequest(cookie))) must equalTo(403)
        status(controllers.AdminController.editCustomerContactInfo("PFuturiceDashboard")(getLoggedRequest(cookie))) must equalTo(403)

      }

    "acces admin controllers correctly for employee" in new WithLoggedUser(minimalApp, identity("employee", "futurice")) {
        val result1 = controllers.AdminController.showProjectStatusForm("PFuturiceDashboard")(getLoggedRequest(cookie))
        status(result1) must equalTo(OK)
        contentAsString(result1) must contain("PFuturiceDashboard")

        val result2 = controllers.AdminController.showAddCustomerForm("PAaltoScampi")(getLoggedRequest(cookie))
        status(result2) must equalTo(OK)
        contentAsString(result2) must contain("PAaltoScampi")

        val result3 = controllers.AdminController.showProjectLinksForm("PFuturiceDashboard")(getLoggedRequest(cookie))
        status(result3) must equalTo(OK)
        contentAsString(result3) must contain("PFuturiceDashboard")
        val result35 = controllers.AdminController.showProjectLinksForm("TestProject")(getLoggedRequest(cookie))
        status(result35) must equalTo(OK)

        val result4 = controllers.AdminController.showContactInfoForm("PAaltoScampi")(getLoggedRequest(cookie))
        status(result4) must equalTo(OK)
        contentAsString(result4) must contain("PAaltoScampi")

/* ------------------------------------------------------------▲Get/Set▼-----------------------------------------------------------*/


        val request1 = FakeRequest().withHeaders((HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")).withCookies(cookie).
          withFormUrlEncodedBody(
          "timestamp" -> "300",
          "spentBudget" -> "500",
          "budgetNotes" -> "data.budgetNotes",
          "spentHours" -> "200",
          "hourNotes" -> "data.hourNotes",
          "workStatus" -> "workStatus",
          "holidays" -> "holidays",
          "project" -> "PFuturiceDashboard"
        )
        val result5 = controllers.AdminController.updateProjectStatus("PFuturiceDashboard")(request1)
        status(result5) must equalTo(303)


        val request2 = FakeRequest().withHeaders((HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")).withCookies(cookie).
          withFormUrlEncodedBody(
            "labels[0]" -> "first",
            "urls[0]" ->  "www.first.com",
            "labels[1]" -> "",
            "urls[1]" ->  "www.NoLabel.com",
            "labels[2]" -> "NoUrl",
            "urls[2]" ->  "",
            "project" -> "PAaltoScampi"
        )        

        val result6 = controllers.AdminController.editProjectLinks("PAaltoScampi")(request2)
        status(result6) must equalTo(303)


        val request3 = FakeRequest().withHeaders((HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")).withCookies(cookie).
          withFormUrlEncodedBody(
            "name" -> "Mister VIP",
            "title" -> "VIP",
            "email" -> "data.email",
            "phoneNumber" -> "data.phoneNumber",
            "project" -> "PFuturiceDashboard"
        ) 

        val result7 = controllers.AdminController.editCustomerContactInfo("PFuturiceDashboard")(request3)
        status(result6) must equalTo(303)


    }

    "give bad request when post methods are invoked by get" in new WithLoggedUser(minimalApp, identity("employee", "futurice")) {
        val result1 = controllers.AdminController.updateProjectStatus("PFuturiceDashboard")(getLoggedRequest(cookie))
        status(result1) must equalTo(400)

        val result2 = controllers.AdminController.editProjectLinks("PFuturiceDashboard")(getLoggedRequest(cookie))
        status(result2) must equalTo(400)

        val result3 = controllers.AdminController.editCustomerContactInfo("PFuturiceDashboard")(getLoggedRequest(cookie))
        status(result3) must equalTo(400)
    }

    "give bad request with broken posts" in new WithLoggedUser(minimalApp, identity("employee", "futurice")) {
        val request1 = FakeRequest().withHeaders((HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")).withCookies(cookie).
          withFormUrlEncodedBody(
          "timestamp" -> "300",
          "spentBudget" -> "500",
          "budgetNotes" -> "data.budgetNotes",
          "spentHours" -> "200",
          "hourNotes" -> "data.hourNotes",
          "workStatus" -> "data.workStatus",
          "holidays" -> "data.holidays"
        )
        //give in project status form to project links
        val result1 = controllers.AdminController.editProjectLinks("PFuturiceDashboard")(request1)
        status(result1) must equalTo(400)


        val request2 = FakeRequest().withHeaders((HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")).withCookies(cookie).
          withFormUrlEncodedBody(
          "links" -> "[]",
          "project" -> "PAaltoScampi"
        )
        //give in project links form to customer contact
        val result2 = controllers.AdminController.editCustomerContactInfo("PFuturiceDashboard")(request2)
        status(result2) must equalTo(400)


        val request3 = FakeRequest().withHeaders((HeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded")).withCookies(cookie).
          withFormUrlEncodedBody(
            "name" -> "Mister VIP",
            "title" -> "VIP",
            "email" -> "data.email",
            "phoneNumber" -> "data.phoneNumber",
            "project" -> "PFuturiceDashboard"
        )
        //give customer contact to project status
        val result3 = controllers.AdminController.updateProjectStatus("PFuturiceDashboard")(request3)
        status(result3) must equalTo(400)

    }



    "error correctly when employee tries to access non-existant project" in new WithLoggedUser(minimalApp, identity("employee", "futurice")) {
        status(controllers.AdminController.showProjectStatusForm("ThisProjectDoesNotExist")(getLoggedRequest(cookie))) must equalTo(404)
        status(controllers.AdminController.showAddCustomerForm("ThisProjectDoesNotExist")(getLoggedRequest(cookie))) must equalTo(404)
        status(controllers.AdminController.showProjectLinksForm("ThisProjectDoesNotExist")(getLoggedRequest(cookie))) must equalTo(404)
        status(controllers.AdminController.showContactInfoForm("ThisProjectDoesNotExist")(getLoggedRequest(cookie))) must equalTo(404)
/* ------------------------------------------------------------▲Get/Set▼-----------------------------------------------------------*/
        status(controllers.AdminController.updateProjectStatus("ThisProjectDoesNotExist")(getLoggedRequest(cookie))) must equalTo(404)
        status(controllers.AdminController.editProjectLinks("ThisProjectDoesNotExist")(getLoggedRequest(cookie))) must equalTo(404)
        status(controllers.AdminController.editCustomerContactInfo("ThisProjectDoesNotExist")(getLoggedRequest(cookie))) must equalTo(404)
    }


  }
}

 
