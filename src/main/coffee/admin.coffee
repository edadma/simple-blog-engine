app = angular.module 'admin', ['ngSanitize', 'ngResource']

app.controller 'AdminController', ['$scope', '$resource', ($scope, $resource) ->
	Posts = $resource '/api/v1/posts/:id'
	
	$scope.categories = {}
	$scope.mode = 'post'
	
	getPosts = ->
		Posts.query (result, response) ->
			$scope.posts = result
		,	(response) ->
			$scope.error = response.data
	
	getPosts()
	
	$scope.clear = ->
		$scope.categories = {}
		$scope.error = false
		$scope.posted = false
		$scope.title = ""
		$scope.content = ""
		
	$scope.edit = (post) ->
		$scope.mode = 'edit'
		$scope.categories = post.categories
		$scope.error = false
		$scope.success = false
		$scope.title = post.title
		$scope.content = post.content
		$scope.id = post.id
		
	$scope.update = ->
		categories = []
		
		for k, v of $scope.categories
			if v
				categories.push( parseInt(v) )
				
		if !$scope.title
			$scope.error = "There is no title."
		else if !$scope.content
			$scope.error = "There is no content."
		else if categories.length == 0
			$scope.error = "There is no category."
		else
			$scope.error = false
			Posts.save {id: $scope.id},
				title: $scope.title
				content: $scope.content
				categories: categories
			, (result, response) ->
				if result.updated != 1
					$scope.error = "update failed"
				else
					$scope.success = "post updated"
					
				getPosts()
			, (response) ->
				$scope.error = response.data	
	
	$scope.publish = ->
	
	
	$scope.post = ->
		$scope.mode = 'post'
		$scope.clear()
		
	$scope.submit = ->
		categories = []
		
		for k, v of $scope.categories
			if v
				categories.push( parseInt(v) )
				
		if !$scope.title
			$scope.error = "There is no title."
		else if !$scope.content
			$scope.error = "There is no content."
		else if categories.length == 0
			$scope.error = "There is no category."
		else
			$scope.error = false
			Posts.save
				title: $scope.title
				content: $scope.content
				categories: categories
			, (result, response) ->
				$scope.success = "post submitted"
				getPosts()
			, (response) ->
				$scope.error = response.data
	]