import org.specs2.mutable._
import play.api.libs.iteratee._
import scala.concurrent._

import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._

object Common {
  import scala.concurrent._
  import scala.concurrent.duration._
  import reactivemongo.api._
  import reactivemongo.bson.handlers.DefaultBSONHandlers

  implicit val ec = ExecutionContext.Implicits.global
  /*implicit val writer = DefaultBSONHandlers.DefaultBSONDocumentWriter
  implicit val reader = DefaultBSONHandlers.DefaultBSONDocumentReader
  implicit val handler = DefaultBSONHandlers.DefaultBSONReaderHandler*/
  
  val timeout = 5 seconds
  
  lazy val connection = MongoConnection(List("localhost:27017"))
  lazy val db = {
    val _db = connection("specs2-test-reactivemongo")
    Await.ready(_db.drop, timeout)
    _db
  }
}


case class Expeditor(name: String)
case class Item(name: String, description: String, occurrences: Int)
case class Package(
  expeditor: Expeditor,
  items: List[Item],
  price: Float
)



class JsonBson extends Specification {
  import Common._

  import reactivemongo.bson._
  import play.modules.reactivemongo.PlayBsonImplicits
  import play.modules.reactivemongo.PlayBsonImplicits._
  
  sequential
  lazy val collection = db("somecollection_commonusecases")

  val pack = Package(
    Expeditor("Amazon"),
    List(Item("A Game of Thrones", "Book", 1)),
    20
  )
  
  "ReactiveMongo Plugin" should {
    "convert a simple json to bson and vice versa" in {
      val json = Json.obj("coucou" -> JsString("jack"))
      println(json)
      val bson = BSONDocument(JsObjectWriter.write(json))
      println(BSONDocument.pretty(bson))
      val json2 = JsObjectReader.fromBSON(bson)
      println(json2)
      json.toString mustEqual json2.toString
    }
    "convert a simple json array to bson and vice versa" in {
      val json = Json.arr(JsString("jack"), JsNumber(9.1))
      println(json)
      val bson = BSONArray(JsArrayWriter.write(json))
      val json2 = JsArrayReader.read(bson.toTraversable)
      println(json2)
      json.toString mustEqual json2.toString
    }
    "convert a json doc containing an array and vice versa" in {
      val json = Json.obj(
        "name" -> JsString("jack"),
        "contacts" -> Json.arr(
          Json.obj(
            "email" -> "jack@jack.com")
        )
      )
      println(json)
      val bson = BSONDocument(JsObjectWriter.write(json))
      println(BSONDocument.pretty(bson))
      val json2 = JsObjectReader.fromBSON(bson)
      println(json2)
      json.toString mustEqual json2.toString
    }
  }
}