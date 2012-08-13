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
          sender ! Success(name)
        },(error:String)=>{
          sender ! Failure(error)
        })
      }.getOrElse{
        sender ! Failure("JSON Request Missing Code Parameter")
      }
    }.getOrElse{
      sender ! Failure("Content not JSON")
    }
  }
  
  val config = Play.current.configuration
  val githubClientId     = config.getString("sjs.github.clientid").get
  val githubClientSecret = config.getString("sjs.github.clientsecret").get
  
  def githubVerifyOAuth(code:String,callback:String=>Unit,error:String=>Unit) = {
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
        WS.url("https://api.github.com/user").withHeaders(
          "Accept" -> "application/json"
        ).withQueryString(
          "access_token" -> accessToken
        ).get().map { response => 
          (response.json \ "login").asOpt[String].map{ login => 
            callback(login)
          }.getOrElse{error("No login parameter in JSON response")}
        } // Promise Expired
      }.getOrElse(error("Missing Access Token"))
    } // Promise Expired
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
  
  def getData(file:String): Option[String] = {
    DB.withConnection { implicit connection => 
      SQL(
        "SELECT content FROM page WHERE pagekey={pagekey}"
      ).on(
        "pagekey"->file
      ).as( 
        str("content") singleOpt
      )
    }
  }
  
  def setData(file:String,location:String,userid:String,jsObject:String) {
    DB.withConnection { implicit connection => 
      SQL("DELETE from page WHERE pagekey={pagekey}").on("pagekey"->file).execute()
      SQL(
        "INSERT INTO page (content,pagekey,location,userid) VALUES ({content},{pagekey},{location},{userid})"
      ).on(
          "pagekey" -> file,
          "content" -> jsObject,
          "location"-> location,
          "userid"  -> userid
      ).executeInsert()
    }
  }
  
}

object Application extends Controller {
  
  val githubActorRef = system.actorOf( Props[GithubActor], name="github" )
  
  def crossSiteOk(response:PlainResult) = {
    response.withHeaders(
      "Access-Control-Allow-Origin"->"*",
      "Access-Control-Allow-Headers"->"Origin, Content-Type, Accept",
      "Access-Control-Allow-Methods"->"POST"
    )
  }
  
  implicit val timeout : Timeout = Timeout(Duration(10,"seconds"))
  def authorize = Action { implicit request =>
    Async {
      new AkkaPromise( githubActorRef ? request.body ) map {
        case Success(message) => Ok(
          jsonify("response" -> "ok","username" -> message)
        ).withSession(
          "user" -> message
        )
        case Failure(message) => BadRequest("Bad "+message)
        case _ => InternalServerError("Internal Message Failure")
      }
    }
  }
  
  def logout = Action {
    NoContent.withNewSession;
  }
  
  def login = Action { implicit request =>
    val username = request.session.get("user").orNull
    if(username==null){
      BadRequest("")
    }else{
      Ok(username).withSession("user"->username)
    }
  }
    
  val BadRequestExpectingJson = BadRequest("Expecting JSON")
  
  def loader = Action {
    Ok(views.html.loader());
  }
  
  def preflight(default:String) = Action { request =>
    crossSiteOk(NoContent)
  }
  
  def index = Action {
    Redirect("/default")
  }
  
  def page(page:String) = Action {
    Ok( views.html.example() )
  }
  
  // Serve Content via JSON API
  def content = Action { implicit request =>
    crossSiteOk(request.body.asJson.map { json =>
      (json \ "location").asOpt[String].map { location =>
        (json \ "clientid").asOpt[String].map { user =>
          Ok(Memstore.getData(hashKey(location,user)) match {
            case Some(page:String) => page
            case None => Json.stringify(Memstore.load("/default"))
          })
        }.getOrElse{BadRequest("Request Requires 'clientid' Parameter")}
      }.getOrElse{BadRequest("Request Requires 'location' Parameter")}
    }.getOrElse{BadRequestExpectingJson})
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
  
  implicit def jsonify(seq:(String,String)*): String = {
    Json.stringify(Json.toJson(Map(seq:_*)))
  }
  
  def update = Action { implicit request =>
    request.body.asJson.map { json =>
      (json \ "location").asOpt[String].map { location =>
        (json \ "clientid").asOpt[String].map { clientid => 
          (json \ "page_content").asOpt[JsObject].map{ x:JsObject => 
            Json.stringify(x)
          }.map{ content => 
            session.get("user").map { user =>
              if (user==clientid){
                Memstore.setData(hashKey(location,user),location,user,content)
                val origin = request.headers.get("origin").getOrElse{"*"}
                Ok(jsonify("response"->"ok", "status"->"okay"))
              }else{
                Unauthorized("User Not Authenticated")
              }
            }.getOrElse{BadRequest("User Not in Session")}
          }.getOrElse{BadRequest("Need to Authenticate")}
        }.getOrElse{BadRequest("Client ID Needed")}
      }.getOrElse{BadRequest("JSON Request Must Include Location")}
    }.getOrElse{BadRequestExpectingJson}
  }
}
