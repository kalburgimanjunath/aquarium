package models

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import javax.mail._
import javax.mail.internet._
import javax.mail.search._
import scala.collection.mutable.ArrayBuffer


class MailDAO() {
  //TODO: Refactior connection and inbox fiddling to 2 separate function

  def getAllUnreadMail(): Array[JsObject] = {
      val properties = System.getProperties()
      properties.setProperty("mail.store.protocol", "imaps")
      val session = javax.mail.Session.getDefaultInstance(properties, null)
      val imapsStore = session.getStore("imaps")
      val user = Play.current.configuration.getString("smtp.user").get
      val pass = Play.current.configuration.getString("smtp.password").get
      try {
        imapsStore.connect("imap.gmail.com", 993, user, pass)
      }
      catch {
        case err: MessagingException => Logger.info("MessagingException: " + err.toString)
        case err: AuthenticationFailedException => Logger.info("Incorrect credentials, check conf file: " + err.toString)
      }
      Logger.info("[MailClient.scala] - Connected to email server")
      val inbox = imapsStore.getFolder("Inbox")
      inbox.open(Folder.READ_WRITE)
      
      // Search inbox for unread messages
      val seen = new Flags(Flags.Flag.SEEN)
      val unread = new FlagTerm(seen, false)
      val unreadMessages = inbox.search(unread)
      
      val result = handleResponse(unreadMessages)

      inbox.close(true)
      imapsStore.close()
      Logger.info("[MailClient.scala] - Connection to email server closed")
      return result
  }

  def responseToJSON(message: Message, projects: JsArray, body: String): JsObject = {
    Json.obj(
              "subject" -> message.getSubject.toString, // currently used to identify project
              "body" -> body,
              "senderAddress" -> message.getFrom.head.asInstanceOf[InternetAddress].getAddress,
              "senderName" -> message.getFrom.head.asInstanceOf[InternetAddress].getPersonal,
              // TODO? check project name against database, and send reply email for erroneous project name
              "project" -> projects,
              "sentDate" -> message.getSentDate,
              "receivedDate" -> message.getReceivedDate,
              "active" -> true // project is active
    )
  }

  // Maps the relevant parts of unreadMessages to the returned messagesList as JSObjects
  def handleResponse(unreadMessages: Array[Message]): Array[JsObject] = {
    val messagesList:Array[JsObject]  = unreadMessages.map{
      message: Message =>
        
        // Separate relevant recipients (= projects)
        val allRecipients = message.getAllRecipients()
        val recipientProjects : ArrayBuffer[String] = new ArrayBuffer[String]
        for (address <- allRecipients) {
          if (address.asInstanceOf[InternetAddress].getAddress().contains("akvaario")) {
            recipientProjects.append(address.asInstanceOf[InternetAddress].getAddress())
          }
        }
        
        // Retrieve project names into JsArray
        var projects = new JsArray
        recipientProjects.foreach { recipient => 
          val project = recipient.substring(recipient.lastIndexOf('+')+1, recipient.indexOf('@')).toLowerCase
          projects = projects.append(JsString(project))
        }
        
        val contentType = message.getContentType.toLowerCase
        var bodyContent = "" // Body of message
        
        // If the message isn't a "normal" multipart/alternative
        // TODO: Make this prettier AKA pattern matching? Since Java -> might not work
        if (contentType.contains("report")||contentType.contains("text/plain")) {
          message.setFlags(new Flags(Flags.Flag.SEEN), true)
          bodyContent = message.getContent.toString
        }
        
        // All gmail messages SHOULD be multipart/alternative
        else {
          val mp = message.getContent.asInstanceOf[Multipart] // Getting the content implicitly sets the flag to SEEN (=read)
          val i = 0
          var part = mp.getBodyPart(0)
          // Iterate through BodyParts of message
          for (i <- 0 until mp.getCount) {
            val bp = mp.getBodyPart(i)
            // Choose the plaintext part and save message body
            if (bp.isMimeType("text/plain")) {
              part = bp
              bodyContent = bp.getContent.toString
            }
            
          }
        }
        
        responseToJSON(message, projects, bodyContent)
    }
  messagesList
  }

}
