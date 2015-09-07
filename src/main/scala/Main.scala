package xyz.hyperreal.blog

import com.typesafe.config.ConfigFactory
import com.github.kxbmap.configs._

import akka.actor.ActorSystem

import spray.routing._
import spray.http._
import spray.json._
import spray.json.DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport._
import MediaTypes._

import shapeless._

import in.azeemarshad.common.sessionutils.SessionDirectives

import models._

import concurrent.duration._
import util.{Success, Failure}


object Main extends App with SimpleRoutingApp with SessionDirectives {

	Startup

  val conf = ConfigFactory.load
	val system = conf.opt[String]( "blog.domain.system" )
	
	implicit val akka = ActorSystem( "on-spray-can" )
	implicit val context = akka.dispatcher

  startServer( conf.get[String]("blog.server.interface"), conf.get[Int]("blog.server.port") ) {
	
		def blog: Directive[dao.Blog :: HNil] = hostName hflatMap {
			case h :: HNil =>
				await( dao.Blogs.find(h) ) match {
					case None => reject
					case Some( b ) => provide( b )
				}
		}
		
		def user: Directive[dao.Blog :: Option[models.User] :: HNil] = (blog & optionalSession) hflatMap {
			case b :: None :: HNil => hprovide( b :: None :: HNil )
			case b :: Some( s ) :: HNil => hprovide( b :: Queries.findUser(s.data("id").toInt) :: HNil )
		}
		
		def admin: Directive[dao.Blog :: models.User :: HNil] = (blog & session) hflatMap {
			case b :: s :: HNil =>
				Queries.findUser( s.data("id").toInt ) match {
					case Some( u ) if u.roles.exists(r => r.blogid == b.id.get && r.role == "admin") => hprovide( b :: u :: HNil )
					case _ => reject( AuthorizationFailedRejection )
				}
		}
	
		val systemValidate = validate( system != None, "blog.domain.system not set" ) & host( system.getOrElse("") )
		
		//
		// resource renaming routes (these will mostly be removed as soon as possible)
		//
		pathPrefix("sass") {
			getFromResourceDirectory("resources/public") } ~
		(pathPrefix("js") | pathPrefix("css")) {
			getFromResourceDirectory("public") } ~
		pathSuffixTest( """.*(?:\.(?:html|png|ico))"""r ) { _ =>
			getFromResourceDirectory( "public" ) } ~
		pathPrefix("coffee") {
			getFromResourceDirectory("public/js") } ~
		pathPrefix("webjars") {
			getFromResourceDirectory("META-INF/resources/webjars") } ~
		//
		// application routes
		//
		//hostName {h => complete(h)} ~
		(get & pathPrefixTest( !("api"|"setup-admin"|"admin") ) & clientIP & unmatchedPath & optionalHeaderValueByName( "Referer" ) & blog) { (ip, path, referrer, blog) =>
			Application.logVisit( ip, path toString, referrer, blog )
			reject } ~
		(get & pathSingleSlash & user) {
			(b, u) => complete( Application.index(b, u) ) } ~
		(get & pathSingleSlash & systemValidate) {
			complete( Application.system ) } ~
		(get & path( "setup-admin"/IntNumber ) & systemValidate) {
			blogid => complete( Application.setup(blogid) ) } ~
		// 		(get & path( "author"/IntNumber ) & hostName & optionalSession) {
		// 			(id, h, session) => complete(Application.index( h, session )) } ~
		// 		(get & path( "category"/IntNumber ) & hostName) {
		// 			(id, host) => complete(Application.index( "localhost" )) } ~
		// 		(get & path( "uncategorized" ) & hostName) {
		// 			host => complete( "uncategorized" ) } ~
		// 		(get & path( "b"/LongNumber ) & hostName) {
		// 			(time, host) => complete(Application.index( "localhost" )) } ~
		// 		(get & path( "a"/LongNumber ) & hostName) {
		// 			(time, host) => complete(Application.index( "localhost" )) } ~
		(get & path( IntNumber ) & user) {
			(p, b, u) => complete( Application.post(p, b, u) ) } ~
		// 		(get & path( IntNumber~"-"~IntNumber ) & hostName) {
		// 			(year, month, host) => complete( (year, month, host) ) } ~
		// 		(get & path( IntNumber~"-"~IntNumber~"-"~IntNumber ) & hostName) {
		// 			(year, month, day, host) => complete( (year, month, day, host) ) } ~
		path( "login" ) {
			(get & user) { (b, u) =>
				if (u != None)
					redirect( "/", StatusCodes.SeeOther )
				else
					complete( Application.login(b) ) } ~
			(post & formFields( 'email, 'password, 'rememberme ? "no" )) {
				(email, password, rememberme) => Application.authenticate( email, password ) } } ~
		(get & path( "register" ) & blog) {
			_ => complete( Application.register ) } ~
		(get & path( "admin" ) & admin) {
			(b, u) => complete( Application.admin(b, u) ) } ~
// 		(post & path( "post" ) & admin & formFields( 'category.as[Int], 'headline, 'text )) {
// 			(b, u, category, headline, text) => complete( Application.post(b, u, category, headline, text) ) } ~
		(get & path( "logout" ) & session) {
			_ => clearSession & redirect( "/", StatusCodes.SeeOther ) } ~
		(post & path( "comment" ) & session & formFields( 'postid.as[Int], 'replytoid.as[Int]?, 'text )) {
			(session, postid, replytoid, text) => complete( Application.comment(session, postid, replytoid, text) ) } ~
		(post & path( "comment" ) & formFields( 'name, 'email?, 'url, 'postid.as[Int], 'replytoid.as[Int]?, 'text )) {
 			(name, email, url, postid, replytoid, text) => complete( Application.comment(name, email, url, postid, replytoid, text) ) } ~
		//
		// API routes
		//
		pathPrefix( "api"/"v1" ) {
			(get & path("domains"/Segment)) {
				d => complete( API.domainsGet(d) ) } ~
			(get & path("blogs") & blog) {
				b => complete( b ) } ~
			(get & path("blogs"/Segment)) {
				d => complete( API.blogsGet(d) ) } ~
			(post & path("blogs") & entity(as[BlogJson])) {
				b => complete( API.blogsPost(b) ) } ~
			(get & path("category"/IntNumber)) {
				categoryid => complete( API.category(categoryid) ) } ~
			(get & path("categories") & blog) {
				b => complete( API.categories(b) ) } ~
			(get & path("links") & blog) {
				b => complete( API.links(b) ) } ~
			(get & path("posts"/IntNumber)) {
				postid => complete( API.postsGet(postid) ) } ~
			(get & path("posts") & blog) {
				b => complete( API.postsGet(b) ) } ~
			(post & path("posts") & admin & entity(as[PostJson])) {
				(b, u, p) => complete( API.postsPost(b, u, p) ) } ~
			(post & path("posts"/IntNumber) & admin & entity(as[PostJson])) {
				(pid, _, _, p) => complete( API.postsPost(pid, p) ) } ~
			(delete & path("posts"/IntNumber)) {
				postid => complete( API.postsDelete(postid) ) } ~
			(get & path("recent"/IntNumber) & blog) {
				(count, b) => complete( API.recent(b, count) ) } ~
			(get & path("users"/IntNumber)) {
				userid => complete( API.usersGet(userid) ) } ~
			(post & path("users") & entity(as[UserJson])) {
				u => complete( API.usersPost(u) ) } ~
// 			(get & path("users"/Segment)) {
// 				email => complete( API.users(email) ) } ~
// 			(get & path("users")) {
// 				complete( API.users ) } ~
			(path("comments"/IntNumber)) { postid => 
				(get & complete( API.comments(postid) )) ~
				(post & formFields('authorid.as[Int]?, 'name?, 'email?, 'url, 'replytoid.as[Int]?, 'text)) {
					(authorid, name, email, url, replytoid, text) =>
						complete( API.comments(postid, authorid, name, email, url, replytoid, text) ) } }
		}
	}
}
