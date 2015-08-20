package xyz.hyperreal.blog

import in.azeemarshad.common.sessionutils.Session

import org.joda.time.{DateTime, Instant}
import org.joda.time.format.DateTimeFormat

import models._


object Views {
	val postDateFormat = DateTimeFormat.forPattern( "EEEE, MMMM d, yyyy" )
	val commentDateFormat = DateTimeFormat.forPattern( "MMMM d, yyyy" )
	val archivesTextDateFormat = DateTimeFormat.forPattern( "MMMM yyyy" )
	val archivesLinkDateFormat = DateTimeFormat.forPattern( "yyyy-MM" )
	
	def main( title: String )( head: xml.Node = xml.Group(Nil) )( content: xml.Node ) =
		<html lang="en">
			<head>
				<meta charset="utf-8"/>
				<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
				<meta name="viewport" content="width=device-width, initial-scale=1"/>
				
				<title>{title}</title>

				<link rel="shortcut icon" href="/favicon.ico"/>
				
				<link href="/webjars/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet"/>
				<link href="/webjars/bootstrap/3.3.5/css/bootstrap-theme.min.css" rel="stylesheet"/>
				
				{head}
			</head>
			
			<body>
				{content}
				
				<script src="/webjars/jquery/1.11.1/jquery.min.js"></script>
				<script src="/webjars/bootstrap/3.3.5/js/bootstrap.min.js"></script>
			</body>
		</html>
	
	def login( blog: dao.Blog ) = {
		main( blog.domain ) {
			<link href="/css/signin.css" rel="stylesheet"/>
		} {
			<div class="container">

				<form class="form-signin" action="/login" method="POST">
					<h2 class="form-signin-heading">Please sign in</h2>
					<label for="inputEmail" class="sr-only">Email address</label>
					<input type="email" name="email" id="inputEmail" class="form-control" placeholder="Email address" required="true" autofocus="true"/>
					<label for="inputPassword" class="sr-only">Password</label>
					<input type="password" name="password" id="inputPassword" class="form-control" placeholder="Password" required="true"/>
					<!-- <div class="checkbox">
						<label>
							<input type="checkbox" name="rememberme" value="yes"/> Remember me
						</label>
					</div> -->
					<button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
				</form>

			</div>
		}
	}
	
	def register =
		main( "Registration" ) {
			<xml:group>
				<link href="/css/register.css" rel="stylesheet"/>
				<script src="/webjars/angularjs/1.4.3/angular.min.js"></script>
				<script src="/coffee/register.js"></script>
			</xml:group>
		} {
			<div class="container">
				<form class="form-register" ng-submit="submit()" ng-controller="registrationFormCtrl">
					<h2 class="form-register-heading">Please register</h2>
					<div class="form-group">
						<input type="email" class="form-control" ng-model="email" placeholder="Email address*" required="true" autofocus="true"/></div>
					<div class="form-group">
						<input type="password" class="form-control" ng-model="password" placeholder="Password*" required="true"/></div>
					<div class="form-group">
						<input type="text" class="form-control" ng-model="name" placeholder="Name*" required="true"/></div>
					<div class="form-group">
						<input type="text" class="form-control" ng-model="url" placeholder="URL"/></div>
					<div class="form-group">
						<textarea class="form-control" rows="4" cols="50" ng-model="bio" placeholder="Bio"></textarea></div>
					<button type="submit" class="btn btn-lg btn-primary btn-block">Register</button>
				</form>
			</div>
		}
	
	def admin( blog: dao.Blog, session: Session ) =
		main( blog.domain )() {
			val user = await( dao.Users.find(session.data("id").toInt) ).get
			
			<div class="container">

				<form action="/post" method="POST">
					<h1>Welcome {user.name}</h1>
					<p>
						<select name="category">
							{
								for ((id, name) <- Queries.findAllCategories( blog.id.get ))
									yield <option value={id.toString}>{name}</option>
							}
						</select>
					</p>
					<p><input type="text" name="headline" placeholder="Headline"/></p>
					<p><textarea rows="4" cols="50" name="text" placeholder="Text"></textarea></p>
					<input type="hidden" name="blogid" value={blog.id.get.toString}/>
					<p class="submit"><input type="submit" value="Post"/></p>
				</form>
				
			</div>
		}
		
