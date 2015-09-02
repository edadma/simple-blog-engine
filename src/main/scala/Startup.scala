package xyz.hyperreal.blog

import slick.driver.H2Driver.api._
import slick.jdbc.meta.MTable

import com.typesafe.config.ConfigFactory
import com.github.kxbmap.configs._

import concurrent.ExecutionContext.Implicits.global

import org.joda.time.Instant

import dao._


object Startup {
	
	if (await(db.run(MTable.getTables)) isEmpty) {
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
	}
	
	val conf = ConfigFactory.load
	
	if (conf hasPath "blog.init") {
		for (ou <- conf.opt[List[Map[String, String]]]("blog.init.users"); u <- ou)
			await( Users.create(u("name"), u("email"), u("password"), None, None, u.get("url")) )
		
		for (ob <- conf.opt[List[Map[String, String]]]("blog.init.blogs"); b <- ob)
			Blogs.create( b("domain"), b("head"), b("title"), b("subtitle"), b("description"), b("footer") )
		
		for (or <- conf.opt[List[Map[String, String]]]("blog.init.roles"); r <- or)
			Roles.create( r("blogid").toInt, r("userid").toInt, r("role") )
		
		for (oc <- conf.opt[List[Map[String, String]]]("blog.init.categories"); c <- oc)
			Categories.create( c("blogid").toInt, c("name"), c("description") )
	}
	
}