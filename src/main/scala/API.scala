package xyz.hyperreal.blog

import com.typesafe.config.ConfigFactory
import com.github.kxbmap.configs._

import spray.http.{StatusCodes, HttpResponse, HttpHeaders, HttpEntity}
import spray.routing.directives.RouteDirectives._

import org.joda.time.Instant

import in.azeemarshad.common.sessionutils.Session
import in.azeemarshad.common.sessionutils.SessionDirectives

import concurrent.ExecutionContext.Implicits.global
import collection.mutable.ListBuffer
import concurrent.Future

import java.net.URLDecoder

import dao._


object API extends SessionDirectives {
	
  val conf = ConfigFactory.load
	val reserved = conf.opt[List[String]]( "blog.domain.reserved" )

	def visitsCount( blog: Blog ) = Queries.visitsCount( blog.id.get ) map (c => Map( "count" -> c ))
	
	def visits( blog: Blog ) = Queries.visits( blog.id.get )
	
	def domainsGet( domain: String ) =
		if (reserved.get exists (_ == domain))
			Future( Map("available" -> false) )
		else
			Blogs.find( domain ) map (u => Map( "available" -> (u == None) ))
		
	def blogsGet( domain: String ) = Blogs.find( domain )
	
	def blogsPost( blog: models.BlogJson ) = {
		Blogs.create( blog.domain, "", blog.title, blog.subtitle, blog.description, blog.footer ) map 
			{ id =>
				for (c <- blog.categories split "," map (_.trim) filter (_ != "") distinct)
					Categories.create( id, c )
					
				Map( "id" -> id )
			}
	}
	
	def links( blog: Blog ) = Queries.findAllLinks( blog.id.get )
	
	def categories( blog: Blog ) = Categories.findByBlogid( blog.id.get )
	
	def category( categoryid: Int ) = Categories.find( categoryid )
	
	def postsGet( postid: Int ) = Posts.find( postid ) map (u => u map (models.Post.from(_)))
	
	def postsPost( blog: Blog, user: models.User, post: models.PostJson ) =
		Posts.create( blog.id.get, user.id, post.title, post.content, Instant.now, post.status, post.commenting ) map { postid =>
			post.categories foreach (Categorizations.create( postid, _ ))
			Map( "postid" -> postid )
		}
	
	def postsPost( pid: Int, post: models.PostJson ) = {
		val categorizations: Seq[Int] = Queries.findCategorizations( pid )
		
		post.categories filterNot (categorizations contains _) foreach (Categorizations.create( pid, _ ))
		categorizations filterNot (post.categories contains _) foreach (Categorizations.delete( pid, _ ))
		
		Posts.update( pid, post.title, post.content, post.status, post.commenting ) map (u => Map( "updated" -> u ))
	}
	
	def postsDelete( postid: Int ) = Posts.delete( postid ) map (u => Map( "deleted" -> u ))

//	def recent( blog: Blog, limit: Int ) = Queries.findRecent( blog.id.get, limit )
	
	def postsGet( blog: Blog ) = Queries.findPosts( blog.id.get )
	
	def recent( blog: Blog, limit: Int ) = Queries.findPostsBefore( blog.id.get, Instant.now, limit )
	
	def usersGet( userid: Int ) = Users.find(userid) map (u => u map (models.User.from(_)))
	
	def usersPost( u: models.UserJson ) = {
		Users.find( u.email ) flatMap {
			case None =>
				Users.create( u.name, u.email, u.password, None, u.bio, u.url ) flatMap {
					id =>
						val response = HttpResponse( status = StatusCodes.Created, s"""{"id": $id}""" )
						
						u.role match {
							case Some( r ) =>
								Roles.create( r.blogid, id, r.role ) map (_ => response)
							case None => Future( response )
						}
				}
			case _ => Future( HttpResponse(status = StatusCodes.Conflict, "A user with that email address already exists.") )
		}
	}
	
	//def users( email: String ) = Users.find(URLDecoder.decode(email, "UTF-8")) map (u => u map (models.User.from(_)))
	
	//def users = Users.list map (u => u map (models.User.from(_)))
	
	def comments( postid: Int ) = Queries.findComments( postid )
	
	def comments( postid: Int, authorid: Option[Int], name: Option[String], email: Option[String], url: String, replyto: Option[Int], content: String ) =
		Comments.create( postid, authorid, name, email, if (url == "") None else Some(url), Instant.now, replyto, content ) map (id => Map( "id" -> id ))
	
}
