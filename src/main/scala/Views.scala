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
		main( "Login: " + blog.title ) {
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
	
	def admin( blog: dao.Blog, user: models.User ) =
		main( "Dashboard: " + blog.title ) {
			<xml:group>
				<link href="/css/admin.css" rel="stylesheet"/>
				<script src="/webjars/angularjs/1.4.3/angular.min.js"></script>
				<script src="/webjars/angularjs/1.4.3/angular-sanitize.min.js"></script>
				<script src="/webjars/angularjs/1.4.3/angular-resource.min.js"></script>
				<script src="/coffee/admin.js"></script>
				<script src="/coffee/post.js"></script>
			</xml:group>
		} {
			<div ng-app="admin" ng-controller="AdminController">
				<nav class="navbar navbar-default navbar-fixed-top">
					<div class="container-fluid">
						<div class="navbar-header">
							<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
								<span class="sr-only">Toggle navigation</span>
								<span class="icon-bar"></span>
								<span class="icon-bar"></span>
								<span class="icon-bar"></span>
							</button>
							<a class="navbar-brand" href="/">{blog.title}</a>
						</div>
						<div id="navbar" class="navbar-collapse collapse">
							<ul class="nav navbar-nav navbar-right">
								<!-- <li><a href="/admin">Dashboard</a></li>
								<li><a href="#">Settings</a></li>
								<li><a href="#">Profile</a></li> -->
								<li><a href="/logout">Logout</a></li>
							</ul>
							<form class="navbar-form navbar-right">
								<input type="text" class="form-control" placeholder="Search..."/>
							</form>
						</div>
					</div>
				</nav>
				
				<div class="container-fluid">

					<div class="row" ng-controller="PostController">
						
						<div class="col-sm-3 col-md-2 sidebar">
							<ul class="nav nav-sidebar">
								<!-- <li class="active"><a href="#">Overview <span class="sr-only">(current)</span></a></li> -->
								<li><a href="#">Posts</a></li>
							</ul>
						</div>
						
						<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
							<h1 class="page-header">Blog Posts</h1>

							<div class="table-responsive">
								<table class="table table-striped table-hover">
									<thead>
										<tr>
											<th>#</th>
											<th>Date</th>
											<th>Time</th>
											<th>Title</th>
											<th>Categories</th>
											<th>Author</th>
											<th>Status</th>
											<!-- <th>Comments</th> -->
										</tr>
									</thead>
									<tbody>
										<tr ng-repeat="post in posts" ng-click="edit(post)" ng-cloak="">
											<td>{"{{post.id}}"}</td>
											<td>{"{{post.date.millis | date: 'yy-MM-dd'}}"}</td>
											<td>{"{{post.date.millis | date: 'HH:mm'}}"}</td>
											<td>{"{{post.title | limitTo: 25}}"}</td>
											<td>{"{{keys(post.categories)}}"}</td>
											<td>{"{{post.author}}"}</td>
											<td><span ng-class="'label label-' + (post.status == 'live' ? 'success' : 'warning')">{"{{post.status}}"}</span></td>
										</tr>
									</tbody>
								</table>
							</div>
							
							<h2 class="sub-header" ng-cloak="">{"{{mode == 'post' ? 'New Post' : 'Editing Post #' + id}}"}</h2>
							
							<div class="row">
								
								<div class="col-md-10">
								
									<div class="row">
									
										<div class="col-md-5">
											<div class="form-group">
												<label>Post Title</label>
												<input type="text" class="form-control" ng-model="title" autofocus=""/></div>
										</div>
										
										<div class="col-md-5">
											<div class="form-group">
												<label>Categories</label><br/>
												{
													for ((id, name) <- Queries.findAllCategories( blog.id.get ))
														yield
															<label style="font-weight: normal;"><input type="checkbox" ng-model={"categories." + name} ng-true-value={id.toString} ng-false-value="false"/>&nbsp;{name}&nbsp;&nbsp;</label>
												}
												</div>
										</div>
									
										<div class="col-md-2">
											<div class="form-group">
												<label>Status</label><br/>
												<label class="radio-inline">
													<input type="radio" ng-model="status" value="live"/> live
												</label>
												<label class="radio-inline">
													<input type="radio" ng-model="status" value="draft"/> draft
												</label>
											</div>
										</div>
										
									</div>
									
									<div class="row">
								
										<div class="col-md-12" ng-cloak="">
											<ng-include src="'message.html'"></ng-include>
										</div>
								
									</div>
								</div>
								
								<div class="col-md-2" ng-cloak="">
									<button ng-show="mode == 'edit'" ng-click="post()" class="btn btn-default btn-block thin">New</button>
									<button ng-show="mode == 'post'" ng-click="submit()" class="btn btn-default btn-block thin">Submit</button>
									<button ng-show="mode == 'edit'" ng-click="update()" class="btn btn-default btn-block thin">Update</button>
									<button ng-show="mode == 'post'" ng-click="clear()" class="btn btn-default btn-block thin">Clear</button>
									<button ng-show="mode == 'edit'" ng-click="delete()" class="btn btn-default btn-block thin">Delete</button>
								</div>
							
							</div>
							
							<div class="row">
							
								<div class="col-md-12">
									<div class="form-group">
										<label>Post Content</label>
										<textarea class="form-control" rows="10" ng-model="content"></textarea></div>
									<div class="panel panel-default">
										<div class="panel-heading">Preview</div>
										<div class="panel-body">
											<style scoped=""> {
												io.Source.fromInputStream( getClass.getResourceAsStream("blog.css") ).getLines.mkString("\n")
											}
											</style>
											<h2 class="blog-post-title"><span ng-bind="title"/></h2>
											<div ng-bind-html="content"></div>
										</div>
									</div>
								</div>
								
							</div>
								
						</div>
								
					</div>
					
				</div>
			</div>
		}
		
	def blog( b: dao.Blog, user: Option[models.User], newer: Boolean, older: Boolean, recent: Seq[models.Post],
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
								if (user != None)
									<xml:group>
										<a class="blog-nav-item navbar-right" href="/logout">Logout</a>
										{if (user.get.is( b.id.get, "admin"))
											<a class="blog-nav-item navbar-right" href="/admin">Admin</a>}
										{if (user.get.is( b.id.get, "author")) <a class="blog-nav-item navbar-right" href="/post">Post</a>}
									</xml:group>
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
								val live = posts filter (_._1.status == "live")
								
								if (live isEmpty)
									<p>no posts</p>
								else
									live map {case (p, c, count) => post(user, p, c, count)}
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
									{for (p <- recent) yield <li><a href={"/" + p.id}>{p.title}</a></li>}
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
	
	def post( user: Option[models.User], p: Post, cs: Seq[CommentWithReplies], count: Int ) =
		<xml:group>
			<div class="blog-post">
				<h2 class="blog-post-title">{p.title}</h2>
				<p class="blog-post-meta">{postDateFormat.print(p.date)} by <a href={"/author/" + p.authorid}>{p.author}</a> in {
					if (p.categories isEmpty)
						<a href="/uncategorized">Uncategorized</a>
					else
						xml.Unparsed(p.categories map {case (name, id) => """<a href="/category/""" + id + """">""" + name + "</a>"} mkString (", "))
				} | <a href={s"#comments-${p.id}"}>{count} comment{if (count == 1) "" else "s"}</a></p>
				<div>
					{xml.Unparsed( p.content )}
				</div>
				{comments( user, p.id, cs, count )}
			</div>
		</xml:group>
	
	def comments( user: Option[models.User], postid: Int, cs: Seq[CommentWithReplies], count: Int ): xml.Node = {
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
											{if (user == None)
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
			<div ng-controller="commentCtrl">
				<button class="btn btn-default btn-xs" ng-hide="commentForm" ng-click="commentForm = true">Leave a comment</button>
				<div ng-show="commentForm">
					<h1>Leave a comment</h1>
					<a ng-click="commentForm = false">Cancel comment</a>
					<p>Required fields are marked *</p>
					<div class="row">
						<form action="/comment" method="POST" class="col-sm-5">
							{if (user == None)
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
		</div>
	}
	
}