	def blog( session: Option[Session], b: dao.Blog, newer: Boolean, older: Boolean, recent: Seq[(Int, String)],
						categories: Seq[(Int, String)], archives: Seq[DateTime], links: Seq[(String, String)],
						posts: Seq[(Post, Seq[CommentWithReplies], Int)] ) =
		main( b.title ){
			<xml:group>
				<link href="/css/blog.css" rel="stylesheet"/>
				
				<script src="/webjars/angularjs/1.4.3/angular.min.js"></script>
				<script src="/coffee/blog.js"></script>
			</xml:group>
		} {
			<xml:group>
				<div class="blog-masthead">
					<div class="container">
						<nav class="blog-nav">
							<!-- <a class="blog-nav-item active" href="#">Home</a>
							<a class="blog-nav-item" href="#">Contact</a>
							<a class="blog-nav-item" href="#">About</a> -->
							{
								if (session != None)
									<a class="blog-nav-item navbar-right" href="/logout">Logout</a>
									<a class="blog-nav-item navbar-right" href="/admin">Admin</a>
									<a class="blog-nav-item navbar-right" href="/post">Post</a>
								else
									<a class="blog-nav-item navbar-right" href="/login">Sign in</a>
							}
						</nav>
					</div>
				</div>
				
				<div class="container" ng-app="blog">
				
					<div class="blog-header">
						<h1 class="blog-title">{b.title}</h1>
						<p class="lead blog-description">{xml.Unparsed(b.subtitle)}</p>
					</div>
					
					<div class="row">
					
						<div class="col-sm-9 blog-main">
							{
								if (posts isEmpty)
									<p>no posts</p>
								else
									posts map {case (p, c, count) => post(session, p, c, count)}
							}
							
							{
								if (newer || older)
									<nav>
										<ul class="pager">
											{if (older) <li><a href="#">&larr; Older</a></li>}
											{if (newer) <li><a href="#">Newer &rarr;</a></li>}
										</ul>
									</nav>
							}

						</div>
						
						<div class="col-sm-3 blog-sidebar">
							<div class="sidebar-module sidebar-module-inset">
								<h4>About</h4>
								<p>{b.description}</p>
							</div>
							
							<div class="sidebar-module">
								<h4>Recent Posts</h4>
								<ol class="list-unstyled">
									{for ((id, title) <- recent) yield <li><a href={"/" + id}>{title}</a></li>}
								</ol>
							</div>
							
							<div class="sidebar-module">
								<h4>Categories</h4>
								<ol class="list-unstyled">
									{for ((id, cat) <- categories) yield <li><a href={"/category/" + id}>{cat}</a></li>}
								</ol>
							</div>
							
							<div class="sidebar-module">
								<h4>Archives</h4>
								<ol class="list-unstyled">
									{for (a <- archives) yield <li><a href={"/" + archivesLinkDateFormat.print(a)}>{archivesTextDateFormat.print(a)}</a></li>}
								</ol>
							</div>
							
							<!-- <div class="sidebar-module">
								<h4>Links</h4>
								<ol class="list-unstyled">
									{for ((url, text) <- links) yield <li><a href={url}>{text}</a></li>}
								</ol>
							</div> -->
						</div>

					</div>
				</div>

				<footer class="blog-footer">
					{xml.Unparsed(b.footer)}
				</footer>
			</xml:group>
		}
	
