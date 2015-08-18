package xyz.hyperreal

import slick.driver.H2Driver.api._

import concurrent._
import concurrent.duration._


package object blog {

	def await[T]( a: Awaitable[T] ) = Await.result( a, Duration.Inf )
	
	def limitName( name: String, limit: Int, append: String = "..." ) = if (name.length > limit) name.substring(0, limit) + append else name
}