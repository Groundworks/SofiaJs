package controllers

import java.security.MessageDigest

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
  
  def setPullRequest(file:String){
    println("Pull Request Submitted for: "+file)
    DB.withConnection { implicit connection => 
      removePullRequest(file)
      SQL("""
        INSERT INTO pullrequest (pagekey) VALUES ({pagekey})
        """).on("pagekey"->file).executeInsert();
    }
  }
  
  def removePullRequest(file:String){
    println("Pull Request Resolved for: "+file)
    DB.withConnection { implicit connection => 
      SQL("DELETE from pullrequest WHERE pagekey={pagekey}").on("pagekey"->file).execute()
    }
  }
  
  def getData(file:String): Option[String] = {
    DB.withConnection { implicit connection => 
      SQL("""
        SELECT content FROM page WHERE pagekey={pagekey}
        """).on("pagekey"->file).as( str("content") singleOpt )
    }
  }
  
  def setData(file:String,jsObject:String) {
    DB.withConnection { implicit connection => 
      SQL("DELETE from page WHERE pagekey={pagekey}").on("pagekey"->file).execute()
      SQL("""
        INSERT INTO page (content,pagekey) VALUES ({content},{pagekey})
        """).on(
          "pagekey" -> file,
          "content" -> jsObject
        ).executeInsert()
      }
  }
  
}

object Application extends Controller {
  
  def current = Action {
    Assets.at(path="/public", "current.js")
  }
  
  val masterCredential = "0239jf09wjf09j23f902jf80hf0ajsf0392jf23023jf"
  val guestCredential = "039jf029jf2039fj0jf0a8jf0asnf0823nf023"
  
  def cred = Action { request =>
    request.body.asJson.map { json =>
      ( json \ "email" ).asOpt[String].map { email =>
        ( json \ "paswd" ).asOpt[String].map { paswd =>
          
          var response = ""
          if (email=="jacob" && paswd=="therealultimatesite") {
            response = """{
              "response":"ok",
              "credential":"%s"
            }""" format ( masterCredential )
          } else if(email=="guest") {
            response = """{
              "response":"ok",
              "credential":"%s"
            }""" format ( guestCredential )
          } else {
            response = """{
              "response":"fail"
            }"""
          }
          Ok(response)
        }.getOrElse {
            BadRequest("Valid Password Required")
        }
      }.getOrElse {
        BadRequest("Valid Email Required")
      }
    } .getOrElse{
      BadRequest("JSON Request Required")
    }
  }
  
  def auth = Action { request =>
    println(request)
    val response = """{
      "response":"ok",
      "role":"editor",
      "access":"092j3f023f0f9j1f0h138fda0fj93jf290jf238fa80f32"
    }"""
    Ok(response)
  }
  
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
        
        var hash = (json \ "hash").asOpt[String].getOrElse{""}
        
        val url  = new URL(location)
        val path = url.getPath()
        val host = url.getHost()
        
        val pagekey = host + path + hash
        println("PageKey (Read):"+pagekey)
        
        // Get Data //
        
        val site = Memstore.getData(host) match {
          case Some(site:String) => 
            site
          case _ => Json.stringify(Memstore.load("/site"))
        }
        
        val page = Memstore.getData(pagekey) match {
          case Some(page:String) => 
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
  
  def pullrequest = Action { request => 
    request.body.asJson.map { json => 
      (json \ "location").asOpt[String].map { location => 
        
        val hash = (json \ "hash").asOpt[String].getOrElse{""}
        val url  = new URL(location)
        val path = url.getPath()
        val host = url.getHost()
        
        Memstore.setPullRequest(location)
        
        Ok( """{"message":"Pull Request Submitted"}""" )
        
      }.getOrElse{BadRequest("")}
    }.getOrElse{BadRequest("")}
  }
  
  def push = Action { request =>
    request.body.asJson.map { json => 
      (json \ "location").asOpt[String].map { location => 
        val hash = (json \ "hash").asOpt[String].getOrElse{""}
        val url  = new URL(location)
        val path = url.getPath()
        val host = url.getHost()
        (json \ "credential").asOpt[String].map { credential => 
          if(credential==masterCredential){
            Memstore.setData(host+path,Memstore.getData(host+path+hash).get)
            Ok("")
          }else if(credential==guestCredential){
            BadRequest("")
          }else{
            BadRequest("")
          }
        }.getOrElse{BadRequest("")}
      }.getOrElse{BadRequest("")}
    }.getOrElse{BadRequest("")}
  }
  
  def site = Action { request => 
    println("Updating Site Data")
    request.body.asJson.map { json => 
      (json \ "location").asOpt[String].map { location => 
        
        val url  = new URL(location)
        val path = url.getPath()
        val host = url.getHost()
        
        (json \ "site_content").asOpt[JsObject].map{siteContent=>
          Memstore.setData(host,Json.stringify(siteContent))
          Ok("{}")
        }.getOrElse(BadRequest("Missing site_content in JSON Request"))
      }.getOrElse(BadRequest("Missing 'site' Parameter in JSON Request"))
    }.getOrElse(BadRequest("Site Action Expecting JSON"))
  }
  
  // Serve Content via JSON API
  def update = Action { request =>
    request.body.asJson.map { json =>
      
      (json \ "location").asOpt[String].map { location =>
        
        val pageContent = (json \ "page_content").asOpt[JsObject].map{page=>Json.stringify(page)}
        
        var hash = MessageDigest.getInstance("SHA1").digest((pageContent.getOrElse{""}).getBytes).map("%02X".format(_)).mkString
        
        val url  = new URL(location)
        val path = url.getPath()
        val host = url.getHost()
        
        val pagekey = host + path + "#" + hash
        println("PageKey (Update):"+pagekey)
        
        // Save Incoming Data //
        (json \ "credential").asOpt[String].map { credential => 
          println("Credentials Received")
          // Check Credentials Here //
          if (credential==masterCredential||credential==guestCredential){
            println("Credential Received is Valid - Saving Data")
            
            pageContent.map{ x => Memstore.setData(pagekey,x) }
            
            val role = if(credential==masterCredential){
              "push"
            } else {
              "pull-request-only"
            }
            
            Ok("""{
                   "hashbang":"%s",
                   "role":"%s"
                  }""" format (hash,role)).withHeaders("Content-Type"->"application/json")
            
          } else {
            // Fail Credentials
            println("Credential Received is Invalid :"+credential)
            BadRequest("Credential Received is Invalid")
          }
        }.getOrElse {
          println("No Credentials Received for Request")
          BadRequest("Credential Received is Invalid")
        }
      }.getOrElse {
        BadRequest("JSON Request Must Include Location Parameter")
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }
}