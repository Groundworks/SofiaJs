package controllers

import anorm._
import anorm.SqlParser._

import play.api._
import play.api.db._
import play.api.mvc._
import play.api.libs.oauth._
import play.api.libs.json._

import play.api.libs.ws.WS
import play.api.Play.current

// Akka
import play.libs.Akka._
import play.api.libs.concurrent.AkkaPromise
import akka.actor.{Actor,ActorRef,Props}
import akka.pattern.ask 
import akka.util.Timeout
import akka.util.Duration
import play.libs.Akka.system

import java.net.URL
import java.security.MessageDigest

case object Success
case class Failure(message:String)

class GithubActor extends Actor {
  
  def receive = {
    case body:AnyContent => verify(body,sender)
    case _ => sender ! Failure("Message Not Understood")
  }
  
  def verify(body:AnyContent,sender:ActorRef) = {
    body.asJson.map { json => 
      (json \ "code").asOpt[String].map { code =>
        githubVerifyOAuth(code,(name:String)=>{
          println("Called Success Callback")
          sender ! Success
        },(error:String)=>{
          println("Called Failure Callback")
          sender ! Failure(error)
        })
      }.getOrElse{
        sender ! Failure("JSON Request Missing Code Parameter")
      }
    }.getOrElse{
      sender ! Failure("Content not JSON")
    }
  }
  
  val githubClientId = "ec46f5e732b30cc3caca"
  val githubClientSecret = "1f9b2f31289ffcedc6b96b28b1599b353d74ac47"
  
  def githubVerifyOAuth(code:String,callback:String=>Unit,error:String=>Unit) = {
    println("Using Github Verification code: "+code)
    WS.url("https://github.com/login/oauth/access_token").withHeaders(
      "Accept" -> "application/json"
    ).post(
      Map(
        "client_id"     -> Seq(githubClientId),
        "client_secret" -> Seq(githubClientSecret),
        "code"          -> Seq(code)
      )
    ).map { response => 
      print( "/access_token Response: " + Json.stringify(response.json) )
      (response.json \ "access_token").asOpt[String].map{ accessToken =>
        println("Using Access Github Token: "+accessToken)
        WS.url("https://api.github.com/user").withHeaders(
          "Accept" -> "application/json"
        ).withQueryString(
          "access_token" -> accessToken
        ).get().map { response => 
          println( "/user Response: " + Json.stringify(response.json) )
          (response.json \ "login").asOpt[String].map{ login => 
            println("Using Login: "+login)
            callback(login)
          }.getOrElse{error("No login parameter in JSON response")}
        } // Promise Expired
      }.getOrElse(error("Missing Access Token")) // No Access Token
    } // Promise Expired
  }
}

object Authenticator extends Controller {
  
  val masterCredential = "0239jf09wjf09j23f902jf80hf0ajsf0392jf23023jf"
  val guestCredential  = "039jf029jf2039fj0jf0a8jf0asnf0823nf023"
  
  val githubActorRef = system.actorOf( Props[GithubActor], name="github" )
  
  def oauth2Callback = Action {
    Ok(views.html.oauthCallback())
  }
  
  implicit val timeout : Timeout = Timeout(Duration(10,"seconds"))
  
  def oauth2 = Action { implicit request =>
    Async {
      new AkkaPromise( githubActorRef ? request.body ) map {
        case Success => Ok("""{"response":"ok","credential":"%s"}""" format masterCredential)
        case Failure(message) => BadRequest(message)
        case _ => BadRequest("Internal Failure")
      }
    }
  }
}

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
  
  def setPullRequest(from:String,to:String){
    println("Pull Request Submitted from: "+from+" to: "+to)
    DB.withConnection { implicit connection => 
      removePullRequest(from)
      SQL("""
        INSERT INTO pullrequest (fromkey,tokey) VALUES ({fromkey},{tokey})
        """).on("fromkey"->from,"tokey"->to).executeInsert();
    }
  }
  
  def getPullRequests(to:String):List[String] = {
    DB.withConnection { implicit connection => 
      SQL("""SELECT fromkey FROM pullrequest WHERE tokey={tokey}""").on("tokey"->to).as( str("fromkey") * ).map {
        case x:String => x
      }
    }
  }
  
  def removePullRequest(from:String){
    println("Pull Request Submitted from: "+from)
    DB.withConnection { implicit connection => 
      SQL("DELETE from pullrequest WHERE fromkey={fromkey}").on("fromkey"->from).execute()
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
  
  val BadRequestExpectingJson = BadRequest("Expecting JSON")
  
  def current = Action {
    Assets.at(path="/public", "current.js")
  }
  
  val masterCredential = "0239jf09wjf09j23f902jf80hf0ajsf0392jf23023jf"
  val guestCredential  = "039jf029jf2039fj0jf0a8jf0asnf0823nf023"
  
  def digest(preimage:String) = {
    MessageDigest.getInstance("SHA1").digest(preimage.getBytes).map("%02X".format(_)).mkString
  }
  
  def cred = Action { request =>
    request.body.asJson.map { json =>
      (json \ "key").asOpt[String].map{ key =>
        (json \ "")
        
        val origin = request.headers.get("Referer").getOrElse{
          BadRequest("")
        }
        
        val preimage = key
        val hashkey = digest(preimage)
        
        // Validate Credential
        println("Credentials with Origin: (Unused) %s" format origin )
        println("            and key    : %s" format key )
        println("            and digest : %s" format hashkey )
        
        Ok("""{"response":"ok","credential":"%s"}""" format masterCredential)
      }.getOrElse{BadRequest("Need 'key' attribute in JSON request")}
    }.getOrElse{BadRequestExpectingJson}
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
        var prot = url.getProtocol()
        var port = url.getPort()
        val path = url.getPath()
        val host = url.getHost()
        
        val pagekey = prot + "://" + host + ":%d".format(port) + path + hash
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
        var prot = url.getProtocol()
        var port = url.getPort()
        val path = url.getPath()
        val host = url.getHost()
        val pagekey = prot + "://" + host + ":%d".format(port) + path
        
        Memstore.setPullRequest(location,pagekey)
        
        Ok( """{"message":"Pull Request Submitted"}""" )
        
      }.getOrElse{BadRequest("")}
    }.getOrElse{BadRequest("")}
  }
  
  def pullrequests = Action { request => 
    request.body.asJson.map { json =>
      (json \ "location").asOpt[String].map {location =>
        
        val prs:List[String] = Memstore.getPullRequests(location)
        
        Ok(Json.toJson(prs))
        
      }.getOrElse{BadRequest("")}
    }.getOrElse{BadRequest("")}
    
  }
  
  def push = Action { request =>
    request.body.asJson.map { json => 
      (json \ "location").asOpt[String].map { location => 
        val hash = (json \ "hash").asOpt[String].getOrElse{""}
        
        val url  = new URL(location)
        var prot = url.getProtocol()
        var port = url.getPort()
        val path = url.getPath()
        val host = url.getHost()
        
        val pagekey = prot + "://" + host + ":%d".format(port) + path
        val hashkey = prot + "://" + host + ":%d".format(port) + path + hash
        
        (json \ "credential").asOpt[String].map { credential => 
          if(credential==masterCredential){
            Memstore.setData(pagekey,Memstore.getData(hashkey).get)
            Memstore.removePullRequest(hashkey)
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
        var prot = url.getProtocol()
        var port = url.getPort()
        val path = url.getPath()
        val host = url.getHost()
        
        val pagekey = prot + "://" + host + ":%d".format(port) + path + "#" + hash
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