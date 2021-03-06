package xyz.hyperreal.blog

import spray.http.{StatusCodes, HttpResponse, HttpHeaders}
import spray.routing.directives.RouteDirectives._

import org.joda.time.Instant

import in.azeemarshad.common.sessionutils.Session
import in.azeemarshad.common.sessionutils.SessionDirectives

import concurrent.ExecutionContext.Implicits.global
import collection.mutable.ListBuffer


object Application extends SessionDirectives {
	
	def index( blog: dao.Blog, user: Option[models.User] ) = {
		val posts = Queries.findPostsBefore( blog.id.get, Instant.now, 10 )
		
		Views.blog( blog, user,
			!posts.isEmpty && Queries.existsPostAfter(blog.id.get, posts.head.date),
			!posts.isEmpty && Queries.existsPostBefore(blog.id.get, posts.last.date),
			Queries.findRecent( blog.id.get, 5 ),
			Queries.findAllCategories( blog.id.get ),
			Queries.findArchives( blog.id.get ),
			Queries.findAllLinks( blog.id.get ),
			posts map { p => Queries.findComments( p.id ) match {case (comments, count) => (p, comments, count)} }
			)
	}
	
	def login( blog: dao.Blog ) = Views.login( blog )
	
	def authenticate( email: String, password: String ) = {
		await( dao.Users.find(email, password) ) match {
			case Some( u ) =>
				setSession( "id" -> u.id.get.toString ) & redirect( "/", StatusCodes.SeeOther )
			case None =>
				redirect( "/", StatusCodes.SeeOther )
		}
	}
	
	def authorPost( blog: dao.Blog, user: models.User ) = Views.authorPost( blog, user )
	
	def register = Views.register
	
	def post( blog: dao.Blog, user: models.User, category: Int, headline: String, text: String ) = {
		dao.Posts.create( blog.id.get, user.id, headline, text, Instant.now ) map (dao.Categorizations.create( _, category ))
		redirectResponse( "/" )
	}
	
	def comment( session: Session, postid: Int, replytoid: Option[Int], text: String ) = {
		dao.Comments.create( postid, Some(session.data("id").toInt), None, None, None, Instant.now, replytoid, text )
		redirectResponse( "/" )
	}
	
	def comment( name: String, email: Option[String], url: String, postid: Int, replytoid: Option[Int], text: String ) = {
		dao.Comments.create( postid, None, Some(name), email, if (url == "") None else Some(url), Instant.now, replytoid, text )
		redirectResponse( "/" )
	}
	
	private def noSuchBlog( domain: String ) = Views.main( "Not a blog: " + domain )()(
		<xml:group>
			<h1>Not a blog</h1>
			<p>This system does not know of a blog under the domain: <code>{domain}</code></p>
		</xml:group>
	)
		
	private def redirectResponse( uri: String ) = HttpResponse( status = StatusCodes.SeeOther, headers = List(HttpHeaders.Location(uri)) )
	
}
