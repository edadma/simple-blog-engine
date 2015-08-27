package xyz.hyperreal.blog.dao

import slick.driver.H2Driver.api._

import com.github.tototoshi.slick.H2JodaSupport._

import org.joda.time.Instant

import spray.json.DefaultJsonProtocol._

import concurrent._
import concurrent.ExecutionContext.Implicits.global


case class User(
	name: String,
	email: String,
	password: String,
	avatar: Option[Array[Byte]],
	thumb: Option[Array[Byte]],
	bio: Option[String],
	url: Option[String],
	registered: Instant,
	id: Option[Int] = None
)

class UsersTable(tag: Tag) extends Table[User](tag, "users") {
	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
	def name = column[String]("name")
	def email = column[String]("email")
	def password = column[String]("password")
	def avatar = column[Option[Array[Byte]]]("avatar")
	def thumb = column[Option[Array[Byte]]]("thumb")
	def bio = column[Option[String]]("bio")
	def url = column[Option[String]]("url")
	def registered = column[Instant]("registered")
	
	def * = (name, email, password, avatar, thumb, bio, url, registered, id.?) <> (User.tupled, User.unapply)
	def idx_users_email = index("idx_users_email", email, unique = true)
	def idx_users_email_password = index("idx_users_email_password", (email, password), unique = true)
}

object Users extends TableQuery(new UsersTable(_)) {
	def find(id: Int): Future[Option[User]] = db.run( filter(_.id === id).result ) map (_.headOption)

	def find( email: String ) = db.run( filter(_.email === email).result ) map (_.headOption)

	def find( email: String, password: String ) = db.run( filter(r => r.email === email && r.password === password).result ) map (_.headOption)
	
	def create( name: String, email: String, password: String, avatar: Option[Array[Byte]], bio: Option[String], url: Option[String] ) =
		db.run( this returning map(_.id) += User(name, email, password, avatar, avatar, bio, url, Instant.now) )

	def delete(id: Int): Future[Int] = {
		db.run(filter(_.id === id).delete)
	}
	
	def list: Future[Seq[User]] = db.run(this.result)
}

case class Role(
	blogid: Int,
	userid: Int,
	role: String
)

object Role {
	implicit val blog = jsonFormat3(Role.apply)
}

class RolesTable(tag: Tag) extends Table[Role](tag, "roles") {
	def blogid = column[Int]("blogid")
	def userid = column[Int]("userid")
	def role = column[String]("role")
	
	def * = (blogid, userid, role) <> (Role.apply _ tupled, Role.unapply)
	def pk = primaryKey("pk_roles", (blogid, userid))
	def idx_roles_userid = index("idx_roles_userid", userid)
}

object Roles extends TableQuery(new RolesTable(_)) {
	def find(userid: Int): Future[Seq[Role]] = db.run( filter(_.userid === userid) result )

	def create( blogid: Int, userid: Int, role: String ) = db.run( this += Role(blogid, userid, role) )

	def delete(userid: Int, blogid: Int): Future[Int] = {
		db.run(filter(r => r.userid === userid && r.blogid === blogid).delete)
	}
	
	def list: Future[Seq[Role]] = db.run(this.result)
}

case class Blog(
	ownerid: Int,
	domain: String,
	title: String,
	subtitle: String,
	description: String,
	footer: String,
	id: Option[Int] = None
)

object Blog {
	implicit val blog = jsonFormat7(Blog.apply)
}

class BlogsTable(tag: Tag) extends Table[Blog](tag, "blogs") {
	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
	def ownerid = column[Int]("ownerid")
	def domain = column[String]("domain")
	def title = column[String]("title")
	def subtitle = column[String]("subtitle")
	def description = column[String]("description")
	def footer = column[String]("footer")
	
	def * = (ownerid, domain, title, subtitle, description, footer, id.?) <> (Blog.apply _ tupled, Blog.unapply)
}

object Blogs extends TableQuery(new BlogsTable(_)) {
	def find(id: Int): Future[Option[Blog]] = db.run( filter(_.id === id) result ) map (_.headOption)
	
	def find(domain: String) = db.run( filter(_.domain === domain) result ) map (_.headOption)

	def create(
			ownerid: Int,
			domain: String,
			title: String,
			subtitle: String,
			description: String,
			footer: String ) = {
		db.run( this returning map(_.id) += Blog(ownerid, domain, title, subtitle, description, footer) )
	}

