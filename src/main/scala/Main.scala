package xyz.hyperreal.blog

import akka.actor.ActorSystem

import spray.routing._
import spray.http._
import spray.json._
import spray.json.DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport._
import MediaTypes._

import in.azeemarshad.common.sessionutils.SessionDirectives

import concurrent.duration._
import util.{Success, Failure}


object Main extends App with SimpleRoutingApp with SessionDirectives {

	Startup
	
	implicit val system = ActorSystem("on-spray-can")
	implicit val context = system.dispatcher

	startServer(interface = args(0), port = 8080) {
	
		def blog[T]( domain: String, result: dao.Blog => T ) = await( dao.Blogs.find(domain) ) map result
		
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
		(get & pathSingleSlash & hostName & optionalSession) {
			(h, session) => complete( Application.index(h, session) ) } ~
		(get & path( "author"/IntNumber ) & hostName & optionalSession) {
			(id, h, session) => complete(Application.index( h, session )) } ~
		// 		(get & path( "category"/IntNumber ) & hostName) {
		// 			(id, host) => complete(Application.index( "localhost" )) } ~
		// 		(get & path( "uncategorized" ) & hostName) {
		// 			host => complete( "uncategorized" ) } ~
		// 		(get & path( "b"/LongNumber ) & hostName) {
		// 			(time, host) => complete(Application.index( "localhost" )) } ~
		// 		(get & path( "a"/LongNumber ) & hostName) {
		// 			(time, host) => complete(Application.index( "localhost" )) } ~
		// 		(get & path( IntNumber ) & hostName) {
		// 			(post, host) => complete( (post, host) ) } ~
		// 		(get & path( IntNumber~"-"~IntNumber ) & hostName) {
		// 			(year, month, host) => complete( (year, month, host) ) } ~
		// 		(get & path( IntNumber~"-"~IntNumber~"-"~IntNumber ) & hostName) {
		// 			(year, month, day, host) => complete( (year, month, day, host) ) } ~
		(path( "login" ) & hostName) { host =>
			(get & optionalSession) {
				session =>
					if (session != None)
						redirect( "/", StatusCodes.SeeOther )
					else
						complete( Application.login(host) ) } ~
			(post & formFields( 'email, 'password, 'rememberme ? "no" )) {
				(email, password, rememberme) => Application.authenticate( host, email, password ) } } ~
		(get & path( "register" ) & hostName) {
			h => complete( blog(h, _ => Application.register) ) } ~
		(get & path( "admin" ) & hostName & session) {
			(host, session) => complete( Application.admin(host, session) ) } ~
		(post & path( "post" ) & session & formFields( 'category.as[Int], 'headline, 'text, 'blogid.as[Int] )) {
			(session, category, headline, text, blogid) => complete( Application.post(session, category, headline, text, blogid) ) } ~
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
			(get & path("blog") & hostName) {
				h => complete( blog(h, b => b) ) } ~
			(get & path("category"/IntNumber) & hostName) {
				(categoryid, h) => complete( blog(h, _ => API.category(categoryid)) ) } ~
			(get & path("categories") & hostName) {
				h => complete( blog(h, API.categories) ) } ~
			(get & path("links") & hostName) {
				h => complete( blog(h, API.links) ) } ~
			(get & path("post"/IntNumber) & hostName) {
				(postid, h) => complete( blog(h, _ => API.post(postid)) ) } ~
			(get & path("recent"/IntNumber) & hostName) {
				(count, h) => complete( blog(h, b => API.recent(b, count)) ) } ~
			(get & path("posts"/IntNumber) & hostName) {
				(count, h) => complete( blog(h, b => API.posts(b, count)) ) } ~
			(get & path("users"/IntNumber) & hostName) {
				(userid, h) => complete( blog(h, _ => API.users(userid)) ) } ~
			(get & path("users"/Segment) & hostName) {
				(email, h) => complete( blog(h, _ => API.users(email)) ) } ~
			(get & path("users") & hostName) {
				h => complete( blog(h, _ => API.users) ) } ~
			(path("comments"/IntNumber) & hostName) { (postid, h) => 
				(get & complete( blog(h, _ => API.comments(postid)) )) ~
				(post & formFields('authorid.as[Int]?, 'name?, 'email?, 'url, 'replytoid.as[Int]?, 'text)) {
					(authorid, name, email, url, replytoid, text) =>
						complete( blog(h, _ => API.comments(postid, authorid, name, email, url, replytoid, text)) ) } }
		}
	}
}