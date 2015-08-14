package ca.hyperreal.blog

import slick.driver.H2Driver.api._
import slick.dbio.{DBIOAction, NoStream}


package object dao {
	
	val db = Database.forConfig( "blog.db" )
	
	def dbrun[R]( a: DBIOAction[R, NoStream, Nothing] ) = await( db.run(a) )
	
}