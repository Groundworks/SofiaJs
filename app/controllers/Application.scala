package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current

import java.net.URL

object Application extends Controller {
  
  implicit def fileToString(file:Option[java.io.File]): String = {
    scala.io.Source.fromFile(file.get).mkString
  }
  
  implicit def jsValuetoString(jsValue:JsValue):String = {
    jsValue.as[String]
  }
  
  def load(file:String) = Json.parse( Play.getExistingFile("resources/"+file+".json")  ) match {
    case JsObject(fields) => fields . toMap . mapValues ( jsValuetoString )
  }
  
  def index = Action {
    Redirect("/default")
  }
  
  def page(page:String) = Action {
    Ok( views.html.example() )
  }
  
  // Serve Content via JSON API
  def content = Action { request =>
    request.body.asJson.map { json =>
      (json \ "location").asOpt[String].map { location =>
        
        val url = new URL(location)
        val path = url.getPath()
        
        Ok( Json.toJson(load(path)) )
        
      } .getOrElse {
        BadRequest("JSON Request Must Include Location Parameter")
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }
}