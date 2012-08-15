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
  
  def getData(location:String,version:String,clientid:String): Option[String] = {
    Logger.info("DB GET %s:%s:%s" format (location,version,clientid))
    DB.withConnection { implicit connection => 
      SQL(
        "SELECT content FROM page WHERE location={location} AND version={version} AND userid={clientid}"
      ).on(
        "location" -> location,
        "version"  -> version,
        "clientid" -> clientid
      ).as( 
        str("content") singleOpt
      )
    }
  }
  
  def setData(file:String,location:String,version:String,userid:String,jsObject:String) {
    Logger.info("DB SET %s:%s:%s" format (location,version,userid))
    DB.withConnection { implicit connection => 
      SQL("DELETE from page WHERE pagekey={pagekey}").on("pagekey"->file).execute()
      SQL(
        "INSERT INTO page (content,pagekey,location,version,userid) VALUES ({content},{pagekey},{location},{version},{userid})"
      ).on(
          "pagekey" -> file,
          "content" -> jsObject,
          "location"-> location,
          "version" -> version,
          "userid"  -> userid
      ).executeInsert()
    }
  }
  
  def getHead(location:String,clientid:String): Option[String] = {
    DB.withConnection { implicit connection => 
      SQL(
        "SELECT version FROM head WHERE location={location} AND clientid={clientid}"
      ).on(
        "location" -> location,
        "clientid" -> clientid
      ).as( 
        str("version") singleOpt
      )
    }
  }
  
  def setHead(location:String,clientid:String,version:String){
    DB.withConnection{ implicit connection =>
      SQL("DELETE FROM head WHERE location={location}").on("location"->location).execute();
      SQL("INSERT INTO head (location,version,clientid) VALUES ({location},{version},{clientid})").on(
        "location"->location,
        "version" ->version,
        "clientid"->clientid
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
  
  def sanitizeLocation(location:String) = {
  val length = location.length
  if( location.charAt(length-1) == '/' ){
      location.substring(0,location.length-1)
    } else {
      location
    }
  }
  
  // Serve Content via JSON API
  def content = Action { implicit request =>
    crossSiteOk(request.body.asJson.map { json =>
      (json \ "location").asOpt[String].map { _location =>
        val location = sanitizeLocation(_location)
        (json \ "clientid").asOpt[String].map { user =>
          val version:String = (json \ "version").asOpt[String].map { version =>
            if(version=="" || version=="#"){
              Memstore.getHead(location,user).getOrElse{""}
            }else{
              version
            }
          }.getOrElse{""}
          println("Load Location: %s%s" format (location,version))
          Ok(Memstore.getData(location,version,user) match {
            case Some(page:String) => page
            case None => Json.stringify(Memstore.load("/default"))
          })
        }.getOrElse{BadRequest("Request Requires 'clientid' Parameter")}
      }.getOrElse{BadRequest("Request Requires 'location' Parameter")}
    }.getOrElse{BadRequestExpectingJson})
  }
  
  def hashKey(location:String,version:String,user:String) = {
    val url  = new URL(location)
    var prot = url.getProtocol()
    val path = url.getPath()
    val host = url.getHost()
    
    val preimage = "%s:%s:%s:%s%s" format(user,prot,path,host,version)
    
    MessageDigest.getInstance(
      "SHA1"
    ).digest(
      preimage.getBytes
    ).map(
      "%02X".format(_)
    ).mkString
  }
  
  def pageHash(page:String) = {
    MessageDigest.getInstance(
      "SHA1"
    ).digest(
      page.getBytes
    ).map(
      "%02X".format(_)
    ).mkString
  }
  
  implicit def jsonify(seq:(String,String)*): String = {
    Json.stringify(Json.toJson(Map(seq:_*)))
  }
  
  def update = Action { implicit request =>
    request.body.asJson.map { json =>
      (json \ "location").asOpt[String].map { _location =>
        val location = sanitizeLocation(_location)
        (json \ "clientid").asOpt[String].map { clientid => 
          (json \ "page_content").asOpt[JsObject].map{ x:JsObject => 
            Json.stringify(x)
          }.map{ content => 
            val version = "#!sofiajs/" + pageHash(content)
            val updated = location + version
            println("Update Location: " + updated)
            Memstore.setData(hashKey(location,version,clientid),location,version,clientid,content)
            request.session.get("user").map { user =>
              if (user==clientid){
                (json \ "publish").asOpt[String].map { publish =>
                  Logger.debug("Publish Update: %s" format publish)
                  if(publish equals "yes"){
                    Memstore.setHead(location,clientid,version)
                  }
                }.getOrElse{
                  Logger.debug("Not Published")
                }
              } else {
                Logger.debug("Non-Owner Update Not Publisehd")
              }
            }.getOrElse{
              Logger.debug("Anonymous Update Not Published")
            }
            Created(
              jsonify("response"->"ok","location"-> updated)
            ).withHeaders("Location" -> updated )
          }.getOrElse{BadRequest("Need to Authenticate")}
        }.getOrElse{BadRequest("Client ID Needed")}
      }.getOrElse{BadRequest("JSON Request Must Include Location")}
    }.getOrElse{BadRequestExpectingJson}
  }
}