	def delete(id: Int): Future[Int] = {
		db.run(filter(_.id === id).delete)
	}
	
	def list: Future[Seq[Blog]] = db.run(this.result)
}

case class Post(
	blogid: Int,
	authorid: Int,
	title: String,
	content: String,
	date: Instant,
	id: Option[Int] = None
)

class PostsTable(tag: Tag) extends Table[Post](tag, "posts") {
	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
	def blogid = column[Int]("blogid")
	def authorid = column[Int]("authorid")
	def title = column[String]("title")
	def content = column[String]("content")
	def date = column[Instant]("date")
	
	def * = (blogid, authorid, title, content, date, id.?) <> (Post.tupled, Post.unapply)
}

object Posts extends TableQuery(new PostsTable(_)) {
	def find(id: Int): Future[Option[Post]] = db.run( filter(_.id === id) result ) map (_.headOption)

	def create( blogid: Int, authorid: Int, title: String, content: String, date: Instant ): Future[Int] = {
		db.run(this returning map(_.id) += Post(blogid, authorid, title, content, date))
	}

	def delete(id: Int): Future[Int] = {
		db.run(filter(_.id === id).delete)
	}
	
	def update( id: Int, title: String, content: String ) = db.run( filter(_.id === id) map (p => (p.title, p.content)) update (title, content) )
	
	def list: Future[Seq[Post]] = db.run(this.result)

	def findByBlogid( blogid: Int ) = filter (_.blogid === blogid) sortBy (_.date.desc)

	def findBefore( blogid: Int, before: Instant ) = findByBlogid( blogid ) filter (_.date < before)

	def findAfter( blogid: Int, after: Instant ) = findByBlogid( blogid ) filter (_.date > after)
	
}

case class Category(
	blogid: Int,
	name: String,
	description: String,
	id: Option[Int] = None
)

object Category {
	implicit val blog = jsonFormat4(Category.apply)
}

class CategoriesTable(tag: Tag) extends Table[Category](tag, "categories") {
	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
	def blogid = column[Int]("blogid")
	def name = column[String]("name")
	def description = column[String]("description")
	
	def * = (blogid, name, description, id.?) <> (Category.apply _ tupled, Category.unapply)
	def idx_categories_blogid = index("idx_categories_blogid", blogid)
	def idx_categories_name = index("idx_categories_name", name)
	def idx_categories_blogid_name = index("idx_categories_blogid_name", (blogid, name), unique = true)
}

object Categories extends TableQuery(new CategoriesTable(_)) {
	def find(id: Int): Future[Option[Category]] = db.run( filter(_.id === id) result ) map (_.headOption)

	def findByBlogid(blogid: Int): Future[Seq[Category]] = db.run( filter (_.blogid === blogid) sortBy (_.name.asc) result )

	def create(
			blogid: Int,
			name: String,
			description: String
		) = 	db.run( this returning map(_.id) += Category(blogid, name, description) )

	def delete(blogid: Int, name: String): Future[Int] = {
		db.run(filter(r => r.blogid === blogid && r.name === name).delete)
	}
	
	def list: Future[Seq[Category]] = db.run(this.result)
}

case class Categorization(
	postid: Int,
	categoryid: Int
)

class CategorizationsTable(tag: Tag) extends Table[Categorization](tag, "categorizations") {
	def postid = column[Int]("postid")
	def categoryid = column[Int]("categoryid")
	
	def * = (postid, categoryid) <> (Categorization.tupled, Categorization.unapply)
	def pk = primaryKey("pk_categorizations", (postid, categoryid))
}

object Categorizations extends TableQuery(new CategorizationsTable(_)) {
	def find(postid: Int) = filter(_.postid === postid)

	def create( postid: Int, categoryid: Int ) {
		db.run(this += Categorization(postid, categoryid))
	}

	def delete(postid: Int, categoryid: Int): Future[Int] = {
		db.run(filter(r => r.postid === postid && r.categoryid === categoryid).delete)
	}
	
	def list: Future[Seq[Categorization]] = db.run(this.result)
}

case class Comment(
	postid: Int,
	authorid: Option[Int],
	name: Option[String],
	email: Option[String],
	url: Option[String],
	date: Instant,
	replyto: Option[Int],
	content: String,
	id: Option[Int] = None
)

