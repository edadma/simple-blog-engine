app = angular.module 'admin', ['ngSanitize', 'ngResource']

app.controller 'AdminController', ['$scope', '$resource', ($scope, $resource) ->
	Posts = $resource '/api/v1/posts/:id'
	
	$scope.categories = {}
	$scope.mode = 'post'
	$scope.status = 'live'
	
	getPosts = ->
		Posts.query (result, response) ->
			$scope.posts = result
		,	(response) ->
			$scope.error = response.data
	
	getPosts()
	
	$scope.clear = ->
		$scope.categories = {}
		$scope.status = 'live'
		$scope.error = false
		$scope.success = false
		$scope.title = ""
		$scope.content = ""
		
	$scope.edit = (post) ->
		postcopy = angular.copy( post )
		$scope.mode = 'edit'
		$scope.categories = postcopy.categories
		$scope.error = false
		$scope.success = false
		$scope.title = postcopy.title
		$scope.content = postcopy.content
		$scope.status = postcopy.status
		$scope.id = postcopy.id
		
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
				status: $scope.status
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
				status: $scope.status
				categories: categories
			, (result, response) ->
				$scope.success = "post submitted"
				getPosts()
			, (response) ->
				$scope.error = response.data
				
	$scope.keys = (obj) ->
		(for k, v of obj
			k).join(', ')
	]
