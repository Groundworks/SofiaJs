package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

object Application extends Controller {
  
  val data = Map(
    "#main" -> "This data has been loaded dynamically via Javascript</a>",
    "#nav1" -> "<ul><li>One</li><li>Two</li></ul>"
  )
  
  def index = Action {
    Redirect("/assets/example.html")
  }
  
  // Serve Content via JSON API
  def content = Action { request =>
    request.body.asJson.map { json =>
      (json \ "location").asOpt[String].map { location =>
        
        // Insert Dynamic Content
        val data2 = data + ("#sub1" -> ("This content is intended for the page: "+ location))
        
        Ok( Json.toJson(data2) )
      } .getOrElse {
        BadRequest("JSON Request Must Include Location Parameter")
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }
}