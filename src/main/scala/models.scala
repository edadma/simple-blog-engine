package xyz.hyperreal.blog

package models

import spray.json.DefaultJsonProtocol._
import spray.json._
import spray.httpx.unmarshalling.MalformedContent

import org.joda.time.Instant


object Implicits {
	
	implicit object InstantJsonFormat extends JsonFormat[Instant] {
		def write(x: Instant) = JsObject(Map("millis" -> JsNumber(x.getMillis), "string" -> JsString(Views.postDateFormat.print(x))))
		def read(value: JsValue) = value match {
			case JsObject(x) => new Instant(x("millis").asInstanceOf[JsNumber].value.longValue)
			case x => sys.error("Expected Instant as JsObject, but got " + x)
		}
	}

}

import Implicits._

case class User(
	id: Int,
	name: String,
	email: String,
	roles: Seq[dao.Role],
	avatar: Option[String],
	thumb: Option[String],
	bio: Option[String],
	url: Option[String],
	registered: Instant
) {
	def is( blogid: Int, role: String ) = roles exists (r => r.blogid == blogid && r.role == role)
}

object User {
	implicit val user = jsonFormat9( User.apply )
	
	def from( u: dao.User ) = User( u.id.get, u.name, u.email, await(dao.Roles.find(u.id.get)), u.avatar map (_ => s"/api/v1/users/${u.id.get}/avatar"),
																	u.thumb map (_ => s"/api/v1/users/${u.id.get}/thumb"), u.bio, u.url, u.registered )
}

// case class Blog(
// 	id: Int,
// 	owner: String,
// 	domain: String,
// 	title: String,
// 	subtitle: String,
// 	description: String,
// 	footer: String
// )
// 
// object Blog {
// 	implicit val blog = jsonFormat7(Blog.apply)
// }

case class PostEntity(
	title: String,
	content: String,
	categories: Seq[Int]
)

object PostEntity {
	implicit val postEntity = jsonFormat3( PostEntity.apply )
}

case class Post(
	id: Int,
	blogid: Int,
	authorid: Int,
	author: String,
	title: String,
	content: String,
	date: Instant,
	categories: Map[String, Int]
)

object Post {

	implicit val post = jsonFormat8( Post.apply )

	def from( p: dao.Post ) = Post( p.id.get, p.blogid, p.authorid, await(dao.Users.find(p.authorid)).get.name, p.title, p.content,
																	p.date, Queries.findCategories(p.id.get) )
	
	def from( p: dao.Post, u: dao.User ) = Post( p.id.get, p.blogid, p.authorid, u.name, p.title, p.content,
																								p.date, Queries.findCategories(p.id.get) )
	
}

case class Comment(
	id: Int,
	postid: Int,
	authorid: Option[Int],
	author: String,
	url: Option[String],
	date: Instant,
	replyto: Option[Int],
	content: String
)

object Comment {
	implicit val comment = jsonFormat8( Comment.apply )
	
	def from( c: dao.Comment ) = Comment( c.id.get, c.postid, None, c.name.get, c.url, c.date, c.replyto, c.content )
	
	def from( c: dao.Comment, u: dao.User ) =
		c.authorid match {
			case None => Comment( c.id.get, c.postid, None, c.name.get, c.url, c.date, c.replyto, c.content )
			case authorid => Comment( c.id.get, c.postid, authorid, u.name, u.url, c.date, c.replyto, c.content )
		}
}

case class CommentWithReplies( comment: Comment, replies: Seq[CommentWithReplies] )

object CommentWithReplies {
	implicit val commentWithRepliesFormat: JsonFormat[CommentWithReplies] = lazyFormat(jsonFormat(CommentWithReplies.apply, "comment", "replies"))
}
