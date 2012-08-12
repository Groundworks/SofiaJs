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

case class Success(message:String)
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
          sender ! Success(name)
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
  
  val githubClientId     = "ec46f5e732b30cc3caca"
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
  
  val githubActorRef = system.actorOf( Props[GithubActor], name="github" )
  
  def oauth2Callback = Action {
    Ok(views.html.oauthCallback())
  }
  
  implicit val timeout : Timeout = Timeout(Duration(10,"seconds"))
  def oauth2 = Action { implicit request =>
    Async {
      new AkkaPromise( githubActorRef ? request.body ) map {
        case Success(message) => Ok(
          """{"response":"ok","username":"%s"}""" format message
          ).withSession(
            "user" -> message
          ).withHeaders(
            "Access-Control-Allow-Origin"->"*",
            "Access-Control-Allow-Headers"->"Origin, Content-Type, Accept",
            "Access-Control-Allow-Methods"->"POST"
          )
        case Failure(message) => BadRequest("Bad "+message)
        case _ => BadRequest("Internal Failure")
      }
    }
  }
  
  def logout = Action {
    println("User Logged Out")
    Ok("").withNewSession;
  }
  
  def login = Action { implicit request =>
    println("Checking Login...")
    val username = request.session.get("user").orNull
    if(username==null){
      println("User Not Logged In")
      BadRequest("")
    }else{
      println("User Logged in as "+username)
      Ok(username).withSession("user"->username)
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
  
  def loader = Action {
    Ok(views.html.loader());
  }
  
  def preflight(default:String) = Action { request =>
    val origin = request.headers.get("origin").getOrElse{"*"}
    Ok("").withHeaders(
      "Access-Control-Allow-Origin" ->origin,
      "Access-Control-Allow-Headers"->"Origin, Content-Type, Accept",
      "Access-Control-Allow-Methods"->"GET,POST",
      "Access-Control-Allow-Credentials" -> "true"
    )
  }
  
  def current = Action {
    Assets.at(path="/public", "current.js")
  }
  
  def digest(preimage:String) = {
    MessageDigest.getInstance("SHA1").digest(preimage.getBytes).map("%02X".format(_)).mkString
  }
  
  def auth = Action { request =>
    println(request)
    val response = """{
      "response":"ok",
      "role":"editor"
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
  def content = Action { implicit request =>
    request.body.asJson.map { json =>
      (json \ "location").asOpt[String].map { location =>
        (json \ "clientid").asOpt[String].map { user =>
          val pagekey = hashKey(location,user)
          val page = Memstore.getData(pagekey) match {
            case Some(page:String) => 
              page
            case _ => Json.stringify(Memstore.load("/default"))
          } 
                   
          Ok(page).withHeaders(
            "Access-Control-Allow-Origin"->"*",
            "Access-Control-Allow-Headers"->"Origin, Content-Type, Accept",
            "Access-Control-Allow-Methods"->"POST"
          )
        }.getOrElse{BadRequest("")}
      }.getOrElse{BadRequest("JSON Request Must Include Location Parameter")}
    }.getOrElse{BadRequest("Expecting Json data")}
  }
  
  def hashKey(location:String,user:String) = {
    
    val url  = new URL(location)
    
    var prot = url.getProtocol()
    val path = url.getPath()
    val host = url.getHost()
    
    val preimage = "%s:%s:%s:%s" format(user,prot,path,host)
    
    MessageDigest.getInstance(
      "SHA1"
    ).digest(
      preimage.getBytes
    ).map(
      "%02X".format(_)
    ).mkString
  }
  
  // Serve Content via JSON API
  def update = Action { implicit request =>
    println("Recieving Update")
    request.body.asJson.map { json =>
      println("Request: "+Json.stringify(json))
      (json \ "location").asOpt[String].map { location =>
        (json \ "clientid").asOpt[String].map { clientid => 
          (json \ "page_content").asOpt[JsObject].map{ x:JsObject => 
            Json.stringify(x)
          }.map{ content => 
            session.get("user").map { user =>
              if (user==clientid){
                Memstore.setData(hashKey(location,user),content)
                val origin = request.headers.get("origin").getOrElse{"*"}
                Ok("").withHeaders(
                  "Access-Control-Allow-Origin" ->origin,
                  "Access-Control-Allow-Headers"->"Origin, Content-Type, Accept",
                  "Access-Control-Allow-Methods"->"POST",
                  "Access-Control-Allow-Credentials" -> "true"
                )
              }else{
                BadRequest("User Not Authenticated")
              }
            }.getOrElse{println("User Not in Session");BadRequest("User Not in Session")}
          }.getOrElse{println("Need to Authenticate");BadRequest("Need to Authenticate")}
        }.getOrElse{println("Client ID Needed");BadRequest("Client ID Needed")}
      }.getOrElse{println("JSON Request Must Include Location");BadRequest("JSON Request Must Include Location")}
    }.getOrElse{println("Expecting Json data");BadRequest("Expecting Json data")}
  }
}
