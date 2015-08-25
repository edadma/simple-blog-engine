app = angular.module 'post', ['ngSanitize', 'ngResource']

app.controller 'PostController', ['$scope', '$resource', ($scope, $resource) ->
	Posts = $resource '/api/v1/posts'
	
	$scope.categories = {}
	
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
				$scope.posted = true
				getPosts()
			, (response) ->
				$scope.error = response.data
	]