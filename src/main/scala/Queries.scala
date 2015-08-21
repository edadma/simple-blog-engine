package xyz.hyperreal.blog

import slick.driver.H2Driver.api._

import com.github.tototoshi.slick.H2JodaSupport._

import org.joda.time.{DateTime, Instant}

import concurrent._
import concurrent.ExecutionContext.Implicits.global
import collection.mutable.{HashSet, ListBuffer}

import dao._


object Queries {
	
	def toMonth( time: Instant ) = time.toDateTime withDayOfMonth 1 withTime (0, 0, 0, 0)
	
// 	def findBlog( domain: String ) =
// 		db.run( Blogs.find(domain) join Users on (_.ownerid === _.id) result ) map (_.headOption map {
// 			case (b, u) => models.Blog(b.id.get, u.name, b.domain, b.title, b.subtitle, b.description, b.footer)
// 	})
	
	def findPostsBefore( blogid: Int, before: Instant, limit: Int ) = dbrun( Posts.findBefore(blogid, before) join Users on (_.authorid === _.id)
		take limit result ) map {case (p, u) => models.Post.from(p, u)}
	
	def findRecent( blogid: Int, limit: Int ) = dbrun( Posts.findByBlogid(blogid) take limit map (p => (p.id, p.title)) result )
	
	def existsPostBefore( blogid: Int, before: Instant ) = dbrun( (Posts.findBefore(blogid, before) exists) result )
	
	def existsPostAfter( blogid: Int, after: Instant ) = dbrun( (Posts.findAfter(blogid, after) exists) result )

	def findArchives( blogid: Int ): Seq[DateTime] = {
		val set = new HashSet[DateTime]
		val buf = new ListBuffer[DateTime]
		
		await( db.stream(Posts.findByBlogid(blogid) map (_.date) result) foreach { d =>
			val month = toMonth( d )
			
			if (!(set contains month)) {
				set += month
				buf += month
			}
		} )
		buf.toList
	}
	
	def findCategories( postid: Int ) = dbrun( Categorizations.find(postid) join Categories on (_.categoryid === _.id) map {
		case (_, c) => (c.id, c.name)} result )
	
	def findAllCategories( blogid: Int ) = await( Categories.findByBlogid(blogid) ) map (c => (c.id.get, c.name))
	
	def findAllLinks( blogid: Int ) = await( Links.findByBlogid(blogid) ) map (l => (l.url, l.text))
	
	def findCommentsNoReply( postid: Int ) = db.stream( Comments.findByPostid(postid) result )
	
	def findCommentsReplies( postid: Int, replyto: Int ) = db.stream( Comments.findByPostid(postid, replyto) result )
	
	def findComments( postid: Int ) = {
		var count = 0
		
		def replies( replyto: Int ): Seq[models.CommentWithReplies] = {
			val comments = new ListBuffer[models.CommentWithReplies]
		
			await( findCommentsReplies( postid, replyto ) foreach { c =>
				val comment =
					c.authorid match {
						case Some( authorid ) => models.Comment.from( c, await(Users.find(authorid)).get )
						case None => models.Comment.from( c )
					}
				
				comments += models.CommentWithReplies(comment, replies( comment.id ))
				count += 1
				} )
			comments.toList
		}
		
		val comments = new ListBuffer[models.CommentWithReplies]
		
		await( findCommentsNoReply(postid) foreach { c =>
			val comment =
				c.authorid match {
					case Some( authorid ) => models.Comment.from( c, await(Users.find(authorid)).get )
					case None => models.Comment.from( c )
				}
		
			comments += models.CommentWithReplies(comment, replies( comment.id ))
			count += 1
			} )
		(comments.toList, count)
	}
	
	def findUser( userid: Int ) = await(Users.find(userid)) map (models.User.from( _ ))

}