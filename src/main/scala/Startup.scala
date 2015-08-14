package ca.hyperreal.blog

import slick.driver.H2Driver.api._

import com.typesafe.config.ConfigFactory
import com.github.kxbmap.configs._

import concurrent.ExecutionContext.Implicits.global

import org.joda.time.Instant

import dao._


object Startup {
	
	await(db.run(DBIO.seq(
		(
			Users.schema ++
			Blogs.schema ++
			Categories.schema ++
			Roles.schema ++
			Posts.schema ++
			Categorizations.schema ++
			Comments.schema ++
			Links.schema
		).create
	)))
	
	val conf = ConfigFactory.load
	
	for (u <- conf.get[List[Map[String, String]]]("blog.init.users"))
		Users.create( u("name"), u("email"), u("password"), None, None, None )
	
	for (b <- conf.get[List[Map[String, String]]]("blog.init.blogs"))
		Blogs.create( b("ownerid").toInt, b("domain"), b("title"), b("subtitle"), b("description"), b("footer") )
	
	for (r <- conf.get[List[Map[String, String]]]("blog.init.roles"))
		Roles.create( r("blogid").toInt, r("userid").toInt, r("role") )
	
	for (c <- conf.get[List[Map[String, String]]]("blog.init.categories"))
		Categories.create( c("blogid").toInt, c("name"), c("description") )
	
}