class CommentsTable(tag: Tag) extends Table[Comment](tag, "comments") {
	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
	def postid = column[Int]("postid")
	def authorid = column[Option[Int]]("authorid")
	def name = column[Option[String]]("name")
	def email = column[Option[String]]("email")
	def url = column[Option[String]]("url")
	def date = column[Instant]("date")
	def replyto = column[Option[Int]]("replyto")
	def content = column[String]("content")
	
	def * = (postid, authorid, name, email, url, date, replyto, content, id.?) <> (Comment.tupled, Comment.unapply)
}

object Comments extends TableQuery(new CommentsTable(_)) {
	def find(id: Int): Future[Option[Comment]] = db.run( filter(_.id === id) result ) map (_.headOption)

	def findByPostid(postid: Int) = filter (r => r.postid === postid && r.replyto.isEmpty) sortBy (_.date.asc)

	def findByPostid(postid: Int, replyto: Int) = filter (r => r.postid === postid && r.replyto === replyto) sortBy (_.date.asc)

	def create(
			postid: Int,
			authorid: Option[Int],
			name: Option[String],
			email: Option[String],
			url: Option[String],
			date: Instant,
			replyto: Option[Int],
			content: String ) = db.run( this returning map(_.id) += Comment(postid, authorid, name, email, url, date, replyto, content) )

	def delete(postid: Int): Future[Int] = {
		db.run(filter(_.postid === postid).delete)
	}
	
	def list: Future[Seq[Comment]] = db.run(this.result)
}

case class Link(
	blogid: Int,
	order: Int,
	url: String,
	text: String,
	id: Option[Int] = None
)

class LinksTable(tag: Tag) extends Table[Link](tag, "links") {
	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
	def blogid = column[Int]("blogid")
	def order = column[Int]("order")
	def url = column[String]("url")
	def text = column[String]("text")
	
	def * = (blogid, order, url, text, id.?) <> (Link.tupled, Link.unapply)
}

object Links extends TableQuery(new LinksTable(_)) {
	def find(id: Int): Future[Option[Link]] = db.run( filter(_.id === id) result ) map (_.headOption)

	def findByBlogid( blogid: Int ): Future[Seq[Link]] = db.run( filter (_.blogid === blogid) sortBy (_.order.asc) result )

	def create( blogid: Int, order: Int, url: String, text: String ) = db.run( this returning map(_.id) += Link(blogid, order, url, text) )

	def delete(id: Int): Future[Int] = {
		db.run(filter(_.id === id).delete)
	}
	
	def list: Future[Seq[Link]] = db.run(this.result)
}

case class Sidebar(
	blogid: Int,
	title: String,
	content: String,
	id: Option[Int] = None
)

class SidebarsTable(tag: Tag) extends Table[Sidebar](tag, "sidebars") {
	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
	def blogid = column[Int]("blogid")
	def title = column[String]("title")
	def content = column[String]("content")
	
	def * = (blogid, title, content, id.?) <> (Sidebar.tupled, Sidebar.unapply)
}

object Sidebars extends TableQuery(new SidebarsTable(_)) {
	def find(id: Int): Future[Option[Sidebar]] = db.run( filter(_.id === id) result ) map (_.headOption)

	def findByBlogid( blogid: Int ): Future[Seq[Sidebar]] = db.run( filter (_.blogid === blogid) result )

	def create( blogid: Int, title: String, content: String ) = db.run( this returning map(_.id) += Sidebar(blogid, title, content) )

	def delete(id: Int): Future[Int] = {
		db.run(filter(_.id === id).delete)
	}
	
	def list: Future[Seq[Sidebar]] = db.run(this.result)
}

case class Media(
	postid: Int,
	data: Array[Byte],
	mime: String,
	id: Option[Int] = None
)

class MediasTable(tag: Tag) extends Table[Media](tag, "medias") {
	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
	def postid = column[Int]("postid")
	def data = column[Array[Byte]]("data")
	def mime = column[String]("mime")
	
	def * = (postid, data, mime, id.?) <> (Media.tupled, Media.unapply)
}

object Medias extends TableQuery(new MediasTable(_)) {
	def find(id: Int): Future[Option[Media]] = db.run( filter(_.id === id) result ) map (_.headOption)

	def findByPostid(postid: Int) = filter (_.postid === postid)

	def create(postid: Int, data: Array[Byte], mime: String) = db.run( this returning map(_.id) += Media(postid, data, mime) )

	def delete(postid: Int): Future[Int] = {
		db.run(filter(_.postid === postid).delete)
	}
	
	def list: Future[Seq[Media]] = db.run(this.result)
}
