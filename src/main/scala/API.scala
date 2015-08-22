package xyz.hyperreal.blog

import spray.http.{StatusCodes, HttpResponse, HttpHeaders}
import spray.routing.directives.RouteDirectives._

import org.joda.time.Instant

import in.azeemarshad.common.sessionutils.Session
import in.azeemarshad.common.sessionutils.SessionDirectives

import concurrent.ExecutionContext.Implicits.global
import collection.mutable.ListBuffer
import concurrent.Future

import java.net.URLDecoder


object API extends SessionDirectives {
	
	def links( blog: dao.Blog ) = Queries.findAllLinks( blog.id.get )
	
	def categories( blog: dao.Blog ) = dao.Categories.findByBlogid( blog.id.get )
	
	def category( categoryid: Int ) = dao.Categories.find( categoryid )
	
	def post( postid: Int ) = dao.Posts.find( postid ) map (u => u map (models.Post.from(_)))
	
	def post( blog: dao.Blog, user: models.User, post: models.PostEntity ) =
		dao.Posts.create( blog.id.get, user.id, post.title, post.content, Instant.now ) map { postid =>
			post.categories foreach (dao.Categorizations.create( postid, _ ))
			Map( "postid" -> postid )
		}
	
	def recent( blog: dao.Blog, limit: Int ) = Queries.findRecent( blog.id.get, limit )
	
	def posts( blog: dao.Blog, limit: Int ) = Queries.findPostsBefore( blog.id.get, Instant.now, limit )
	
	def users( userid: Int ) = dao.Users.find(userid) map (u => u map (models.User.from(_)))
	
	def users( email: String ) = dao.Users.find(URLDecoder.decode(email, "UTF-8")) map (u => u map (models.User.from(_)))
	
	def users = dao.Users.list map (u => u map (models.User.from(_)))
	
	def comments( postid: Int ) = Queries.findComments( postid )
	
	def comments( postid: Int, authorid: Option[Int], name: Option[String], email: Option[String], url: String, replyto: Option[Int], content: String ) =
		dao.Comments.create( postid, authorid, name, email, if (url == "") None else Some(url), Instant.now, replyto, content ) map (id => Map( "id" -> id ))
	
}