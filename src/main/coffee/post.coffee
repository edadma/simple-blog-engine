app = angular.module 'post', ['ngSanitize', 'ngResource']

app.controller 'PostController', ['$scope', '$resource', ($scope, $resource) ->
	Post = $resource '/api/v1/post'
	
	$scope.categories = {}
	
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
			Post.save
				title: $scope.title
				content: $scope.content
				categories: categories
			, (result, response) ->
				$scope.posted = true
			, (response) ->
				$scope.error = response.data
	]