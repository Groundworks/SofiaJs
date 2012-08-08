package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current

import java.net.URL

object Memstore {
  
  implicit def fileToString(file:Option[java.io.File]): String = {
    scala.io.Source.fromFile(file.get).mkString
  }
  
  implicit def jsValuetoString(jsValue:JsValue):String = {
    jsValue.as[String]
  }
  
  def load(file:String) = Json.parse( Play.getExistingFile("resources/"+file+".json")  )
  
  var pages = scala.collection.mutable.Map[String,JsObject]()
  var sites = scala.collection.mutable.Map[String,JsObject]()
  
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
        
        val pagekey = host + "/" + path
        
        (json \ "page_content").asOpt[JsObject].map { page =>
          Memstore.pages(pagekey) = page
        }
        
        (json \ "site_content").asOpt[JsObject].map { site =>
          Memstore.sites(host) = site
        }
        
        val site = Memstore.sites.get(host) match {
          case Some(site) => site
          case _ => Memstore.load("/site")
        }
        
        val page = Memstore.pages.get(pagekey) match {
          case Some(page) => page
          case _ => Memstore.load("/default")
        }
        
        (json \ "content").asOpt[String].map { content=>
          Ok( content match {
            case "site" => Json.stringify(site)
            case _      => Json.stringify(page)
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