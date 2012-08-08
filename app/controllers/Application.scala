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
  var site  = load("/site")
  
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
      "Access-Control-Allow-Headers"->"Content-Type"
      )
  }
  
  // Serve Content via JSON API
  def content = Action { request =>
    request.body.asJson.map { json =>
      
      (json \ "location").asOpt[String].map { location =>
        
        val url  = new URL(location)
        val path = url.getPath()
        
        (json \ "page_content").asOpt[JsObject].map { page =>
          print("Updating Page Content")
          Memstore.pages(path) = page
        }
        
        (json \ "site_content").asOpt[JsObject].map { site =>
          print("Updating Site Content")
          Memstore.site = site
        }
        
        val page = Memstore.pages.get(path) match {
          case Some(page) => page
          case _ => Memstore.load("/default")
        }
        
        path match {
          case "/site" => Ok( Json.stringify(Memstore.site) )
          case _       => Ok( Json.stringify(page) )
        }
        
      } .getOrElse {
        BadRequest("JSON Request Must Include Location Parameter")
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }
}