package controllers.plugin

import play.api.mvc.{AnyContent, Controller, RequestHeader, Request}
import play.api.templates.{Html, Txt}
import play.api.{Logger, Plugin, Application}
import securesocial.core.{Identity, SecuredRequest}
import play.api.data.Form
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.controllers.TemplatesPlugin

//imported to handle token->email extraction
import models.DAO.getToken 
import play.api.libs.json._ 
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class MyViews(application: play.Application) extends TemplatesPlugin 
{
 /**
   * Returns the html for the login page
   * @param request
   * @tparam A
   * @return
   */

  override def getLoginPage(form: Form[(String, String)],
                               msg: Option[String] = None)(implicit request: Request[AnyContent]): Html =
  {
    views.html.login(form, msg)
  }

  /**
   * Returns the html for the signup page
   * 
   * @param request
   * @tparam A
   * @return
   */
  override def getSignUpPage(form: Form[RegistrationInfo], token: String)(implicit request: Request[AnyContent]): Html = {
    views.html.signUp(form, token)
  }

  /**
   * Returns the html for the start signup page
   *
   * @param request
   * @tparam A
   * @return
   */
  override def getStartSignUpPage(form: Form[String])(implicit request: Request[AnyContent]): Html = {
    securesocial.views.html.Registration.startSignUp(form)
  }

  override def getStartResetPasswordPage(form: Form[String])(implicit request: Request[AnyContent]): Html = {
    securesocial.views.html.Registration.startResetPassword(form)
  }

  override def getResetPasswordPage(form: Form[(String, String)], token: String)(implicit request: Request[AnyContent]): Html = {
    securesocial.views.html.Registration.resetPasswordPage(form, token)
  }

  override def getPasswordChangePage(form: Form[ChangeInfo])(implicit request: SecuredRequest[AnyContent]):Html = {
    securesocial.views.html.passwordChange(form)
  }

  def getNotAuthorizedPage(implicit request: Request[AnyContent]): Html = {
    securesocial.views.html.notAuthorized()
  }

  def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    //fetches the email based on token UUID from DB
    val email: String = (Await.result(getToken(token), 5 seconds).getOrElse(Json.obj("email" -> "")) \ "email").as[String]

    (None, Some(views.html.customSignUpEmail(token,email)))
  }

  def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.alreadyRegisteredEmail(user)))
  }

  def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.customWelcomeEmail(user)))
  }

  def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.unknownEmailNotice()))
  }

  def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.passwordResetEmail(user, token)))
  }

  def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.passwordChangedNotice(user)))
  }

}