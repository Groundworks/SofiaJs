package controllers

import anorm._
import anorm.SqlParser._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current
import play.api.db._

import java.net.URL

object Memstore {
  
  implicit def fileToString(file:Option[java.io.File]): String = {
    scala.io.Source.fromFile(file.get).mkString
  }
  
  implicit def jsValuetoString(jsValue:JsValue):String = {
    jsValue.as[String]
  }
  
  def load(file:String) = {
    Json.parse( Play.getExistingFile("resources/"+file+".json") )
  }
  
  def getData(file:String): Option[String] = {
    DB.withConnection { implicit connection => 
      SQL("""
        SELECT content FROM page WHERE pagekey={pagekey}
        """).on("pagekey"->file).as( str("content") singleOpt )
    }
  }
  
  def setData(file:String,jsObject:JsObject) = {
    DB.withConnection { implicit connection => 
      SQL("DELETE from page WHERE pagekey={pagekey}").on("pagekey"->file).execute()
      SQL("""
        INSERT INTO page (pagekey,content) VALUES ({pagekey},{content})
        """).on(
          "pagekey" -> file,
          "content" -> Json.stringify(jsObject)
        ).executeInsert()
      }
  }
  
}

object Application extends Controller {
  
  def index = Action {
    Redirect("/default")
  }
  
  def page(page:String) = Action {
    Ok( views.html.example() )
  }
  
  def options = Action {
    Ok("").withHeaders( 
      "Access-Control-Allow-Origin"->"*",
      "Access-Control-Allow-Headers"->"Origin, Content-Type, Accept",
      "Access-Control-Allow-Methods"->"POST"
    )
  }
  
  // Serve Content via JSON API
  def content = Action { request =>
    request.body.asJson.map { json =>
      
      (json \ "location").asOpt[String].map { location =>
        
        val url  = new URL(location)
        val path = url.getPath()
        val host = url.getHost()
        
        val pagekey = host + path
        
        (json \ "page_content").asOpt[JsObject].map { page =>
          println("Saving Page Data for: " + host)
          Memstore.setData(pagekey,page)
        }
        
        (json \ "site_content").asOpt[JsObject].map { site =>
          println("Saving Site Data for: " + host)
          Memstore.setData(host,site)
        }
        
        val site = Memstore.getData(host) match {
          case Some(site:String) => 
            println("Loaded Existing Site Data for: " + host)
            site
          case _ => Json.stringify(Memstore.load("/site"))
        }
        
        val page = Memstore.getData(pagekey) match {
          case Some(page:String) => 
            println("Loaded Existing Page Data for: " + host)
            page
          case _ => Json.stringify(Memstore.load("/default"))
        }
        
        (json \ "content").asOpt[String].map { content=>
          Ok( content match {
            case "site" => site
            case _      => page
          }).withHeaders(
            "Access-Control-Allow-Origin"->"*",
            "Access-Control-Allow-Headers"->"Origin, Content-Type, Accept",
            "Access-Control-Allow-Methods"->"POST"
          )
        }.getOrElse{
          BadRequest("JSON Request Must Include Content Type")
        }
        
      }.getOrElse {
        BadRequest("JSON Request Must Include Location Parameter")
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }
}