	def post( session: Option[Session], p: Post, cs: Seq[CommentWithReplies], count: Int ) =
		<xml:group>
			<div class="blog-post">
				<h2 class="blog-post-title">{p.title}</h2>
				<p class="blog-post-meta">{postDateFormat.print(p.date)} by <a href={"/author/" + p.authorid}>{p.author}</a> in {
					if (p.categories isEmpty)
						<a href="/uncategorized">Uncategorized</a>
					else
						xml.Unparsed(p.categories map {case (id, cat) => """<a href="/category/""" + id + """">""" + cat + "</a>"} mkString (", "))
				} | <a href={s"#comments-${p.id}"}>{count} comment{if (count == 1) "" else "s"}</a></p>
				<div>
					{xml.Unparsed( p.content )}
				</div>
				{comments( session, p.id, cs, count )}
			</div>
		</xml:group>
	
	def comments( session: Option[Session], postid: Int, cs: Seq[CommentWithReplies], count: Int ): xml.Node = {
		def commentsLevel( cs: Seq[CommentWithReplies], level: Int ): Seq[xml.Node] = {
			for (CommentWithReplies(c, r) <- cs)
				yield {
					<xml:group>
						<div class={(if (level > 0) s"col-sm-offset-$level " else "") + "blog-comment"}>
							<h2 class="blog-comment-name">{if (c.url == None) c.author else <a href={c.url.get}>{c.author}</a>}</h2>
							<p class="blog-comment-meta">{commentDateFormat.print(c.date)}</p>
							<p>{c.content}</p>
							<div ng-controller="commentCtrl">
								<button class="btn btn-default btn-xs" ng-hide="commentForm" ng-click="commentForm = true">Reply</button>
								<div ng-show="commentForm">
									<h1>Reply to {c.author}</h1>
									<a ng-click="commentForm = false">Cancel reply</a>
									<p>Required fields are marked *</p>
									<div class="row">
										<form action="/comment" method="POST" class="col-sm-5">
											{if (session == None)
												<div class="form-group">
													<input type="text" class="form-control" name="name" placeholder="Your Name*" required=""/></div>
												<div class="form-group">
													<input type="text" class="form-control" name="url" placeholder="Your URL"/></div>
											}
											<div class="form-group">
												<textarea class="form-control" rows="4" cols="50" name="text" required=""></textarea></div>
											<input type="hidden" name="postid" value={postid.toString}/>
											<input type="hidden" name="replytoid" value={c.id.toString}/>
											<p><button type="submit" class="btn btn-default">Submit reply</button></p>
										</form>
									</div>
								</div>
							</div>
						</div>
						{commentsLevel( r, level + 1 )}
					</xml:group>
				}
		}
		
		<div id={s"comments-$postid"} class="blog-comments">
			{if (!cs.isEmpty)
				<xml:group>
					<h1>{count} Comment{if (count == 1) "" else "s"}</h1>
					{commentsLevel( cs, 0 )}
				</xml:group>
			}
			<!-- {if (session == None)
				<a href="/register">Register to leave a comment</a>
			else { -->
				<div ng-controller="commentCtrl">
					<button class="btn btn-default btn-xs" ng-hide="commentForm" ng-click="commentForm = true">Leave a comment</button>
					<div ng-show="commentForm">
						<h1>Leave a comment</h1>
						<a ng-click="commentForm = false">Cancel comment</a>
						<p>Required fields are marked *</p>
						<div class="row">
							<form action="/comment" method="POST" class="col-sm-5">
								{if (session == None)
									<div class="form-group">
										<input type="text" class="form-control" name="name" placeholder="Your Name*" required=""/></div>
									<div class="form-group">
										<input type="text" class="form-control" name="url" placeholder="Your URL"/></div>
								}
								<div class="form-group">
									<textarea class="form-control" rows="4" cols="50" name="text" required=""></textarea></div>
								<input type="hidden" name="postid" value={postid.toString}/>
								<p><button type="submit" class="btn btn-default">Submit comment</button></p>
							</form>
						</div>
					</div>
				</div>
				<!-- }
			} -->
		</div>
	}
	
}
