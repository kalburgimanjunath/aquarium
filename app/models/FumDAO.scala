package models

import play.api.libs.json.{JsValue, JsUndefined}
import play.api.libs.ws.{Response, WS}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play
import scala._
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.language.postfixOps


class FumDAO(client: FumRestClient) {

  def getAllUsers(): Option[JsValue] = {
    val responseFuture = client.call("users/?limit=10000")
    val resultFuture = responseFuture map handleResponse
    Await.result(resultFuture, 15 seconds)
  }
  
   def getAllFuturiceUsers(): Option[JsValue] = {
    val responseFuture = client.call("groups/Futurice/")
    val resultFuture = responseFuture map handleResponse
    Await.result(resultFuture, 15 seconds)
  }

  def getAllProjects(): Option[JsValue] = {
    val responseFuture = client.call("projects/?limit=10000")
    val resultFuture = responseFuture map handleResponse
    Await.result(resultFuture, 60 seconds)
  }

  def handleResponse(response: Response): Option[JsValue] ={
    if (response.status == 200) response.json \ "result" match {
      case _ => Some(response.json)
    } else if (response.status == 401) {
      throw new RuntimeException("HTTP 401: Incorrect authentication token")
    } else {
      throw new RuntimeException("Unexpected HTTP status code in response. Expected 200, got: " + response.status)
    }
  }

}

trait FumRestClient {

  def call(apiRequest: String): Future[Response]

}

object fumClient extends FumRestClient {

  def call(apiRequest: String): Future[Response] = {
    val url = Play.current.configuration.getString("fum.api.url").get + apiRequest
    val authorization = "Token " + Play.current.configuration.getString("fum.api.token").get
    val request = WS.url(url)
      .withHeaders("Authorization" -> authorization)

    request.get()
  }

}
