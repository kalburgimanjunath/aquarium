package plugins.fixtures

// Code from https://github.com/schleichardt/play-2-mongodb-app


import play.api.{Logger, Plugin, Application, Play}
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.libs.iteratee.Enumerator
import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin.db
import scala.io.Source
import scala.collection.JavaConversions._
import concurrent.ExecutionContext.Implicits.global
import concurrent.Future
import concurrent.duration.Duration
import scala.concurrent.Await


class FixturesPlugin(app: Application) extends Plugin {
  override def enabled = filesOption.isDefined && !filesOption.get.isEmpty


  override def onStart() { 
    if (Play.isTest){
      Logger.info("Loading Fixture files")
      filesOption.get.foreach { fileName =>
        loadFile(fileName)
      }
    }

  }

  private def loadFile(fileName: String) {
    val file = app.getFile(fileName)
    val fileContent = Source.fromFile(file, "UTF-8").mkString
    val json = Json.parse(fileContent)
    val collectionName = file.getName.replace(".json", "")
    json match {
      case array: JsArray => importToCollection(collectionName, array)
      case _ => throw new RuntimeException("expected JSON array in $fileName")
    }
  }

  private def getDataCollection(collectionName: String): JSONCollection = db.collection[JSONCollection](collectionName)
  
  def importToCollection(collectionName: String, array: JsArray) {
    val collection = getDataCollection(collectionName)

    val remove = collection.drop
    Await.ready(remove, Duration("10 seconds"))

    val allInserts = array.value map {
      document =>
        collection.insert(document)
    }
    Await.ready(Future.sequence(allInserts), Duration("10 seconds"))
  }

  private def filesOption = app.configuration.getStringList("fixtures.files")
}
